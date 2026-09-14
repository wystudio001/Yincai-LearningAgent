package xyz.wystudio.yincai.student;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.wystudio.yincai.ai.application.service.AiApplicationService;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.security.UserContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** 学生端辅导适配层。上游模型调用始终留在 Java 服务端。 */
@RestController
@RequestMapping("/api/v1/tutor")
public class TutorController {
    private final AiApplicationService aiApplicationService;
    private final Map<String, TutorSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> feedback = new ConcurrentHashMap<>();

    public TutorController(AiApplicationService aiApplicationService) {
        this.aiApplicationService = aiApplicationService;
    }

    @PostMapping("/sessions")
    public R<Map<String, Object>> createSession(@RequestBody(required = false) Map<String, Object> body) {
        String id = UUID.randomUUID().toString();
        String title = text(body, "title", "学习辅导");
        TutorSession session = new TutorSession(id, currentUserId(), title);
        sessions.put(id, session);
        return R.ok(session.toMap());
    }

    @GetMapping("/sessions")
    public R<List<Map<String, Object>>> listSessions() {
        Long userId = currentUserId();
        List<Map<String, Object>> result = new ArrayList<>();
        sessions.values().stream()
                .filter(item -> userId == null || userId.equals(item.userId))
                .sorted((left, right) -> right.updatedAt.compareTo(left.updatedAt))
                .limit(50)
                .forEach(item -> result.add(item.toMap()));
        return R.ok(result);
    }

    @PostMapping(value = "/sessions/{sessionId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<Map<String, Object>> sendMessage(@PathVariable String sessionId,
                                               @RequestBody Map<String, Object> body) {
        TutorSession session = sessions.get(sessionId);
        if (session == null || !owns(session)) {
            return R.fail(ErrorCode.NOT_FOUND, "辅导会话不存在");
        }
        String content = text(body, "content", text(body, "message", ""));
        if (content.isEmpty()) {
            return R.fail(ErrorCode.BAD_REQUEST, "问题内容不能为空");
        }
        session.messages.add(Map.of("role", "user", "content", content));
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("messages", new ArrayList<>(session.messages));
        Map<String, Object> answer = aiApplicationService.tutorAnswer(request);
        String reply = text(answer, "content", text(answer, "answer", "暂时没有生成回答"));
        session.messages.add(Map.of("role", "assistant", "content", reply));
        session.updatedAt = LocalDateTime.now();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", UUID.randomUUID().toString());
        result.put("sessionId", session.id);
        result.put("role", "assistant");
        result.put("content", reply);
        result.put("answer", reply);
        return R.ok(result);
    }

    /** Compatibility mapping for clients that request SSE on the original messages path. */
    @PostMapping(value = "/sessions/{sessionId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStreamCompat(@PathVariable String sessionId,
                                               @RequestBody Map<String, Object> body) {
        return sendMessageStream(sessionId, body);
    }

    /** Forward upstream model deltas to the student client as SSE events. */
    @PostMapping(value = "/sessions/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(@PathVariable String sessionId,
                                        @RequestBody Map<String, Object> body) {
        TutorSession session = sessions.get(sessionId);
        if (session == null || !owns(session)) {
            throw new xyz.wystudio.yincai.core.BizException(ErrorCode.NOT_FOUND, "辅导会话不存在");
        }
        String content = text(body, "content", text(body, "message", ""));
        if (content.isEmpty()) {
            throw new xyz.wystudio.yincai.core.BizException(ErrorCode.BAD_REQUEST, "问题内容不能为空");
        }

        String assistantId = UUID.randomUUID().toString();
        session.messages.add(Map.of("id", UUID.randomUUID().toString(), "role", "user", "content", content));
        session.updatedAt = LocalDateTime.now();

        SseEmitter emitter = new SseEmitter(0L);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean assistantSaved = new AtomicBoolean(false);
        emitter.onCompletion(() -> cancelled.set(true));
        emitter.onTimeout(() -> {
            cancelled.set(true);
            emitter.complete();
        });
        emitter.onError(error -> cancelled.set(true));

        try {
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data(Map.of("sessionId", session.id, "messageId", assistantId)));
        } catch (Exception e) {
            cancelled.set(true);
            emitter.completeWithError(e);
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            StringBuilder reply = new StringBuilder();
            try {
                Map<String, Object> request = new LinkedHashMap<>();
                request.put("messages", session.snapshotMessages());
                aiApplicationService.tutorAnswerStream(request, delta -> {
                    if (cancelled.get() || delta == null || delta.isEmpty()) return;
                    reply.append(delta);
                    try {
                        emitter.send(SseEmitter.event().name("delta").data(Map.of("delta", delta)));
                    } catch (Exception sendError) {
                        cancelled.set(true);
                    }
                }, cancelled::get);

                if (!cancelled.get()) {
                    if (reply.length() == 0) {
                        throw new xyz.wystudio.yincai.core.BizException(
                                ErrorCode.AI_SERVICE_UNAVAILABLE,
                                "大模型返回为空，请检查模型配置或稍后重试");
                    }
                    String finalReply = reply.toString();
                    saveAssistant(session, assistantId, finalReply);
                    assistantSaved.set(true);
                    emitter.send(SseEmitter.event().name("done").data(Map.of(
                            "messageId", assistantId,
                            "content", finalReply,
                            "answer", finalReply)));
                }
            } catch (Exception error) {
                if (!cancelled.get()) {
                    try {
                        emitter.send(SseEmitter.event().name("error").data(Map.of(
                                "message", error.getMessage() == null ? "大模型暂时无法回答" : error.getMessage())));
                    } catch (Exception sendError) {
                        cancelled.set(true);
                    }
                }
            } finally {
                if (reply.length() > 0 && assistantSaved.compareAndSet(false, true)) {
                    saveAssistant(session, assistantId, reply.toString());
                }
                if (!cancelled.get()) {
                    emitter.complete();
                }
            }
        });
        return emitter;
    }

    @GetMapping("/sessions/{sessionId}/stream")
    public R<Map<String, Object>> stream(@PathVariable String sessionId) {
        TutorSession session = sessions.get(sessionId);
        if (session == null || !owns(session)) return R.fail(ErrorCode.NOT_FOUND, "辅导会话不存在");
        return R.ok(Map.of("sessionId", sessionId, "status", "IDLE", "messages", session.snapshotMessages()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public R<List<Map<String, Object>>> messages(@PathVariable String sessionId) {
        TutorSession session = sessions.get(sessionId);
        if (session == null || !owns(session)) return R.fail(ErrorCode.NOT_FOUND, "辅导会话不存在");
        return R.ok(session.snapshotMessages());
    }

    @PostMapping("/messages/{messageId}/feedback")
    public R<Map<String, Object>> saveFeedback(@PathVariable String messageId, @RequestBody Map<String, Object> body) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("messageId", messageId);
        item.put("helpful", body != null && Boolean.TRUE.equals(body.get("helpful")));
        item.put("comment", text(body, "comment", ""));
        feedback.put(messageId, item);
        return R.ok(item);
    }

    @GetMapping("/recommended-questions")
    public R<List<String>> recommendedQuestions() {
        return R.ok(List.of("帮我解释一个难点", "根据我的学习路径安排复习", "如何改进这道练习题"));
    }

    private boolean owns(TutorSession session) {
        Long userId = currentUserId();
        return userId == null || session.userId == null || userId.equals(session.userId);
    }

    private Long currentUserId() {
        return UserContextHolder.getUserId();
    }

    private String text(Map<String, Object> body, String key, String fallback) {
        if (body == null || body.get(key) == null) return fallback;
        String value = String.valueOf(body.get(key)).trim();
        return value.isEmpty() ? fallback : value;
    }

    private void saveAssistant(TutorSession session, String messageId, String content) {
        session.messages.add(Map.of("id", messageId, "role", "assistant", "content", content));
        session.updatedAt = LocalDateTime.now();
    }

    private static final class TutorSession {
        private final String id;
        private final Long userId;
        private final String title;
        private final List<Map<String, Object>> messages = Collections.synchronizedList(new ArrayList<>());
        private volatile LocalDateTime updatedAt = LocalDateTime.now();

        private TutorSession(String id, Long userId, String title) {
            this.id = id;
            this.userId = userId;
            this.title = title;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", id);
            result.put("title", title);
            result.put("messageCount", messages.size());
            result.put("updatedAt", updatedAt);
            return result;
        }

        private List<Map<String, Object>> snapshotMessages() {
            synchronized (messages) {
                return new ArrayList<>(messages);
            }
        }
    }
}
