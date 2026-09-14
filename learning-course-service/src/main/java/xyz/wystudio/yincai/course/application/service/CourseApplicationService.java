package xyz.wystudio.yincai.course.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.course.infrastructure.persistence.entity.*;
import xyz.wystudio.yincai.course.infrastructure.persistence.mapper.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseApplicationService {

    private final CourseMapper courseMapper;
    private final ChapterMapper chapterMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final KnowledgeRelationMapper knowledgeRelationMapper;
    private final CourseMaterialMapper courseMaterialMapper;

    // ==================== Courses ====================

    public Map<String, Object> createCourse(Map<String, Object> body) {
        CourseEntity entity = new CourseEntity();
        entity.setTenantId(longValue(body.get("tenantId"), 0L));
        entity.setName(requiredString(body, "name"));
        entity.setDescription(stringValue(body.get("description")));
        entity.setCoverImage(stringValue(body.get("coverImage")));
        entity.setStatus(stringValue(body.getOrDefault("status", "DRAFT")));
        courseMapper.insert(entity);
        return toCourseMap(entity);
    }

    public R.PageData<Map<String, Object>> listCourses(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<CourseEntity> wrapper = new LambdaQueryWrapper<CourseEntity>()
                .like(StringUtils.hasText(keyword), CourseEntity::getName, keyword)
                .eq(StringUtils.hasText(status), CourseEntity::getStatus, status)
                .orderByDesc(CourseEntity::getCreatedAt);
        Page<CourseEntity> result = courseMapper.selectPage(new Page<>(page, size), wrapper);
        return pageData(result, result.getRecords().stream().map(this::toCourseMap).toList());
    }

    public Map<String, Object> getCourseById(Long id) {
        CourseEntity entity = requireCourse(id);
        Map<String, Object> result = toCourseMap(entity);
        result.put("chapters", listChapters(id));
        result.put("knowledgePoints", listKnowledgePoints(id));
        return result;
    }

    public void updateCourse(Long id, Map<String, Object> body) {
        CourseEntity entity = requireCourse(id);
        setIfPresent(body, "name", entity::setName);
        setIfPresent(body, "description", entity::setDescription);
        setIfPresent(body, "coverImage", entity::setCoverImage);
        setIfPresent(body, "status", entity::setStatus);
        courseMapper.updateById(entity);
    }

    public void publishCourse(Long id) {
        CourseEntity entity = requireCourse(id);
        entity.setStatus("PUBLISHED");
        courseMapper.updateById(entity);
    }

    public void unpublishCourse(Long id) {
        CourseEntity entity = requireCourse(id);
        entity.setStatus("DRAFT");
        courseMapper.updateById(entity);
    }

    public void deleteCourse(Long id) {
        requireCourse(id);
        courseMapper.deleteById(id);
    }

    // ==================== Chapters ====================

    public List<Map<String, Object>> listChapters(Long courseId) {
        return chapterMapper.selectByCourseId(courseId).stream().map(this::toChapterMap).toList();
    }

    public Map<String, Object> createChapter(Map<String, Object> body) {
        requireCourse(longValue(body.get("courseId"), null));
        ChapterEntity entity = new ChapterEntity();
        entity.setCourseId(longValue(body.get("courseId"), null));
        entity.setParentId(longValue(body.get("parentId"), 0L));
        entity.setName(requiredString(body, "name"));
        entity.setSortOrder(intValue(body.get("sortOrder"), 0));
        entity.setDescription(stringValue(body.get("description")));
        chapterMapper.insert(entity);
        return toChapterMap(entity);
    }

    public void updateChapter(Long id, Map<String, Object> body) {
        ChapterEntity entity = requireChapter(id);
        setIfPresent(body, "name", entity::setName);
        setIfPresent(body, "description", entity::setDescription);
        if (body.containsKey("parentId")) entity.setParentId(longValue(body.get("parentId"), entity.getParentId()));
        if (body.containsKey("sortOrder")) entity.setSortOrder(intValue(body.get("sortOrder"), entity.getSortOrder()));
        chapterMapper.updateById(entity);
    }

    public void deleteChapter(Long id) {
        requireChapter(id);
        chapterMapper.deleteById(id);
    }

    public void reorderChapters(Long courseId, List<Map<String, Object>> chapterOrder) {
        requireCourse(courseId);
        if (chapterOrder == null) {
            return;
        }
        for (Map<String, Object> item : chapterOrder) {
            ChapterEntity entity = requireChapter(longValue(item.get("id"), null));
            entity.setSortOrder(intValue(item.get("sortOrder"), entity.getSortOrder()));
            chapterMapper.updateById(entity);
        }
    }

    // ==================== Knowledge Points ====================

    public List<Map<String, Object>> listKnowledgePoints(Long courseId) {
        LambdaQueryWrapper<KnowledgePointEntity> wrapper = new LambdaQueryWrapper<KnowledgePointEntity>()
                .eq(KnowledgePointEntity::getCourseId, courseId)
                .orderByAsc(KnowledgePointEntity::getChapterId)
                .orderByAsc(KnowledgePointEntity::getDifficulty);
        return knowledgePointMapper.selectList(wrapper).stream().map(this::toKnowledgePointMap).toList();
    }

    public Map<String, Object> createKnowledgePoint(Map<String, Object> body) {
        requireCourse(longValue(body.get("courseId"), null));
        KnowledgePointEntity entity = new KnowledgePointEntity();
        entity.setCourseId(longValue(body.get("courseId"), null));
        entity.setChapterId(longValue(body.get("chapterId"), null));
        entity.setName(requiredString(body, "name"));
        entity.setDescription(stringValue(body.get("description")));
        entity.setDifficulty(intValue(body.get("difficulty"), 3));
        entity.setTags(stringValue(body.get("tags")));
        entity.setVectorId(longValue(body.get("vectorId"), null));
        knowledgePointMapper.insert(entity);
        return toKnowledgePointMap(entity);
    }

    public void updateKnowledgePoint(Long id, Map<String, Object> body) {
        KnowledgePointEntity entity = requireKnowledgePoint(id);
        setIfPresent(body, "name", entity::setName);
        setIfPresent(body, "description", entity::setDescription);
        setIfPresent(body, "tags", entity::setTags);
        if (body.containsKey("chapterId")) entity.setChapterId(longValue(body.get("chapterId"), entity.getChapterId()));
        if (body.containsKey("difficulty")) entity.setDifficulty(intValue(body.get("difficulty"), entity.getDifficulty()));
        if (body.containsKey("vectorId")) entity.setVectorId(longValue(body.get("vectorId"), entity.getVectorId()));
        knowledgePointMapper.updateById(entity);
    }

    public void deleteKnowledgePoint(Long id) {
        requireKnowledgePoint(id);
        knowledgePointMapper.deleteById(id);
    }

    public Map<String, Object> createKnowledgeRelation(Map<String, Object> body) {
        requireKnowledgePoint(longValue(body.get("pointId"), null));
        requireKnowledgePoint(longValue(body.get("relatedPointId"), null));
        KnowledgeRelationEntity entity = new KnowledgeRelationEntity();
        entity.setPointId(longValue(body.get("pointId"), null));
        entity.setRelatedPointId(longValue(body.get("relatedPointId"), null));
        entity.setRelationType(stringValue(body.getOrDefault("relationType", "RELATED")));
        knowledgeRelationMapper.insert(entity);
        return toKnowledgeRelationMap(entity);
    }

    public List<Map<String, Object>> listKnowledgeRelations(Long pointId) {
        return knowledgeRelationMapper.selectByPointId(pointId).stream().map(this::toKnowledgeRelationMap).toList();
    }

    public Map<String, Object> getKnowledgeGraph(Long courseId) {
        List<Map<String, Object>> nodes = listKnowledgePoints(courseId);
        List<Map<String, Object>> edges = knowledgeRelationMapper.selectList(new LambdaQueryWrapper<KnowledgeRelationEntity>())
                .stream()
                .filter(edge -> nodes.stream().anyMatch(node -> node.get("id").equals(edge.getPointId())))
                .map(this::toKnowledgeRelationMap)
                .toList();
        return Map.of("courseId", courseId, "nodes", nodes, "edges", edges);
    }

    // ==================== Course Materials ====================

    public List<Map<String, Object>> listCourseMaterials(Long courseId) {
        LambdaQueryWrapper<CourseMaterialEntity> wrapper = new LambdaQueryWrapper<CourseMaterialEntity>()
                .eq(CourseMaterialEntity::getCourseId, courseId)
                .orderByDesc(CourseMaterialEntity::getCreatedAt);
        return courseMaterialMapper.selectList(wrapper).stream().map(this::toCourseMaterialMap).toList();
    }

    public Map<String, Object> uploadCourseMaterial(MultipartFile file, Long courseId) {
        requireCourse(courseId);
        CourseMaterialEntity entity = new CourseMaterialEntity();
        entity.setCourseId(courseId);
        entity.setFileName(file.getOriginalFilename());
        entity.setFileType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setStoragePath("/uploads/course-materials/" + courseId + "/" + file.getOriginalFilename());
        entity.setUploadedBy(0L);
        courseMaterialMapper.insert(entity);
        return toCourseMaterialMap(entity);
    }

    public void deleteCourseMaterial(Long id) {
        CourseMaterialEntity entity = courseMaterialMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "课程资料不存在");
        }
        courseMaterialMapper.deleteById(id);
    }

    private CourseEntity requireCourse(Long id) {
        if (id == null) throw new BizException(ErrorCode.BAD_REQUEST, "courseId不能为空");
        CourseEntity entity = courseMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "课程不存在");
        return entity;
    }

    private ChapterEntity requireChapter(Long id) {
        if (id == null) throw new BizException(ErrorCode.BAD_REQUEST, "chapterId不能为空");
        ChapterEntity entity = chapterMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "章节不存在");
        return entity;
    }

    private KnowledgePointEntity requireKnowledgePoint(Long id) {
        if (id == null) throw new BizException(ErrorCode.BAD_REQUEST, "knowledgePointId不能为空");
        KnowledgePointEntity entity = knowledgePointMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND, "知识点不存在");
        return entity;
    }

    private Map<String, Object> toCourseMap(CourseEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenantId", entity.getTenantId());
        map.put("name", entity.getName());
        map.put("description", entity.getDescription());
        map.put("coverImage", entity.getCoverImage());
        map.put("status", entity.getStatus());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toChapterMap(ChapterEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("courseId", entity.getCourseId());
        map.put("parentId", entity.getParentId());
        map.put("name", entity.getName());
        map.put("sortOrder", entity.getSortOrder());
        map.put("description", entity.getDescription());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toKnowledgePointMap(KnowledgePointEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("courseId", entity.getCourseId());
        map.put("chapterId", entity.getChapterId());
        map.put("name", entity.getName());
        map.put("description", entity.getDescription());
        map.put("difficulty", entity.getDifficulty());
        map.put("tags", entity.getTags());
        map.put("vectorId", entity.getVectorId());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toKnowledgeRelationMap(KnowledgeRelationEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("pointId", entity.getPointId());
        map.put("relatedPointId", entity.getRelatedPointId());
        map.put("relationType", entity.getRelationType());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private Map<String, Object> toCourseMaterialMap(CourseMaterialEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("courseId", entity.getCourseId());
        map.put("fileName", entity.getFileName());
        map.put("fileType", entity.getFileType());
        map.put("fileSize", entity.getFileSize());
        map.put("storagePath", entity.getStoragePath());
        map.put("uploadedBy", entity.getUploadedBy());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private <T> R.PageData<T> pageData(Page<?> page, List<T> records) {
        return new R.PageData<>(records, page.getCurrent(), page.getSize(), page.getTotal(), page.hasNext());
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

    private void setIfPresent(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) {
        if (body.containsKey(key)) {
            setter.accept(stringValue(body.get(key)));
        }
    }
}
