package xyz.wystudio.yincai.path.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.path.infrastructure.persistence.entity.LearningPathEntity;
import xyz.wystudio.yincai.path.infrastructure.persistence.entity.PathInterventionEntity;
import xyz.wystudio.yincai.path.infrastructure.persistence.entity.PathNodeEntity;
import xyz.wystudio.yincai.path.infrastructure.persistence.entity.PathRecommendationEntity;
import xyz.wystudio.yincai.path.infrastructure.persistence.mapper.LearningPathMapper;
import xyz.wystudio.yincai.path.infrastructure.persistence.mapper.PathInterventionMapper;
import xyz.wystudio.yincai.path.infrastructure.persistence.mapper.PathNodeMapper;
import xyz.wystudio.yincai.path.infrastructure.persistence.mapper.PathRecommendationMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathApplicationService {

    private final LearningPathMapper learningPathMapper;
    private final PathNodeMapper pathNodeMapper;
    private final PathRecommendationMapper pathRecommendationMapper;
    private final PathInterventionMapper pathInterventionMapper;

    /**
     * Get current learning path for the student
     */
    public Map<String, Object> getCurrentLearningPath(Long studentId) {
        LearningPathEntity path = findCurrentPath(studentId);
        if (path == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "当前学习路径不存在");
        }
        return toPathDetails(path);
    }

    /**
     * Generate a learning path for the student
     */
    public Map<String, Object> generateLearningPath(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "studentId和courseId不能为空");
        }
        LearningPathEntity existing = findPathByStudentAndCourse(studentId, courseId);
        if (existing != null) {
            return toPathDetails(existing);
        }

        LearningPathEntity path = new LearningPathEntity();
        path.setStudentId(studentId);
        path.setCourseId(courseId);
        path.setStatus("ACTIVE");
        path.setVersion(1);
        path.setGeneratedBy("SYSTEM");
        learningPathMapper.insert(path);

        PathNodeEntity firstNode = createPathNode(path.getId(), 1, "课程基础概念梳理", "IN_PROGRESS");
        createPathNode(path.getId(), 2, "核心知识点学习", "PENDING");
        createPathNode(path.getId(), 3, "阶段练习与复盘", "PENDING");

        path.setCurrentNodeId(firstNode.getId());
        learningPathMapper.updateById(path);
        return toPathDetails(path);
    }

    /**
     * Get learning path details by pathId
     */
    public Map<String, Object> getLearningPathDetails(Long pathId) {
        return toPathDetails(requirePath(pathId));
    }

    /**
     * Get nodes of a learning path
     */
    public List<Map<String, Object>> getPathNodes(Long pathId) {
        requirePath(pathId);
        return listNodeEntities(pathId).stream().map(this::toNodeMap).toList();
    }

    /**
     * Update node status
     */
    public Map<String, Object> updateNodeStatus(Long nodeId, String status) {
        if (!StringUtils.hasText(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "status不能为空");
        }
        PathNodeEntity node = requireNode(nodeId);
        node.setStatus(status);
        if ("IN_PROGRESS".equals(status) && node.getStartedAt() == null) {
            node.setStartedAt(LocalDateTime.now());
        }
        if ("COMPLETED".equals(status)) {
            node.setCompletedAt(LocalDateTime.now());
        }
        pathNodeMapper.updateById(node);

        LearningPathEntity path = requirePath(node.getPathId());
        path.setCurrentNodeId(node.getId());
        if (listNodeEntities(path.getId()).stream().allMatch(item -> "COMPLETED".equals(item.getStatus()))) {
            path.setStatus("COMPLETED");
        }
        learningPathMapper.updateById(path);
        return toNodeMap(node);
    }

    /**
     * Mark node as complete
     */
    public Map<String, Object> completeNode(Long nodeId) {
        return updateNodeStatus(nodeId, "COMPLETED");
    }

    /**
     * Skip a node
     */
    public Map<String, Object> skipNode(Long nodeId) {
        return updateNodeStatus(nodeId, "SKIPPED");
    }

    /**
     * Get recommendations for the student's learning path
     */
    public List<Map<String, Object>> getRecommendations(Long studentId) {
        LearningPathEntity path = findCurrentPath(studentId);
        if (path == null) {
            return List.of();
        }
        List<Long> nodeIds = listNodeEntities(path.getId()).stream().map(PathNodeEntity::getId).toList();
        if (nodeIds.isEmpty()) {
            return List.of();
        }
        return pathRecommendationMapper.selectList(new LambdaQueryWrapper<PathRecommendationEntity>()
                        .in(PathRecommendationEntity::getPathNodeId, nodeIds)
                        .orderByAsc(PathRecommendationEntity::getPriority))
                .stream()
                .map(this::toRecommendationMap)
                .toList();
    }

    /**
     * Post an intervention
     */
    public Map<String, Object> postIntervention(Long studentId, String action, String reason) {
        LearningPathEntity path = findCurrentPath(studentId);
        if (path == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "当前学习路径不存在");
        }
        PathInterventionEntity entity = new PathInterventionEntity();
        entity.setStudentId(studentId);
        entity.setPathId(path.getId());
        entity.setInstructorId(0L);
        entity.setAction(action);
        entity.setReason(reason);
        entity.setResult("CREATED");
        pathInterventionMapper.insert(entity);
        return toInterventionMap(entity);
    }

    /**
     * Get admin teaching view data
     */
    public Map<String, Object> getTeachingView(Long instructorId) {
        List<LearningPathEntity> paths = learningPathMapper.selectList(new LambdaQueryWrapper<LearningPathEntity>()
                .orderByDesc(LearningPathEntity::getUpdatedAt));
        long activeCount = paths.stream().filter(item -> "ACTIVE".equals(item.getStatus())).count();
        long completedCount = paths.stream().filter(item -> "COMPLETED".equals(item.getStatus())).count();
        return Map.of("instructorId", instructorId, "pathCount", paths.size(), "activeCount", activeCount,
                "completedCount", completedCount, "recentPaths", paths.stream().limit(10).map(this::toPathMap).toList());
    }

    /**
     * Recalculate learning paths (internal)
     */
    public Map<String, Object> recalculatePaths(List<Long> pathIds) {
        if (pathIds == null || pathIds.isEmpty()) {
            return Map.of("pathIds", List.of(), "updated", 0);
        }
        int updated = 0;
        for (Long pathId : pathIds) {
            LearningPathEntity path = learningPathMapper.selectById(pathId);
            if (path != null) {
                path.setVersion(path.getVersion() == null ? 1 : path.getVersion() + 1);
                learningPathMapper.updateById(path);
                updated++;
            }
        }
        return Map.of("pathIds", pathIds, "updated", updated);
    }

    private LearningPathEntity findCurrentPath(Long studentId) {
        if (studentId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "studentId不能为空");
        }
        return learningPathMapper.selectList(new LambdaQueryWrapper<LearningPathEntity>()
                        .eq(LearningPathEntity::getStudentId, studentId)
                        .orderByDesc(LearningPathEntity::getUpdatedAt))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private LearningPathEntity findPathByStudentAndCourse(Long studentId, Long courseId) {
        return learningPathMapper.selectOne(new LambdaQueryWrapper<LearningPathEntity>()
                .eq(LearningPathEntity::getStudentId, studentId)
                .eq(LearningPathEntity::getCourseId, courseId));
    }

    private LearningPathEntity requirePath(Long pathId) {
        if (pathId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "pathId不能为空");
        }
        LearningPathEntity path = learningPathMapper.selectById(pathId);
        if (path == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学习路径不存在");
        }
        return path;
    }

    private PathNodeEntity requireNode(Long nodeId) {
        if (nodeId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "nodeId不能为空");
        }
        PathNodeEntity node = pathNodeMapper.selectById(nodeId);
        if (node == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学习路径节点不存在");
        }
        return node;
    }

    private List<PathNodeEntity> listNodeEntities(Long pathId) {
        return pathNodeMapper.selectList(new LambdaQueryWrapper<PathNodeEntity>()
                .eq(PathNodeEntity::getPathId, pathId)
                .orderByAsc(PathNodeEntity::getNodeOrder));
    }

    private PathNodeEntity createPathNode(Long pathId, int order, String target, String status) {
        PathNodeEntity node = new PathNodeEntity();
        node.setPathId(pathId);
        node.setNodeOrder(order);
        node.setTarget(target);
        node.setStatus(status);
        node.setCompletionRule("完成节点学习资源并通过阶段练习");
        if ("IN_PROGRESS".equals(status)) {
            node.setStartedAt(LocalDateTime.now());
        }
        pathNodeMapper.insert(node);
        return node;
    }

    private Map<String, Object> toPathDetails(LearningPathEntity path) {
        Map<String, Object> result = toPathMap(path);
        result.put("nodes", getPathNodes(path.getId()));
        return result;
    }

    private Map<String, Object> toPathMap(LearningPathEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("studentId", entity.getStudentId());
        map.put("courseId", entity.getCourseId());
        map.put("status", entity.getStatus());
        map.put("version", entity.getVersion());
        map.put("generatedBy", entity.getGeneratedBy());
        map.put("currentNodeId", entity.getCurrentNodeId());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toNodeMap(PathNodeEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("pathId", entity.getPathId());
        map.put("knowledgePointId", entity.getKnowledgePointId());
        map.put("nodeOrder", entity.getNodeOrder());
        map.put("target", entity.getTarget());
        map.put("status", entity.getStatus());
        map.put("completionRule", entity.getCompletionRule());
        map.put("startedAt", entity.getStartedAt());
        map.put("completedAt", entity.getCompletedAt());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toRecommendationMap(PathRecommendationEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("pathNodeId", entity.getPathNodeId());
        map.put("resourceId", entity.getResourceId());
        map.put("resourceType", entity.getResourceType());
        map.put("priority", entity.getPriority());
        map.put("reason", entity.getReason());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private Map<String, Object> toInterventionMap(PathInterventionEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("studentId", entity.getStudentId());
        map.put("pathId", entity.getPathId());
        map.put("instructorId", entity.getInstructorId());
        map.put("action", entity.getAction());
        map.put("reason", entity.getReason());
        map.put("result", entity.getResult());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }
}
