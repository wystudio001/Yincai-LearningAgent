package xyz.wystudio.yincai.ai.application.service;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.wystudio.yincai.ai.application.agent.CoordinatorAgentService;
import xyz.wystudio.yincai.ai.application.agent.ProfileAgentService;
import xyz.wystudio.yincai.ai.application.agent.ResourceAgentService;
import xyz.wystudio.yincai.ai.infrastructure.llm.OpenAiCompatibleClient;
import xyz.wystudio.yincai.ai.infrastructure.persistence.entity.AiModelProviderEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiApplicationService {

    private final CoordinatorAgentService coordinatorAgentService;
    private final ProfileAgentService profileAgentService;
    private final ResourceAgentService resourceAgentService;
    private final ModelProviderApplicationService modelProviderApplicationService;
    private final OpenAiCompatibleClient openAiCompatibleClient;

    public Map<String, Object> createAgentTask(Map<String, Object> request) {
        log.info("Creating agent task with request: {}", request);
        return Map.of("taskId", "task-" + System.currentTimeMillis(),
                "status", "pending",
                "type", request.getOrDefault("type", "unknown"));
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        log.info("Getting task status for taskId: {}", taskId);
        return Map.of("taskId", taskId,
                "status", "running",
                "progress", 50);
    }

    public Map<String, Object> cancelTask(String taskId) {
        log.info("Cancelling task: {}", taskId);
        return Map.of("taskId", taskId,
                "status", "cancelled");
    }

    public String streamTaskProgress(String taskId) {
        log.info("Streaming task progress for taskId: {}", taskId);
        return "Streaming progress for task: " + taskId;
    }

    public Map<String, Object> extractProfile(Map<String, Object> request) {
        log.info("Extracting profile with request: {}", request);
        Long userId = request.get("userId") != null ? Long.valueOf(request.get("userId").toString()) : null;
        return profileAgentService.extractProfile(userId);
    }

    public Map<String, Object> generateResources(Map<String, Object> request) {
        log.info("Generating resources with request: {}", request);
        return resourceAgentService.generate(request);
    }

    public Map<String, Object> planLearningPath(Map<String, Object> request) {
        log.info("Planning learning path with request: {}", request);
        return coordinatorAgentService.orchestrate(request);
    }

    public Map<String, Object> tutorAnswer(Map<String, Object> request) {
        log.info("Tutor answering with request: {}", request);
        return llmChat(withSystemPrompt(request, "你是因材智学系统的大学计算机专业学习导师。请用清晰、准确、循序渐进的方式回答学生问题，并给出后续学习建议。"));
    }

    public Map<String, Object> analyzeAssessment(Map<String, Object> request) {
        log.info("Analyzing assessment with request: {}", request);
        return llmChat(withSystemPrompt(request, "你是因材智学系统的测评分析助手。请根据题目、答案记录和知识点掌握情况，输出薄弱点、原因分析和可执行的复习计划。"));
    }

    public Map<String, Object> ragSearch(Map<String, Object> request) {
        log.info("Performing RAG search with request: {}", request);
        return Map.of("results", List.of(),
                "totalHits", 0);
    }

    public Map<String, Object> generateEmbeddings(Map<String, Object> request) {
        log.info("Generating embeddings with request: {}", request);
        return Map.of("embeddings", List.of(),
                "dimension", 0);
    }

    public Map<String, Object> safetyReview(Map<String, Object> request) {
        log.info("Performing safety review with request: {}", request);
        return Map.of("passed", true,
                "riskLevel", "low",
                "issues", List.of());
    }

    public Map<String, Object> llmChat(Map<String, Object> request) {
        log.info("LLM chat with request: {}", request);
        AiModelProviderEntity provider = modelProviderApplicationService.resolveProvider(request);
        return openAiCompatibleClient.chat(provider, request);
    }

    public String llmChatStream(Map<String, Object> request) {
        log.info("LLM chat stream with request: {}", request);
        AiModelProviderEntity provider = modelProviderApplicationService.resolveProvider(request);
        return openAiCompatibleClient.chatStreamAsText(provider, request);
    }

    public void llmChatStream(
            Map<String, Object> request,
            Consumer<String> onDelta,
            BooleanSupplier cancelled
    ) {
        llmChatStream(request, onDelta, ignored -> { }, cancelled);
    }

    public void llmChatStream(
            Map<String, Object> request,
            Consumer<String> onDelta,
            Consumer<String> onReasoning,
            BooleanSupplier cancelled
    ) {
        log.info("LLM chat stream callback with request: {}", request);
        AiModelProviderEntity provider = modelProviderApplicationService.resolveProvider(request);
        openAiCompatibleClient.chatStream(provider, request, onDelta, onReasoning, cancelled);
    }

    public void tutorAnswerStream(
            Map<String, Object> request,
            Consumer<String> onDelta,
            BooleanSupplier cancelled
    ) {
        tutorAnswerStream(request, onDelta, ignored -> { }, cancelled);
    }

    public void tutorAnswerStream(
            Map<String, Object> request,
            Consumer<String> onDelta,
            Consumer<String> onReasoning,
            BooleanSupplier cancelled
    ) {
        log.info("Tutor answering in stream mode with request: {}", request);
        AiModelProviderEntity provider = modelProviderApplicationService.resolveProvider(request);
        openAiCompatibleClient.chatStream(
                provider,
                withSystemPrompt(request, "你是因材智学系统的大学计算机专业学习导师。请用清晰、准确、循序渐进的方式回答学生问题，并给出后续学习建议。"),
                onDelta,
                onReasoning,
                cancelled
        );
    }

    public Map<String, Object> multimodalGenerate(Map<String, Object> request) {
        log.info("Multimodal generation with request: {}", request);
        return llmChat(withSystemPrompt(request, "你是因材智学系统的多模态学习助手。请结合用户提供的文本或图片说明生成学习内容；如果当前模型不支持图片，请基于文本信息尽量完成任务。"));
    }

    private Map<String, Object> withSystemPrompt(Map<String, Object> request, String systemPrompt) {
        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        Object rawMessages = request.get("messages");
        if (rawMessages instanceof List<?> list && !list.isEmpty()) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> raw) {
                    String role = stringValue(raw.get("role"), "user");
                    Map<String, Object> message = new java.util.LinkedHashMap<>();
                    message.put("role", role);
                    message.put("content", stringValue(raw.get("content"), ""));
                    String reasoning = stringValue(raw.get("reasoningContent"), stringValue(raw.get("reasoning_content"), ""));
                    if ("assistant".equalsIgnoreCase(role) && !reasoning.isEmpty()) {
                        message.put("reasoningContent", reasoning);
                    }
                    messages.add(message);
                }
            }
        } else {
            String prompt = String.valueOf(request.getOrDefault("prompt", request.getOrDefault("question", request.toString())));
            messages.add(Map.of("role", "user", "content", prompt));
        }
        Map<String, Object> chatRequest = new java.util.LinkedHashMap<>(request);
        chatRequest.put("messages", messages);
        return chatRequest;
    }

    private String stringValue(Object value, String defaultValue) {
        if (value == null || !org.springframework.util.StringUtils.hasText(String.valueOf(value))) {
            return defaultValue;
        }
        return String.valueOf(value).trim();
    }
}
