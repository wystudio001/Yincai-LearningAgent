package xyz.wystudio.yincai.ai.infrastructure.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import xyz.wystudio.yincai.ai.infrastructure.persistence.entity.AiModelProviderEntity;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class OpenAiCompatibleClient {

    private final ObjectMapper objectMapper;

    public Map<String, Object> chat(AiModelProviderEntity provider, Map<String, Object> request) {
        validateProvider(provider);
        Map<String, Object> payload = buildPayload(provider, request, false);
        String responseBody = postJson(provider, chatCompletionsEndpoint(provider.getBaseUrl()), payload, "application/json");
        return parseChatResponse(provider, payload, responseBody);
    }

    public String chatStreamAsText(AiModelProviderEntity provider, Map<String, Object> request) {
        StringBuilder result = new StringBuilder();
        chatStream(provider, request, result::append, () -> false);
        return result.toString();
    }

    /** Read the upstream SSE response incrementally and invoke the callback per text delta. */
    public void chatStream(
            AiModelProviderEntity provider,
            Map<String, Object> request,
            Consumer<String> onDelta,
            BooleanSupplier cancelled
    ) {
        validateProvider(provider);
        Map<String, Object> payload = buildPayload(provider, request, true);
        String endpoint = chatCompletionsEndpoint(provider.getBaseUrl());
        try {
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofMillis(timeoutMs(provider)))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .header("Authorization", "Bearer " + provider.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs(provider)))
                    .build();
            HttpResponse<InputStream> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream stream = response.body()) {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE,
                            "大模型接口调用失败，HTTP " + response.statusCode() + "：" + truncate(readAll(stream)));
                }
                String contentType = response.headers().firstValue("Content-Type").orElse("");
                if (contentType.toLowerCase(Locale.ROOT).contains("text/event-stream")) {
                    boolean emitted = readSse(stream, onDelta, cancelled);
                    if (!emitted && (cancelled == null || !cancelled.getAsBoolean())) {
                        throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE,
                                "大模型返回为空，请检查模型配置或稍后重试");
                    }
                } else {
                    // Some OpenAI-compatible gateways ignore stream=true and return one JSON response.
                    String responseBody = readAll(stream);
                    String content = extractChatContent(responseBody);
                    if (StringUtils.hasText(content)) {
                        onDelta.accept(content);
                    } else if (cancelled == null || !cancelled.getAsBoolean()) {
                        throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE,
                                "大模型返回为空，请检查模型配置或稍后重试");
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "baseURL 格式不正确：" + e.getMessage(), e);
        } catch (BizException e) {
            throw e;
        } catch (IOException e) {
            if (cancelled != null && cancelled.getAsBoolean()) {
                return;
            }
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "大模型接口通信失败：" + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (cancelled != null && cancelled.getAsBoolean()) {
                return;
            }
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "大模型接口调用被中断", e);
        }
    }

    public List<String> fetchModels(String baseUrl, String apiKey, Integer timeoutMs) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "baseURL不能为空");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "API Key不能为空");
        }
        String responseBody = getJson(modelCatalogEndpoint(baseUrl), apiKey, timeoutMs == null || timeoutMs <= 0 ? 30000 : timeoutMs);
        return parseModelIds(responseBody);
    }

    public Map<String, Object> test(AiModelProviderEntity provider, String prompt) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("messages", List.of(Map.of("role", "user", "content", StringUtils.hasText(prompt) ? prompt : "请回复 pong")));
        request.put("maxTokens", 64);
        return chat(provider, request);
    }

    private Map<String, Object> buildPayload(AiModelProviderEntity provider, Map<String, Object> request, boolean stream) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", stringValue(request.get("model"), provider.getDefaultModel()));
        payload.put("messages", normalizeMessages(request));
        payload.put("temperature", decimalValue(request.get("temperature"), provider.getTemperature()));
        payload.put("max_tokens", intValue(request.get("maxTokens"), intValue(request.get("max_tokens"), provider.getMaxTokens())));
        if (stream) {
            payload.put("stream", true);
        }
        return payload;
    }

    private List<Map<String, Object>> normalizeMessages(Map<String, Object> request) {
        Object rawMessages = request.get("messages");
        if (rawMessages instanceof List<?> list && !list.isEmpty()) {
            List<Map<String, Object>> messages = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> raw) {
                    Map<String, Object> message = new LinkedHashMap<>();
                    message.put("role", stringValue(raw.get("role"), "user"));
                    message.put("content", stringValue(raw.get("content"), ""));
                    messages.add(message);
                }
            }
            if (!messages.isEmpty()) {
                return messages;
            }
        }
        String prompt = stringValue(request.get("prompt"), stringValue(request.get("question"), "请介绍因材智学系统"));
        return List.of(Map.of("role", "user", "content", prompt));
    }

    private String postJson(AiModelProviderEntity provider, String endpoint, Map<String, Object> payload, String accept) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofMillis(timeoutMs(provider)))
                    .header("Content-Type", "application/json")
                    .header("Accept", accept)
                    .header("Authorization", "Bearer " + provider.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs(provider)))
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "大模型接口调用失败，HTTP " + response.statusCode() + "：" + truncate(response.body()));
            }
            return response.body();
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "baseURL 格式不正确：" + e.getMessage(), e);
        } catch (IOException e) {
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "大模型接口通信失败：" + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "大模型接口调用被中断", e);
        }
    }

    private String getJson(String endpoint, String apiKey, int timeoutMs) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "模型列表获取失败，HTTP " + response.statusCode() + "：" + truncate(response.body()));
            }
            return response.body();
        } catch (IllegalArgumentException e) {
            throw new BizException(ErrorCode.BAD_REQUEST, "baseURL格式不正确：" + e.getMessage(), e);
        } catch (IOException e) {
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "模型列表获取通信失败：" + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "模型列表获取被中断", e);
        }
    }

    private List<String> parseModelIds(String responseBody) {
        try {
            JsonNode data = objectMapper.readTree(responseBody).path("data");
            List<String> ids = new ArrayList<>();
            if (data.isArray()) {
                for (JsonNode item : data) {
                    String id = item.path("id").asText("");
                    if (StringUtils.hasText(id)) {
                        ids.add(id);
                    }
                }
            }
            ids.sort(String::compareToIgnoreCase);
            return ids;
        } catch (Exception e) {
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "模型列表响应解析失败：" + e.getMessage(), e);
        }
    }

    public String modelCatalogEndpoint(String baseUrl) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.endsWith("/chat/completions")) {
            base = base.substring(0, base.length() - "/chat/completions".length());
        }
        if (base.endsWith("/models")) {
            return base;
        }
        return base + "/models";
    }

    private Map<String, Object> parseChatResponse(AiModelProviderEntity provider, Map<String, Object> payload, String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choice = root.path("choices").isArray() && !root.path("choices").isEmpty()
                    ? root.path("choices").get(0)
                    : objectMapper.createObjectNode();
            String content = extractChatContent(root);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("providerId", String.valueOf(provider.getId()));
            result.put("providerName", provider.getName());
            result.put("model", root.path("model").asText(String.valueOf(payload.get("model"))));
            result.put("content", content);
            result.put("finishReason", choice.path("finish_reason").asText(""));
            result.put("usage", objectMapper.convertValue(root.path("usage"), Map.class));
            return result;
        } catch (Exception e) {
            throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "大模型响应解析失败：" + e.getMessage(), e);
        }
    }

    private boolean readSse(InputStream stream, Consumer<String> onDelta, BooleanSupplier cancelled) throws IOException {
        boolean emitted = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (cancelled != null && cancelled.getAsBoolean()) {
                    return emitted;
                }
                if (line.isEmpty()) {
                    emitted = emitSseData(data, onDelta) || emitted;
                    data.setLength(0);
                    continue;
                }
                if (line.startsWith("data:")) {
                    if (data.length() > 0) {
                        data.append('\n');
                    }
                    String value = line.substring("data:".length());
                    data.append(value.startsWith(" ") ? value.substring(1) : value);
                }
            }
            emitted = emitSseData(data, onDelta) || emitted;
        }
        return emitted;
    }

    private boolean emitSseData(StringBuilder data, Consumer<String> onDelta) throws IOException {
        if (data.length() == 0 || onDelta == null) {
            return false;
        }
        String raw = data.toString().trim();
        if (raw.isEmpty() || "[DONE]".equals(raw)) {
            return false;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            if (root.has("error")) {
                throw new BizException(ErrorCode.AI_SERVICE_UNAVAILABLE, "大模型流式错误：" + root.path("error").toString());
            }
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return false;
            }
            JsonNode delta = choices.path(0).path("delta");
            String value = extractText(delta.path("content"));
            if (!StringUtils.hasText(value)) {
                value = extractText(delta.path("text"));
            }
            if (!StringUtils.hasText(value)) {
                value = extractText(choices.path(0).path("text"));
            }
            if (StringUtils.hasText(value)) {
                onDelta.accept(value);
                return true;
            }
            return false;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("大模型流式响应解析失败：" + e.getMessage(), e);
        }
    }

    private String extractChatContent(String responseBody) throws IOException {
        try {
            return extractChatContent(objectMapper.readTree(responseBody));
        } catch (Exception e) {
            throw new IOException("大模型响应解析失败：" + e.getMessage(), e);
        }
    }

    private String extractChatContent(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return "";
        }
        String direct = extractText(root.path("output_text"));
        if (StringUtils.hasText(direct)) {
            return direct;
        }
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return "";
        }
        JsonNode choice = choices.get(0);
        String content = extractText(choice.path("message").path("content"));
        if (!StringUtils.hasText(content)) {
            content = extractText(choice.path("delta").path("content"));
        }
        if (!StringUtils.hasText(content)) {
            content = extractText(choice.path("text"));
        }
        return content;
    }

    private String extractText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isArray()) {
            StringBuilder text = new StringBuilder();
            for (JsonNode item : node) {
                String value = extractText(item.path("text"));
                if (!StringUtils.hasText(value)) {
                    value = extractText(item);
                }
                if (StringUtils.hasText(value)) {
                    text.append(value);
                }
            }
            return text.toString();
        }
        return "";
    }

    private String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public String chatCompletionsEndpoint(String baseUrl) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.endsWith("/chat/completions")) {
            return base;
        }
        return base + "/chat/completions";
    }

    private void validateProvider(AiModelProviderEntity provider) {
        if (provider == null || provider.getDeleted() != null && provider.getDeleted() == 1) {
            throw new BizException(ErrorCode.BAD_REQUEST, "大模型配置不存在");
        }
        if (!Boolean.TRUE.equals(provider.getEnabled())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "大模型配置未启用");
        }
        if (!StringUtils.hasText(provider.getBaseUrl()) || !StringUtils.hasText(provider.getApiKey()) || !StringUtils.hasText(provider.getDefaultModel())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "大模型 baseURL、key 和默认模型必须完整配置");
        }
    }

    private int timeoutMs(AiModelProviderEntity provider) {
        return provider.getTimeoutMs() == null || provider.getTimeoutMs() <= 0 ? 120000 : provider.getTimeoutMs();
    }

    private String stringValue(Object value, String defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return String.valueOf(value).trim();
    }

    private BigDecimal decimalValue(Object value, BigDecimal defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue == null ? new BigDecimal("0.20") : defaultValue;
        }
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }

    private Integer intValue(Object value, Integer defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue == null ? 2048 : defaultValue;
        }
        return value instanceof Number number ? number.intValue() : Integer.valueOf(String.valueOf(value));
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
