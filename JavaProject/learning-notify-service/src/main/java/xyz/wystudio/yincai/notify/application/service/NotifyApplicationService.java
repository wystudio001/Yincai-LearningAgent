package xyz.wystudio.yincai.notify.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.notify.infrastructure.persistence.entity.NotificationEntity;
import xyz.wystudio.yincai.notify.infrastructure.persistence.entity.NotificationTemplateEntity;
import xyz.wystudio.yincai.notify.infrastructure.persistence.mapper.NotificationMapper;
import xyz.wystudio.yincai.notify.infrastructure.persistence.mapper.NotificationTemplateMapper;
import xyz.wystudio.yincai.notify.infrastructure.persistence.mapper.PushTaskMapper;
import xyz.wystudio.yincai.security.CurrentUser;
import xyz.wystudio.yincai.security.UserContextHolder;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyApplicationService {

    private final NotificationMapper notificationMapper;
    private final NotificationTemplateMapper notificationTemplateMapper;
    private final PushTaskMapper pushTaskMapper;

    // ========== Notifications ==========

    public R<Page<NotificationEntity>> listNotifications(int pageNo, int pageSize) {
        CurrentUser currentUser = UserContextHolder.get();
        LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
        if (currentUser != null) {
            wrapper.eq(NotificationEntity::getUserId, currentUser.getUserId());
        }
        wrapper.orderByDesc(NotificationEntity::getCreatedAt);
        return R.ok(notificationMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
    }

    public R<Map<String, Long>> getUnreadCount() {
        CurrentUser currentUser = UserContextHolder.get();
        Long count = 0L;
        if (currentUser != null) {
            LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(NotificationEntity::getUserId, currentUser.getUserId());
            wrapper.eq(NotificationEntity::getStatus, "UNREAD");
            count = notificationMapper.selectCount(wrapper);
        }
        return R.ok(Map.of("unreadCount", count));
    }

    public R<Void> markAsRead(Long id) {
        NotificationEntity entity = notificationMapper.selectById(id);
        if (entity != null) {
            entity.setStatus("READ");
            notificationMapper.updateById(entity);
        }
        return R.ok();
    }

    public R<Void> markAllAsRead() {
        CurrentUser currentUser = UserContextHolder.get();
        if (currentUser != null) {
            LambdaQueryWrapper<NotificationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(NotificationEntity::getUserId, currentUser.getUserId());
            wrapper.eq(NotificationEntity::getStatus, "UNREAD");
            NotificationEntity update = new NotificationEntity();
            update.setStatus("READ");
            notificationMapper.update(update, wrapper);
        }
        return R.ok();
    }

    public R<Void> deleteNotification(Long id) {
        notificationMapper.deleteById(id);
        return R.ok();
    }

    // ========== Notification Templates ==========

    public R<NotificationTemplateEntity> createTemplate(NotificationTemplateEntity template) {
        notificationTemplateMapper.insert(template);
        return R.ok(template);
    }

    public R<NotificationTemplateEntity> updateTemplate(Long id, NotificationTemplateEntity template) {
        template.setId(id);
        notificationTemplateMapper.updateById(template);
        return R.ok(notificationTemplateMapper.selectById(id));
    }

    // ========== Internal Send ==========

    public R<Void> sendNotification(Map<String, Object> params) {
        log.info("Send notification via internal API: {}", params);
        NotificationEntity entity = new NotificationEntity();
        if (params.get("tenantId") != null) {
            entity.setTenantId(Long.valueOf(params.get("tenantId").toString()));
        }
        if (params.get("userId") != null) {
            entity.setUserId(Long.valueOf(params.get("userId").toString()));
        }
        entity.setTitle((String) params.get("title"));
        entity.setContent((String) params.get("content"));
        entity.setType((String) params.getOrDefault("type", "SYSTEM"));
        entity.setStatus("UNREAD");
        entity.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(entity);
        return R.ok();
    }
}
