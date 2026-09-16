package xyz.wystudio.yincai.user.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.user.infrastructure.persistence.entity.RoleEntity;
import xyz.wystudio.yincai.user.infrastructure.persistence.entity.UserEntity;
import xyz.wystudio.yincai.user.infrastructure.persistence.mapper.RoleMapper;
import xyz.wystudio.yincai.user.infrastructure.persistence.mapper.UserMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApplicationService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    // ==================== User Management ====================

    public Map<String, Object> createUser(Map<String, Object> body) {
        UserEntity existing = userMapper.selectByUsername(stringValue(body.get("username")));
        if (existing != null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }
        UserEntity entity = new UserEntity();
        entity.setTenantId(longValue(body.get("tenantId"), 0L));
        entity.setUsername(requiredString(body, "username"));
        entity.setRealName(stringValue(body.get("realName")));
        entity.setEmail(stringValue(body.get("email")));
        entity.setPhone(stringValue(body.get("phone")));
        entity.setRoleCode(stringValue(body.getOrDefault("roleCode", "STUDENT")));
        entity.setStatus(intValue(body.get("status"), 1));
        userMapper.insert(entity);
        return toUserMap(entity);
    }

    public R.PageData<Map<String, Object>> listUsers(int page, int size, String keyword) {
        Page<UserEntity> result = userMapper.selectPage(new Page<>(page, size), userQuery(keyword));
        return pageData(result, result.getRecords().stream().map(this::toUserMap).toList());
    }

    public Map<String, Object> getUserById(Long id) {
        return toUserMap(requireUser(id));
    }

    public void updateUser(Long id, Map<String, Object> body) {
        UserEntity entity = requireUser(id);
        setIfPresent(body, "realName", entity::setRealName);
        setIfPresent(body, "email", entity::setEmail);
        setIfPresent(body, "phone", entity::setPhone);
        setIfPresent(body, "roleCode", entity::setRoleCode);
        if (body.containsKey("status")) {
            entity.setStatus(intValue(body.get("status"), entity.getStatus()));
        }
        userMapper.updateById(entity);
    }

    public void updateUserStatus(Long id, Integer status) {
        UserEntity entity = requireUser(id);
        entity.setStatus(status == null ? entity.getStatus() : status);
        userMapper.updateById(entity);
    }

    public void deleteUser(Long id) {
        requireUser(id);
        userMapper.deleteById(id);
    }

    public List<String> getUserRoles(Long userId) {
        UserEntity entity = requireUser(userId);
        return StringUtils.hasText(entity.getRoleCode()) ? List.of(entity.getRoleCode()) : List.of();
    }

    public void updateUserRoles(Long userId, List<String> roleCodes) {
        UserEntity entity = requireUser(userId);
        entity.setRoleCode(roleCodes == null || roleCodes.isEmpty() ? null : roleCodes.get(0));
        userMapper.updateById(entity);
    }

    // ==================== Role Management ====================

    public R.PageData<Map<String, Object>> listRoles(int page, int size) {
        Page<RoleEntity> result = roleMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RoleEntity>().orderByDesc(RoleEntity::getCreatedAt));
        return pageData(result, result.getRecords().stream().map(this::toRoleMap).toList());
    }

    public Map<String, Object> createRole(Map<String, Object> body) {
        RoleEntity existing = roleMapper.selectByCode(requiredString(body, "code"));
        if (existing != null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "角色编码已存在");
        }
        RoleEntity entity = new RoleEntity();
        entity.setTenantId(longValue(body.get("tenantId"), 0L));
        entity.setName(requiredString(body, "name"));
        entity.setCode(requiredString(body, "code"));
        entity.setDescription(stringValue(body.get("description")));
        roleMapper.insert(entity);
        return toRoleMap(entity);
    }

    public void updateRole(Long id, Map<String, Object> body) {
        RoleEntity entity = requireRole(id);
        setIfPresent(body, "name", entity::setName);
        setIfPresent(body, "code", entity::setCode);
        setIfPresent(body, "description", entity::setDescription);
        roleMapper.updateById(entity);
    }

    public void deleteRole(Long id) {
        requireRole(id);
        roleMapper.deleteById(id);
    }

    // ==================== Permission Management ====================

    public List<Object> getPermissionTree() {
        return List.of(
                Map.of("code", "admin:*", "name", "管理端全部权限"),
                Map.of("code", "student:profile", "name", "学生画像"),
                Map.of("code", "student:path", "name", "学习路径"),
                Map.of("code", "student:resource", "name", "学习资源"),
                Map.of("code", "student:exam", "name", "练习测评")
        );
    }

    public void updateRolePermissions(Long roleId, List<String> permissions) {
        requireRole(roleId);
        log.info("Role permissions are represented by fixed roleCode policy in current version, roleId={}, permissions={}", roleId, permissions);
    }

    private UserEntity requireUser(Long id) {
        UserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return entity;
    }

    private RoleEntity requireRole(Long id) {
        RoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return entity;
    }

    private LambdaQueryWrapper<UserEntity> userQuery(String keyword) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>().orderByDesc(UserEntity::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(UserEntity::getUsername, keyword).or().like(UserEntity::getRealName, keyword));
        }
        return wrapper;
    }

    private Map<String, Object> toUserMap(UserEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenantId", entity.getTenantId());
        map.put("username", entity.getUsername());
        map.put("realName", entity.getRealName());
        map.put("email", entity.getEmail());
        map.put("phone", entity.getPhone());
        map.put("roleCode", entity.getRoleCode());
        map.put("status", entity.getStatus());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toRoleMap(RoleEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("tenantId", entity.getTenantId());
        map.put("name", entity.getName());
        map.put("code", entity.getCode());
        map.put("description", entity.getDescription());
        map.put("createdAt", entity.getCreatedAt());
        map.put("updatedAt", entity.getUpdatedAt());
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
