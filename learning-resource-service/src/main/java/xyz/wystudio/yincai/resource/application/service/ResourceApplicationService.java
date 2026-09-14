package xyz.wystudio.yincai.resource.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.resource.infrastructure.persistence.entity.*;
import xyz.wystudio.yincai.resource.infrastructure.persistence.mapper.*;
import xyz.wystudio.yincai.security.UserContextHolder;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceApplicationService {

    private final ResourceMapper resourceMapper;
    private final ResourcePackageMapper resourcePackageMapper;
    private final ResourceFileMapper resourceFileMapper;
    private final ResourceFavoriteMapper resourceFavoriteMapper;
    private final ResourceFeedbackMapper resourceFeedbackMapper;
    private final GenerationTaskMapper generationTaskMapper;
    private final GenerationStepMapper generationStepMapper;
    private final FileMapper fileMapper;

    // Resource CRUD

    public Object pageResources(Integer page, Integer size, Long courseId, Long knowledgePointId,
                                String resourceType, String difficulty, String status, String keyword) {
        LambdaQueryWrapper<ResourceEntity> wrapper = resourceQuery(courseId, knowledgePointId, resourceType, difficulty, status, keyword)
                .orderByDesc(ResourceEntity::getCreatedAt);
        Page<ResourceEntity> result = resourceMapper.selectPage(new Page<>(page, size), wrapper);
        return pageData(result, result.getRecords().stream().map(this::toResourceMap).toList());
    }

    public Object createResource(Object request) {
        Map<String, Object> body = toBody(request);
        ResourceEntity entity = new ResourceEntity();
        entity.setTenantId(longValue(body.get("tenantId"), 0L));
        entity.setResourceType(requiredString(body, "resourceType"));
        entity.setTitle(requiredString(body, "title"));
        entity.setSummary(stringValue(body.get("summary")));
        entity.setCourseId(longValue(body.get("courseId"), null));
        entity.setKnowledgePointId(longValue(body.get("knowledgePointId"), null));
        entity.setDifficulty(stringValue(body.getOrDefault("difficulty", "MEDIUM")));
        entity.setStatus(stringValue(body.getOrDefault("status", "DRAFT")));
        entity.setQualityScore(intValue(body.get("qualityScore"), 0));
        entity.setSourceTaskId(longValue(body.get("sourceTaskId"), null));
        resourceMapper.insert(entity);
        return toResourceMap(entity);
    }

    public Object getResourceById(String id) {
        ResourceEntity entity = requireResource(id);
        Map<String, Object> result = toResourceMap(entity);
        result.put("files", resourceFileMapper.selectList(new LambdaQueryWrapper<ResourceFileEntity>()
                .eq(ResourceFileEntity::getResourceId, entity.getId())).stream().map(this::toResourceFileMap).toList());
        return result;
    }

    public Object updateResource(String id, Object request) {
        ResourceEntity entity = requireResource(id);
        Map<String, Object> body = toBody(request);
        setIfPresent(body, "resourceType", entity::setResourceType);
        setIfPresent(body, "title", entity::setTitle);
        setIfPresent(body, "summary", entity::setSummary);
        setIfPresent(body, "difficulty", entity::setDifficulty);
        setIfPresent(body, "status", entity::setStatus);
        if (body.containsKey("courseId")) entity.setCourseId(longValue(body.get("courseId"), entity.getCourseId()));
        if (body.containsKey("knowledgePointId")) entity.setKnowledgePointId(longValue(body.get("knowledgePointId"), entity.getKnowledgePointId()));
        if (body.containsKey("qualityScore")) entity.setQualityScore(intValue(body.get("qualityScore"), entity.getQualityScore()));
        resourceMapper.updateById(entity);
        return toResourceMap(entity);
    }

    public Object deleteResource(String id) {
        ResourceEntity entity = requireResource(id);
        resourceMapper.deleteById(entity.getId());
        return Map.of("id", entity.getId(), "deleted", true);
    }

    public Object getResourceDownloadUrl(String id) {
        ResourceEntity entity = requireResource(id);
        ResourceFileEntity file = resourceFileMapper.selectList(new LambdaQueryWrapper<ResourceFileEntity>()
                        .eq(ResourceFileEntity::getResourceId, entity.getId())
                        .orderByDesc(ResourceFileEntity::getCreatedAt))
                .stream().findFirst().orElse(null);
        String downloadUrl = file != null && StringUtils.hasText(file.getDownloadUrl())
                ? file.getDownloadUrl()
                : "/api/v1/resources/" + entity.getId() + "/download";
        return Map.of("resourceId", entity.getId(), "downloadUrl", downloadUrl);
    }

    // Favorites

    public Object addFavorite(String resourceId) {
        Long userId = currentUserId();
        ResourceEntity resource = requireResource(resourceId);
        Long count = resourceFavoriteMapper.selectCount(new LambdaQueryWrapper<ResourceFavoriteEntity>()
                .eq(ResourceFavoriteEntity::getUserId, userId)
                .eq(ResourceFavoriteEntity::getResourceId, resource.getId()));
        if (count == 0) {
            ResourceFavoriteEntity entity = new ResourceFavoriteEntity();
            entity.setUserId(userId);
            entity.setResourceId(resource.getId());
            resourceFavoriteMapper.insert(entity);
        }
        return Map.of("resourceId", resource.getId(), "favorited", true);
    }

    public Object removeFavorite(String resourceId) {
        Long userId = currentUserId();
        ResourceEntity resource = requireResource(resourceId);
        resourceFavoriteMapper.delete(new LambdaQueryWrapper<ResourceFavoriteEntity>()
                .eq(ResourceFavoriteEntity::getUserId, userId)
                .eq(ResourceFavoriteEntity::getResourceId, resource.getId()));
        return Map.of("resourceId", resource.getId(), "favorited", false);
    }

    public Object submitFeedback(String resourceId, Object request) {
        Long userId = currentUserId();
        ResourceEntity resource = requireResource(resourceId);
        Map<String, Object> body = toBody(request);
        ResourceFeedbackEntity entity = new ResourceFeedbackEntity();
        entity.setUserId(userId);
        entity.setResourceId(resource.getId());
        entity.setRating(intValue(body.get("rating"), 0));
        entity.setComment(stringValue(body.get("comment")));
        resourceFeedbackMapper.insert(entity);
        return toFeedbackMap(entity);
    }

    public Object getUserFavorites(Integer page, Integer size) {
        Long userId = currentUserId();
        Page<ResourceFavoriteEntity> result = resourceFavoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ResourceFavoriteEntity>()
                        .eq(ResourceFavoriteEntity::getUserId, userId)
                        .orderByDesc(ResourceFavoriteEntity::getCreatedAt));
        List<Map<String, Object>> records = result.getRecords().stream()
                .map(item -> resourceMapper.selectById(item.getResourceId()))
                .filter(item -> item != null)
                .map(this::toResourceMap)
                .toList();
        return pageData(result, records);
    }

    public Object getRecentResources(Integer limit) {
        return resourceMapper.selectList(new LambdaQueryWrapper<ResourceEntity>()
                        .eq(ResourceEntity::getStatus, "PUBLISHED")
                        .orderByDesc(ResourceEntity::getCreatedAt)
                        .last("LIMIT " + Math.max(1, limit)))
                .stream().map(this::toResourceMap).toList();
    }

    public Object searchResources(String keyword, Integer page, Integer size, String resourceType, String difficulty) {
        Page<ResourceEntity> result = resourceMapper.selectPage(new Page<>(page, size),
                resourceQuery(null, null, resourceType, difficulty, "PUBLISHED", keyword)
                        .orderByDesc(ResourceEntity::getQualityScore)
                        .orderByDesc(ResourceEntity::getCreatedAt));
        return pageData(result, result.getRecords().stream().map(this::toResourceMap).toList());
    }

    public Object semanticSearch(Object request) {
        Map<String, Object> body = toBody(request);
        String keyword = stringValue(body.get("keyword"));
        if (!StringUtils.hasText(keyword)) {
            keyword = stringValue(body.get("query"));
        }
        return searchResources(keyword == null ? "" : keyword, 1, 20, null, null);
    }

    // Resource Packages

    public Object listPackages(Integer page, Integer size, Long courseId) {
        Page<ResourcePackageEntity> result = resourcePackageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ResourcePackageEntity>()
                        .eq(courseId != null, ResourcePackageEntity::getCourseId, courseId)
                        .orderByDesc(ResourcePackageEntity::getCreatedAt));
        return pageData(result, result.getRecords().stream().map(this::toPackageMap).toList());
    }

    public Object createPackage(Object request) {
        Map<String, Object> body = toBody(request);
        ResourcePackageEntity entity = new ResourcePackageEntity();
        entity.setTenantId(longValue(body.get("tenantId"), 0L));
        entity.setName(requiredString(body, "name"));
        entity.setDescription(stringValue(body.get("description")));
        entity.setCourseId(longValue(body.get("courseId"), null));
        entity.setStudentId(longValue(body.get("studentId"), null));
        entity.setStatus(stringValue(body.getOrDefault("status", "DRAFT")));
        resourcePackageMapper.insert(entity);
        return toPackageMap(entity);
    }

    public Object getPackageById(String id) {
        ResourcePackageEntity entity = requirePackage(id);
        return toPackageMap(entity);
    }

    public Object publishPackage(String id) {
        ResourcePackageEntity entity = requirePackage(id);
        entity.setStatus("PUBLISHED");
        resourcePackageMapper.updateById(entity);
        return toPackageMap(entity);
    }

    // Generation Tasks

    public Object createGenerationTask(Object request) {
        Map<String, Object> body = toBody(request);
        GenerationTaskEntity entity = new GenerationTaskEntity();
        entity.setTenantId(longValue(body.get("tenantId"), 0L));
        entity.setRequesterId(currentUserIdOrDefault(longValue(body.get("requesterId"), 0L)));
        entity.setCourseId(longValue(body.get("courseId"), null));
        entity.setKnowledgePointId(longValue(body.get("knowledgePointId"), null));
        entity.setTaskType(requiredString(body, "taskType"));
        entity.setStatus("CREATED");
        entity.setProgress(0);
        generationTaskMapper.insert(entity);
        return toGenerationTaskMap(entity);
    }

    public Object listGenerationTasks(Integer page, Integer size, String status, String taskType) {
        Page<GenerationTaskEntity> result = generationTaskMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<GenerationTaskEntity>()
                        .eq(StringUtils.hasText(status), GenerationTaskEntity::getStatus, status)
                        .eq(StringUtils.hasText(taskType), GenerationTaskEntity::getTaskType, taskType)
                        .orderByDesc(GenerationTaskEntity::getCreatedAt));
        return pageData(result, result.getRecords().stream().map(this::toGenerationTaskMap).toList());
    }

    public Object getGenerationTaskById(String id) {
        return toGenerationTaskMap(requireTask(id));
    }

    public Object getGenerationTaskSteps(String id) {
        GenerationTaskEntity task = requireTask(id);
        return generationStepMapper.selectList(new LambdaQueryWrapper<GenerationStepEntity>()
                        .eq(GenerationStepEntity::getTaskId, task.getId())
                        .orderByAsc(GenerationStepEntity::getCreatedAt))
                .stream().map(this::toGenerationStepMap).toList();
    }

    public Object streamGenerationTask(String id) {
        GenerationTaskEntity task = requireTask(id);
        return Map.of("id", task.getId(), "status", task.getStatus(), "progress", task.getProgress() == null ? 0 : task.getProgress());
    }

    public Object cancelGenerationTask(String id) {
        GenerationTaskEntity task = requireTask(id);
        task.setStatus("CANCELLED");
        task.setFinishedAt(LocalDateTime.now());
        generationTaskMapper.updateById(task);
        return toGenerationTaskMap(task);
    }

    public Object retryGenerationTask(String id) {
        GenerationTaskEntity task = requireTask(id);
        task.setStatus("CREATED");
        task.setProgress(0);
        task.setErrorMessage(null);
        task.setStartedAt(null);
        task.setFinishedAt(null);
        generationTaskMapper.updateById(task);
        return toGenerationTaskMap(task);
    }

    public Object getGenerationTaskOutputs(String id) {
        GenerationTaskEntity task = requireTask(id);
        return resourceMapper.selectList(new LambdaQueryWrapper<ResourceEntity>()
                        .eq(ResourceEntity::getSourceTaskId, task.getId()))
                .stream().map(this::toResourceMap).toList();
    }

    public Object publishGenerationTaskOutputs(String id) {
        GenerationTaskEntity task = requireTask(id);
        List<ResourceEntity> outputs = resourceMapper.selectList(new LambdaQueryWrapper<ResourceEntity>()
                .eq(ResourceEntity::getSourceTaskId, task.getId()));
        outputs.forEach(item -> {
            item.setStatus("PUBLISHED");
            resourceMapper.updateById(item);
        });
        return Map.of("taskId", task.getId(), "publishedCount", outputs.size());
    }

    // Templates

    public Object listTemplates(Integer page, Integer size, String taskType) {
        return pageData(new Page<>(page, size, 0), List.of());
    }

    public Object createTemplate(Object request) {
        throw new BizException(ErrorCode.BAD_REQUEST, "当前数据库尚未创建资源生成模板表，无法保存模板");
    }

    public Object updateTemplate(String id, Object request) {
        throw new BizException(ErrorCode.BAD_REQUEST, "当前数据库尚未创建资源生成模板表，无法更新模板");
    }

    public Object deleteTemplate(String id) {
        throw new BizException(ErrorCode.BAD_REQUEST, "当前数据库尚未创建资源生成模板表，无法删除模板");
    }

    // Files

    public Object getUploadToken() {
        return Map.of("mode", "LOCAL_METADATA", "uploadUrl", "/api/v1/files", "expiresIn", 3600);
    }

    public Object uploadFile(Object request) {
        Map<String, Object> body = toBody(request);
        FileEntity entity = new FileEntity();
        entity.setTenantId(longValue(body.get("tenantId"), 0L));
        entity.setOriginalName(requiredString(body, "originalName"));
        entity.setFileType(stringValue(body.get("fileType")));
        entity.setFileSize(longValue(body.get("fileSize"), 0L));
        entity.setStoragePath(requiredString(body, "storagePath"));
        entity.setStatus(stringValue(body.getOrDefault("status", "UPLOADED")));
        entity.setUploadedBy(currentUserIdOrDefault(longValue(body.get("uploadedBy"), 0L)));
        fileMapper.insert(entity);
        return toFileMap(entity);
    }

    public Object getFileInfo(String id) {
        return toFileMap(requireFile(id));
    }

    public Object getFileDownloadUrl(String id) {
        FileEntity file = requireFile(id);
        return Map.of("id", file.getId(), "downloadUrl", file.getStoragePath());
    }

    public Object deleteFile(String id) {
        FileEntity file = requireFile(id);
        fileMapper.deleteById(file.getId());
        return Map.of("id", file.getId(), "deleted", true);
    }

    public Object scanFile(String id) {
        FileEntity file = requireFile(id);
        return Map.of("id", file.getId(), "scanResult", "PENDING_MANUAL_REVIEW", "status", file.getStatus());
    }

    public Object getFilePreviewUrl(String id) {
        FileEntity file = requireFile(id);
        return Map.of("id", file.getId(), "previewUrl", file.getStoragePath());
    }

    private LambdaQueryWrapper<ResourceEntity> resourceQuery(Long courseId, Long knowledgePointId, String resourceType,
                                                             String difficulty, String status, String keyword) {
        LambdaQueryWrapper<ResourceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(courseId != null, ResourceEntity::getCourseId, courseId)
                .eq(knowledgePointId != null, ResourceEntity::getKnowledgePointId, knowledgePointId)
                .eq(StringUtils.hasText(resourceType), ResourceEntity::getResourceType, resourceType)
                .eq(StringUtils.hasText(difficulty), ResourceEntity::getDifficulty, difficulty)
                .eq(StringUtils.hasText(status), ResourceEntity::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(ResourceEntity::getTitle, keyword).or().like(ResourceEntity::getSummary, keyword));
        }
        return wrapper;
    }

    private ResourceEntity requireResource(String id) {
        ResourceEntity entity = resourceMapper.selectById(longValue(id, null));
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "资源不存在");
        return entity;
    }

    private ResourcePackageEntity requirePackage(String id) {
        ResourcePackageEntity entity = resourcePackageMapper.selectById(longValue(id, null));
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "资源包不存在");
        return entity;
    }

    private GenerationTaskEntity requireTask(String id) {
        GenerationTaskEntity entity = generationTaskMapper.selectById(longValue(id, null));
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "生成任务不存在");
        return entity;
    }

    private FileEntity requireFile(String id) {
        FileEntity entity = fileMapper.selectById(longValue(id, null));
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "文件不存在");
        return entity;
    }

    private Long currentUserId() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) throw new BizException(ErrorCode.UNAUTHORIZED, "请先登录");
        return userId;
    }

    private Long currentUserIdOrDefault(Long defaultValue) {
        Long userId = UserContextHolder.getUserId();
        return userId == null ? defaultValue : userId;
    }

    private Map<String, Object> toResourceMap(ResourceEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenantId", entity.getTenantId());
        map.put("resourceType", entity.getResourceType());
        map.put("title", entity.getTitle());
        map.put("summary", entity.getSummary());
        map.put("courseId", entity.getCourseId());
        map.put("knowledgePointId", entity.getKnowledgePointId());
        map.put("difficulty", entity.getDifficulty());
        map.put("status", entity.getStatus());
        map.put("qualityScore", entity.getQualityScore());
        map.put("sourceTaskId", entity.getSourceTaskId());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toResourceFileMap(ResourceFileEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("resourceId", entity.getResourceId());
        map.put("fileName", entity.getFileName());
        map.put("fileType", entity.getFileType());
        map.put("fileSize", entity.getFileSize());
        map.put("storagePath", entity.getStoragePath());
        map.put("downloadUrl", entity.getDownloadUrl());
        map.put("previewUrl", entity.getPreviewUrl());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private Map<String, Object> toFeedbackMap(ResourceFeedbackEntity entity) {
        return Map.of("id", entity.getId(), "userId", entity.getUserId(), "resourceId", entity.getResourceId(),
                "rating", entity.getRating(), "comment", entity.getComment() == null ? "" : entity.getComment());
    }

    private Map<String, Object> toPackageMap(ResourcePackageEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenantId", entity.getTenantId());
        map.put("name", entity.getName());
        map.put("description", entity.getDescription());
        map.put("courseId", entity.getCourseId());
        map.put("studentId", entity.getStudentId());
        map.put("status", entity.getStatus());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toGenerationTaskMap(GenerationTaskEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenantId", entity.getTenantId());
        map.put("requesterId", entity.getRequesterId());
        map.put("courseId", entity.getCourseId());
        map.put("knowledgePointId", entity.getKnowledgePointId());
        map.put("taskType", entity.getTaskType());
        map.put("status", entity.getStatus());
        map.put("progress", entity.getProgress());
        map.put("errorMessage", entity.getErrorMessage());
        map.put("startedAt", entity.getStartedAt());
        map.put("finishedAt", entity.getFinishedAt());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toGenerationStepMap(GenerationStepEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("taskId", entity.getTaskId());
        map.put("agentCode", entity.getAgentCode());
        map.put("stepType", entity.getStepType());
        map.put("status", entity.getStatus());
        map.put("inputSummary", entity.getInputSummary());
        map.put("outputResourceId", entity.getOutputResourceId());
        map.put("startedAt", entity.getStartedAt());
        map.put("finishedAt", entity.getFinishedAt());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private Map<String, Object> toFileMap(FileEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenantId", entity.getTenantId());
        map.put("originalName", entity.getOriginalName());
        map.put("fileType", entity.getFileType());
        map.put("fileSize", entity.getFileSize());
        map.put("storagePath", entity.getStoragePath());
        map.put("status", entity.getStatus());
        map.put("uploadedBy", entity.getUploadedBy());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private <T> Map<String, Object> pageData(Page<?> page, List<T> records) {
        return Map.of("records", records, "total", page.getTotal(), "page", page.getCurrent(),
                "size", page.getSize(), "hasNext", page.hasNext());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toBody(Object request) {
        if (request instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private String requiredString(Map<String, Object> body, String key) {
        String value = stringValue(body.get(key));
        if (!StringUtils.hasText(value)) throw new BizException(ErrorCode.BAD_REQUEST, key + "不能为空");
        return value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Long longValue(Object value, Long defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) return defaultValue;
        return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
    }

    private Integer intValue(Object value, Integer defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) return defaultValue;
        return value instanceof Number number ? number.intValue() : Integer.valueOf(String.valueOf(value));
    }

    private void setIfPresent(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) {
        if (body.containsKey(key)) setter.accept(stringValue(body.get(key)));
    }
}
