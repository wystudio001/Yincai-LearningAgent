package xyz.wystudio.yincai.ai.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wystudio.yincai.ai.infrastructure.llm.OpenAiCompatibleClient;
import xyz.wystudio.yincai.ai.infrastructure.persistence.entity.AiModelProviderEntity;
import xyz.wystudio.yincai.ai.infrastructure.persistence.mapper.AiModelProviderMapper;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.security.CurrentUser;
import xyz.wystudio.yincai.security.UserContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ModelProviderApplicationService {

    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "PLATFORM_ADMIN", "TEACHER");

    private final AiModelProviderMapper modelProviderMapper;
    private final OpenAiCompatibleClient openAiCompatibleClient;

    public List<Map<String, Object>> listProviders() {
        requireAdmin();
        return modelProviderMapper.selectList(new LambdaQueryWrapper<AiModelProviderEntity>()
                        .orderByDesc(AiModelProviderEntity::getEnabled)
                        .orderByDesc(AiModelProviderEntity::getUpdatedAt))
                .stream()
                .map(this::toSafeMap)
                .toList();
    }

    public Map<String, Object> createProvider(Map<String, Object> body) {
        requireAdmin();
        AiModelProviderEntity entity = new AiModelProviderEntity();
        entity.setTenantId(longValue(body.get("tenantId"), tenantId()));
        applyRequiredFields(entity, body, true);
        entity.setEnabled(booleanValue(body.get("enabled"), false));
        entity.setDeleted(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        modelProviderMapper.insert(entity);
        return toSafeMap(entity);
    }

    public Map<String, Object> updateProvider(Long id, Map<String, Object> body) {
        requireAdmin();
        AiModelProviderEntity entity = requireProvider(id);
        applyRequiredFields(entity, body, false);
        if (body.containsKey("enabled")) {
            entity.setEnabled(booleanValue(body.get("enabled"), entity.getEnabled()));
        }
        entity.setUpdatedAt(LocalDateTime.now());
        modelProviderMapper.updateById(entity);
        return toSafeMap(entity);
    }

    public void deleteProvider(Long id) {
        requireAdmin();
        requireProvider(id);
        modelProviderMapper.deleteById(id);
    }

    public Map<String, Object> testProvider(Long id, Map<String, Object> body) {
        requireAdmin();
        AiModelProviderEntity entity = requireProvider(id);
        String prompt = stringValue(body.get("prompt"));
        Map<String, Object> result = openAiCompatibleClient.test(entity, prompt);
        result.put("endpoint", openAiCompatibleClient.chatCompletionsEndpoint(entity.getBaseUrl()));
        return result;
    }

    public Map<String, Object> fetchModels(Map<String, Object> body) {
        requireAdmin();
        Long providerId = longValue(body.get("providerId"), null);
        AiModelProviderEntity entity = providerId == null ? null : requireProvider(providerId);
        String requestBaseUrl = stringValue(body.get("baseUrl"));
        String requestApiKey = stringValue(body.get("apiKey"));
        String baseUrl = StringUtils.hasText(requestBaseUrl) ? requestBaseUrl : entity == null ? null : entity.getBaseUrl();
        String apiKey = StringUtils.hasText(requestApiKey) ? requestApiKey : entity == null ? null : entity.getApiKey();
        Integer timeoutMs = intValue(body.get("timeoutMs"), entity == null ? 30000 : entity.getTimeoutMs());
        List<String> models = openAiCompatibleClient.fetchModels(trimTrailingSlash(requiredValue(baseUrl, "baseUrl")), requiredValue(apiKey, "apiKey"), timeoutMs);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("endpoint", openAiCompatibleClient.modelCatalogEndpoint(baseUrl));
        result.put("models", models);
        result.put("total", models.size());
        return result;
    }

    public AiModelProviderEntity resolveProvider(Map<String, Object> request) {
        Long providerId = longValue(request.get("providerId"), longValue(request.get("modelProviderId"), null));
        if (providerId != null) {
            return requireProvider(providerId);
        }
        AiModelProviderEntity active = modelProviderMapper.selectList(new LambdaQueryWrapper<AiModelProviderEntity>()
                        .eq(AiModelProviderEntity::getEnabled, true)
                        .orderByDesc(AiModelProviderEntity::getUpdatedAt)
                        .last("LIMIT 1"))
                .stream()
                .findFirst()
                .orElse(null);
        if (active == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "大模型API尚未配置，请先在管理端配置baseURL和key");
        }
        return active;
    }

    public AiModelProviderEntity requireProvider(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "模型配置ID不能为空");
        }
        AiModelProviderEntity entity = modelProviderMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "模型配置不存在");
        }
        return entity;
    }

    private void applyRequiredFields(AiModelProviderEntity entity, Map<String, Object> body, boolean create) {
        if (create || body.containsKey("name")) {
            entity.setName(requiredString(body, "name"));
        }
        if (create || body.containsKey("providerCode")) {
            String providerCode = stringValue(body.get("providerCode"));
            entity.setProviderCode(StringUtils.hasText(providerCode) ? providerCode.toLowerCase(Locale.ROOT) : normalizeCode(entity.getName()));
        }
        if (create || body.containsKey("baseUrl")) {
            entity.setBaseUrl(trimTrailingSlash(requiredString(body, "baseUrl")));
        }
        if (create || body.containsKey("defaultModel") || body.containsKey("model")) {
            entity.setDefaultModel(requiredString(body, body.containsKey("defaultModel") ? "defaultModel" : "model"));
        }
        String apiKey = stringValue(body.get("apiKey"));
        if (create || StringUtils.hasText(apiKey)) {
            if (!StringUtils.hasText(apiKey)) {
                throw new BizException(ErrorCode.BAD_REQUEST, "API Key不能为空");
            }
            entity.setApiKey(apiKey);
        }
        if (body.containsKey("timeoutMs")) {
            entity.setTimeoutMs(intValue(body.get("timeoutMs"), 120000));
        } else if (create) {
            entity.setTimeoutMs(120000);
        }
        if (body.containsKey("temperature")) {
            entity.setTemperature(decimalValue(body.get("temperature"), new BigDecimal("0.20")));
        } else if (create) {
            entity.setTemperature(new BigDecimal("0.20"));
        }
        if (body.containsKey("maxTokens")) {
            entity.setMaxTokens(intValue(body.get("maxTokens"), 2048));
        } else if (create) {
            entity.setMaxTokens(2048);
        }
        if (body.containsKey("remark")) {
            entity.setRemark(stringValue(body.get("remark")));
        }
    }

    private Map<String, Object> toSafeMap(AiModelProviderEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", String.valueOf(entity.getId()));
        map.put("tenantId", entity.getTenantId());
        map.put("name", entity.getName());
        map.put("providerCode", entity.getProviderCode());
        map.put("baseUrl", entity.getBaseUrl());
        map.put("defaultModel", entity.getDefaultModel());
        map.put("model", entity.getDefaultModel());
        map.put("apiKeyMasked", maskKey(entity.getApiKey()));
        map.put("keyMasked", maskKey(entity.getApiKey()));
        map.put("enabled", entity.getEnabled());
        map.put("timeoutMs", entity.getTimeoutMs());
        map.put("temperature", entity.getTemperature());
        map.put("maxTokens", entity.getMaxTokens());
        map.put("remark", entity.getRemark());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private void requireAdmin() {
        CurrentUser currentUser = UserContextHolder.get();
        String roleCode = currentUser == null ? null : currentUser.getRoleCode();
        if (!StringUtils.hasText(roleCode) || !ADMIN_ROLES.contains(roleCode)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只有管理员和老师可以管理大模型配置");
        }
    }

    private Long tenantId() {
        Long tenantId = UserContextHolder.getTenantId();
        return tenantId == null ? 0L : tenantId;
    }

    private String requiredString(Map<String, Object> body, String key) {
        String value = stringValue(body.get(key));
        if (!StringUtils.hasText(value)) {
            throw new BizException(ErrorCode.BAD_REQUEST, key + "不能为空");
        }
        return value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String requiredValue(String value, String key) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(ErrorCode.BAD_REQUEST, key + "不能为空");
        }
        return value.trim();
    }

    private String normalizeCode(String name) {
        if (!StringUtils.hasText(name)) {
            return "custom";
        }
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String trimTrailingSlash(String value) {
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private Boolean booleanValue(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Long longValue(Object value, Long defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
    }

    private Integer intValue(Object value, Integer defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return value instanceof Number number ? number.intValue() : Integer.valueOf(String.valueOf(value));
    }

    private BigDecimal decimalValue(Object value, BigDecimal defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }

    private String maskKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return "未配置";
        }
        String value = apiKey.trim();
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, Math.min(6, value.length())) + "****" + value.substring(value.length() - 4);
    }
}
