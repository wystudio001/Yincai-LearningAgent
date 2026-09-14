package xyz.wystudio.yincai.profile.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.ProfileDialogueEntity;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.ProfileDimensionEntity;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.ProfileVersionEntity;
import xyz.wystudio.yincai.profile.infrastructure.persistence.entity.StudentProfileEntity;
import xyz.wystudio.yincai.profile.infrastructure.persistence.mapper.ProfileDialogueMapper;
import xyz.wystudio.yincai.profile.infrastructure.persistence.mapper.ProfileDimensionMapper;
import xyz.wystudio.yincai.profile.infrastructure.persistence.mapper.ProfileVersionMapper;
import xyz.wystudio.yincai.profile.infrastructure.persistence.mapper.StudentProfileMapper;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileApplicationService {

    private final StudentProfileMapper studentProfileMapper;
    private final ProfileDimensionMapper profileDimensionMapper;
    private final ProfileVersionMapper profileVersionMapper;
    private final ProfileDialogueMapper profileDialogueMapper;

    // ========== Student Profile ==========

    public Map<String, Object> getProfile(Long studentId, Long courseId) {
        StudentProfileEntity profile = findProfile(studentId, courseId);
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学生画像不存在");
        }
        Map<String, Object> result = toProfileMap(profile);
        result.put("dimensions", getDimensions(profile.getId()));
        result.put("radar", getRadarData(profile.getId()));
        return result;
    }

    public Map<String, Object> saveOrUpdateProfile(Map<String, Object> body) {
        Long studentId = longValue(body.get("studentId"), null);
        Long courseId = longValue(body.get("courseId"), null);
        if (studentId == null || courseId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "studentId和courseId不能为空");
        }
        StudentProfileEntity entity = findProfile(studentId, courseId);
        if (entity == null) {
            entity = new StudentProfileEntity();
            entity.setStudentId(studentId);
            entity.setCourseId(courseId);
            entity.setStatus("READY");
        }
        setProfileFields(entity, body);
        entity.setLastRefreshAt(LocalDateTime.now());
        if (entity.getId() == null) {
            studentProfileMapper.insert(entity);
        } else {
            studentProfileMapper.updateById(entity);
        }
        createProfileVersion(entity, "MANUAL_SAVE");
        return toProfileMap(entity);
    }

    public Map<String, Object> initProfile(Map<String, Object> body) {
        Long studentId = longValue(body.get("studentId"), null);
        Long courseId = longValue(body.get("courseId"), null);
        if (studentId == null || courseId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "studentId和courseId不能为空");
        }
        StudentProfileEntity entity = findProfile(studentId, courseId);
        if (entity == null) {
            entity = new StudentProfileEntity();
            entity.setStudentId(studentId);
            entity.setCourseId(courseId);
            entity.setStatus("INITIALIZING");
            entity.setSummary(stringValue(body.getOrDefault("summary", "画像初始化中")));
            entity.setConfidence(0.0);
            studentProfileMapper.insert(entity);
        }
        return toProfileMap(entity);
    }

    public R.PageData<Map<String, Object>> listProfiles(int page, int size, Long courseId, String keyword) {
        LambdaQueryWrapper<StudentProfileEntity> wrapper = new LambdaQueryWrapper<StudentProfileEntity>()
                .eq(courseId != null, StudentProfileEntity::getCourseId, courseId)
                .like(StringUtils.hasText(keyword), StudentProfileEntity::getSummary, keyword)
                .orderByDesc(StudentProfileEntity::getUpdatedAt);
        Page<StudentProfileEntity> result = studentProfileMapper.selectPage(new Page<>(page, size), wrapper);
        return pageData(result, result.getRecords().stream().map(this::toProfileMap).toList());
    }

    // ========== Profile Dialogues ==========

    public Map<String, Object> postDialogue(Map<String, Object> body) {
        ProfileDialogueEntity entity = new ProfileDialogueEntity();
        entity.setStudentId(longValue(body.get("studentId"), null));
        if (entity.getStudentId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "studentId不能为空");
        }
        entity.setRole(stringValue(body.getOrDefault("role", "STUDENT")));
        entity.setContent(requiredString(body, "content"));
        entity.setMetadata(stringValue(body.get("metadata")));
        profileDialogueMapper.insert(entity);
        return toDialogueMap(entity);
    }

    public List<Map<String, Object>> getDialogueHistory(Long studentId, Long courseId, int limit) {
        if (studentId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "studentId不能为空");
        }
        return profileDialogueMapper.selectByStudentId(studentId).stream()
                .limit(Math.max(1, limit))
                .map(this::toDialogueMap)
                .toList();
    }

    // ========== Profile Dimensions ==========

    public List<Map<String, Object>> getDimensions(Long profileId) {
        requireProfile(profileId);
        return profileDimensionMapper.selectByProfileId(profileId).stream().map(this::toDimensionMap).toList();
    }

    public Map<String, Object> updateDimensionOverride(Long dimensionId, Map<String, Object> body) {
        ProfileDimensionEntity entity = profileDimensionMapper.selectById(dimensionId);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "画像维度不存在");
        }
        setIfPresent(body, "dimensionName", entity::setDimensionName);
        setIfPresent(body, "label", entity::setLabel);
        setIfPresent(body, "evidence", entity::setEvidence);
        if (body.containsKey("score")) entity.setScore(intValue(body.get("score"), entity.getScore()));
        if (body.containsKey("confidence")) entity.setConfidence(doubleValue(body.get("confidence"), entity.getConfidence()));
        profileDimensionMapper.updateById(entity);
        return toDimensionMap(entity);
    }

    // ========== Profile Versions ==========

    public R.PageData<Map<String, Object>> getVersions(Long profileId, int page, int size) {
        requireProfile(profileId);
        LambdaQueryWrapper<ProfileVersionEntity> wrapper = new LambdaQueryWrapper<ProfileVersionEntity>()
                .eq(ProfileVersionEntity::getProfileId, profileId)
                .orderByDesc(ProfileVersionEntity::getVersionNo);
        Page<ProfileVersionEntity> result = profileVersionMapper.selectPage(new Page<>(page, size), wrapper);
        return pageData(result, result.getRecords().stream().map(this::toVersionMap).toList());
    }

    public Map<String, Object> getRadarData(Long profileId) {
        List<Map<String, Object>> dimensions = getDimensions(profileId);
        return Map.of(
                "profileId", profileId,
                "indicators", dimensions.stream().map(item -> Map.of(
                        "code", item.get("dimensionCode"),
                        "name", item.get("dimensionName"),
                        "score", item.get("score"),
                        "confidence", item.get("confidence")
                )).toList()
        );
    }

    // ========== Internal ==========

    public Map<String, Object> handleProfileEvent(Map<String, Object> event) {
        return saveOrUpdateProfile(event);
    }

    private StudentProfileEntity findProfile(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            return null;
        }
        return studentProfileMapper.selectByStudentId(studentId).stream()
                .filter(item -> Objects.equals(item.getCourseId(), courseId))
                .findFirst()
                .orElse(null);
    }

    private StudentProfileEntity requireProfile(Long profileId) {
        StudentProfileEntity entity = studentProfileMapper.selectById(profileId);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "学生画像不存在");
        }
        return entity;
    }

    private void setProfileFields(StudentProfileEntity entity, Map<String, Object> body) {
        setIfPresent(body, "status", entity::setStatus);
        setIfPresent(body, "summary", entity::setSummary);
        if (body.containsKey("confidence")) {
            entity.setConfidence(doubleValue(body.get("confidence"), entity.getConfidence()));
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("READY");
        }
        if (entity.getConfidence() == null) {
            entity.setConfidence(0.0);
        }
    }

    private void createProfileVersion(StudentProfileEntity profile, String triggerType) {
        int nextVersion = profileVersionMapper.selectByProfileId(profile.getId()).stream()
                .map(ProfileVersionEntity::getVersionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        ProfileVersionEntity version = new ProfileVersionEntity();
        version.setProfileId(profile.getId());
        version.setVersionNo(nextVersion);
        version.setSnapshot(String.format(Locale.ROOT,
                "{\"studentId\":%d,\"courseId\":%d,\"status\":\"%s\",\"confidence\":%.2f}",
                profile.getStudentId(), profile.getCourseId(), profile.getStatus(), profile.getConfidence()));
        version.setTriggerType(triggerType);
        profileVersionMapper.insert(version);
    }

    private Map<String, Object> toProfileMap(StudentProfileEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("studentId", entity.getStudentId());
        map.put("courseId", entity.getCourseId());
        map.put("status", entity.getStatus());
        map.put("summary", entity.getSummary());
        map.put("confidence", entity.getConfidence());
        map.put("lastRefreshAt", entity.getLastRefreshAt());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toDialogueMap(ProfileDialogueEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("studentId", entity.getStudentId());
        map.put("role", entity.getRole());
        map.put("content", entity.getContent());
        map.put("metadata", entity.getMetadata());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private Map<String, Object> toDimensionMap(ProfileDimensionEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("profileId", entity.getProfileId());
        map.put("dimensionCode", entity.getDimensionCode());
        map.put("dimensionName", entity.getDimensionName());
        map.put("score", entity.getScore());
        map.put("label", entity.getLabel());
        map.put("evidence", entity.getEvidence());
        map.put("confidence", entity.getConfidence());
        map.put("createdAt", entity.getCreatedAt());
        return map;
    }

    private Map<String, Object> toVersionMap(ProfileVersionEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("profileId", entity.getProfileId());
        map.put("versionNo", entity.getVersionNo());
        map.put("snapshot", entity.getSnapshot());
        map.put("triggerType", entity.getTriggerType());
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
        if (value == null || !StringUtils.hasText(String.valueOf(value))) return defaultValue;
        return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
    }

    private Integer intValue(Object value, Integer defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) return defaultValue;
        return value instanceof Number number ? number.intValue() : Integer.valueOf(String.valueOf(value));
    }

    private Double doubleValue(Object value, Double defaultValue) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) return defaultValue;
        return value instanceof Number number ? number.doubleValue() : Double.valueOf(String.valueOf(value));
    }

    private void setIfPresent(Map<String, Object> body, String key, java.util.function.Consumer<String> setter) {
        if (body.containsKey(key)) {
            setter.accept(stringValue(body.get(key)));
        }
    }
}
