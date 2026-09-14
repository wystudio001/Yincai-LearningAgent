package xyz.wystudio.yincai.ai.interfaces.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import xyz.wystudio.yincai.ai.application.service.AiApplicationService;
import xyz.wystudio.yincai.core.R;

@RestController
@RequestMapping("/internal/v1/ai")
@RequiredArgsConstructor
public class AiInternalController {

    private final AiApplicationService aiApplicationService;

    @PostMapping("/agent-tasks")
    public R<Map<String, Object>> createAgentTask(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.createAgentTask(request));
    }

    @GetMapping("/{taskId}")
    public R<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        return R.ok(aiApplicationService.getTaskStatus(taskId));
    }

    @PostMapping("/{taskId}/cancel")
    public R<Map<String, Object>> cancelTask(@PathVariable String taskId) {
        return R.ok(aiApplicationService.cancelTask(taskId));
    }

    @GetMapping("/{taskId}/stream")
    public R<String> streamTaskProgress(@PathVariable String taskId) {
        return R.ok(aiApplicationService.streamTaskProgress(taskId));
    }

    @PostMapping("/profile/extract")
    public R<Map<String, Object>> extractProfile(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.extractProfile(request));
    }

    @PostMapping("/resources/generate")
    public R<Map<String, Object>> generateResources(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.generateResources(request));
    }

    @PostMapping("/learning-path/plan")
    public R<Map<String, Object>> planLearningPath(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.planLearningPath(request));
    }

    @PostMapping(value = "/tutor/answer", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<Map<String, Object>> tutorAnswer(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.tutorAnswer(request));
    }

    /** Compatibility mapping for internal clients that request SSE on the original path. */
    @PostMapping(value = "/tutor/answer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter tutorAnswerStreamCompat(@RequestBody Map<String, Object> request) {
        return stream((onDelta, cancelled) -> aiApplicationService.tutorAnswerStream(request, onDelta, cancelled));
    }

    @PostMapping(value = "/tutor/answer/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter tutorAnswerStream(@RequestBody Map<String, Object> request) {
        return stream((onDelta, cancelled) -> aiApplicationService.tutorAnswerStream(request, onDelta, cancelled));
    }

    @PostMapping("/assessment/analyze")
    public R<Map<String, Object>> analyzeAssessment(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.analyzeAssessment(request));
    }

    @PostMapping("/rag/search")
    public R<Map<String, Object>> ragSearch(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.ragSearch(request));
    }

    @PostMapping("/embeddings")
    public R<Map<String, Object>> generateEmbeddings(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.generateEmbeddings(request));
    }

    @PostMapping("/safety/review")
    public R<Map<String, Object>> safetyReview(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.safetyReview(request));
    }

    @PostMapping("/llm/chat")
    public R<Map<String, Object>> llmChat(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.llmChat(request));
    }

    @PostMapping(value = "/llm/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter llmChatStream(@RequestBody Map<String, Object> request) {
        return stream((onDelta, cancelled) -> aiApplicationService.llmChatStream(request, onDelta, cancelled));
    }

    /** Keep the old full-text response for legacy callers. */
    @PostMapping("/llm/chat-stream/text")
    public R<String> llmChatStreamText(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.llmChatStream(request));
    }

    @PostMapping("/multimodal/generate")
    public R<Map<String, Object>> multimodalGenerate(@RequestBody Map<String, Object> request) {
        return R.ok(aiApplicationService.multimodalGenerate(request));
    }

    private SseEmitter stream(StreamRunner runner) {
        SseEmitter emitter = new SseEmitter(0L);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        emitter.onCompletion(() -> cancelled.set(true));
        emitter.onTimeout(() -> {
            cancelled.set(true);
            emitter.complete();
        });
        emitter.onError(error -> cancelled.set(true));

        String streamId = UUID.randomUUID().toString();
        try {
            emitter.send(SseEmitter.event().name("start").data(Map.of("streamId", streamId)));
        } catch (Exception error) {
            cancelled.set(true);
            emitter.completeWithError(error);
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            StringBuilder content = new StringBuilder();
            try {
                runner.run(delta -> {
                    if (cancelled.get() || delta == null || delta.isEmpty()) return;
                    content.append(delta);
                    try {
                        emitter.send(SseEmitter.event().name("delta").data(Map.of("delta", delta)));
                    } catch (Exception error) {
                        cancelled.set(true);
                    }
                }, cancelled::get);
                if (!cancelled.get()) {
                    String result = content.toString();
                    emitter.send(SseEmitter.event().name("done").data(Map.of("content", result)));
                }
            } catch (Exception error) {
                if (!cancelled.get()) {
                    try {
                        Map<String, Object> payload = new LinkedHashMap<>();
                        payload.put("message", error.getMessage() == null ? "大模型暂时无法回答" : error.getMessage());
                        emitter.send(SseEmitter.event().name("error").data(payload));
                    } catch (Exception sendError) {
                        cancelled.set(true);
                    }
                }
            } finally {
                if (!cancelled.get()) emitter.complete();
            }
        });
        return emitter;
    }

    @FunctionalInterface
    private interface StreamRunner {
        void run(Consumer<String> onDelta, BooleanSupplier cancelled);
    }
}
