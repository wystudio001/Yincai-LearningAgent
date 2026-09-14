package xyz.wystudio.yincai.path.interfaces.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.path.application.service.PathApplicationService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PathController {

    private final PathApplicationService pathApplicationService;

    // ==================== Student-facing endpoints ====================

    /**
     * Get current learning path for the student
     */
    @GetMapping("/api/v1/student/learning-path/current")
    public R<Map<String, Object>> getCurrentLearningPath(@RequestParam Long studentId) {
        return R.ok(pathApplicationService.getCurrentLearningPath(studentId));
    }

    /**
     * Generate a learning path
     */
    @PostMapping("/api/v1/student/learning-path/generate")
    public R<Map<String, Object>> generateLearningPath(@Valid @RequestBody Map<String, Object> request) {
        Long studentId = Long.valueOf(request.get("studentId").toString());
        Long courseId = Long.valueOf(request.get("courseId").toString());
        return R.ok(pathApplicationService.generateLearningPath(studentId, courseId));
    }

    // ==================== Learning path endpoints ====================

    /**
     * Get learning path details
     */
    @GetMapping("/api/v1/learning-paths/{pathId}")
    public R<Map<String, Object>> getLearningPathDetails(@PathVariable Long pathId) {
        return R.ok(pathApplicationService.getLearningPathDetails(pathId));
    }

    /**
     * Get nodes of a learning path
     */
    @GetMapping("/api/v1/learning-paths/{pathId}/nodes")
    public R<List<Map<String, Object>>> getPathNodes(@PathVariable Long pathId) {
        return R.ok(pathApplicationService.getPathNodes(pathId));
    }

    // ==================== Node endpoints ====================

    /**
     * Update node status
     */
    @PatchMapping("/api/v1/learning-path-nodes/{nodeId}/status")
    public R<Map<String, Object>> updateNodeStatus(@PathVariable Long nodeId, @Valid @RequestBody Map<String, String> request) {
        return R.ok(pathApplicationService.updateNodeStatus(nodeId, request.get("status")));
    }

    /**
     * Mark node as complete
     */
    @PostMapping("/api/v1/learning-path-nodes/{nodeId}/complete")
    public R<Map<String, Object>> completeNode(@PathVariable Long nodeId) {
        return R.ok(pathApplicationService.completeNode(nodeId));
    }

    /**
     * Skip a node
     */
    @PostMapping("/api/v1/learning-path-nodes/{nodeId}/skip")
    public R<Map<String, Object>> skipNode(@PathVariable Long nodeId) {
        return R.ok(pathApplicationService.skipNode(nodeId));
    }

    // ==================== Recommendation & intervention endpoints ====================

    /**
     * Get recommendations for the student's learning path
     */
    @GetMapping("/api/v1/student/learning-path/recommendations")
    public R<List<Map<String, Object>>> getRecommendations(@RequestParam Long studentId) {
        return R.ok(pathApplicationService.getRecommendations(studentId));
    }

    /**
     * Post an intervention
     */
    @PostMapping("/api/v1/student/learning-path/interventions")
    public R<Map<String, Object>> postIntervention(@Valid @RequestBody Map<String, Object> request) {
        Long studentId = Long.valueOf(request.get("studentId").toString());
        String action = request.get("action").toString();
        String reason = request.get("reason").toString();
        return R.ok(pathApplicationService.postIntervention(studentId, action, reason));
    }

    // ==================== Admin endpoints ====================

    /**
     * Admin teaching view
     */
    @GetMapping("/api/v1/admin/teaching-view")
    public R<Map<String, Object>> getTeachingView(@RequestParam Long instructorId) {
        return R.ok(pathApplicationService.getTeachingView(instructorId));
    }

    // ==================== Internal endpoints ====================

    /**
     * Internal: recalculate learning paths
     * This endpoint is for internal service-to-service communication only.
     */
    @PostMapping("/internal/v1/learning-paths/recalculate")
    public R<Map<String, Object>> recalculatePaths(@Valid @RequestBody Map<String, Object> request) {
        List<Long> pathIds = toLongList(request.get("pathIds"));
        return R.ok(pathApplicationService.recalculatePaths(pathIds));
    }

    private List<Long> toLongList(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream()
                .map(item -> item instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(item)))
                .toList();
    }
}
