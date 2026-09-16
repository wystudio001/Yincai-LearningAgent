package xyz.wystudio.yincai.notify.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.notify.application.service.NotifyApplicationService;
import xyz.wystudio.yincai.notify.infrastructure.persistence.entity.NotificationEntity;
import xyz.wystudio.yincai.notify.infrastructure.persistence.entity.NotificationTemplateEntity;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyApplicationService notifyApplicationService;

    // ==================== Notifications ====================

    @GetMapping("/api/v1/notifications")
    public R<?> listNotifications(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return notifyApplicationService.listNotifications(pageNo, pageSize);
    }

    @GetMapping("/api/v1/notifications/unread-count")
    public R<Map<String, Long>> getUnreadCount() {
        return notifyApplicationService.getUnreadCount();
    }

    @PatchMapping("/api/v1/notifications/{id}/read")
    public R<Void> markAsRead(@PathVariable Long id) {
        return notifyApplicationService.markAsRead(id);
    }

    @PatchMapping("/api/v1/notifications/read-all")
    public R<Void> markAllAsRead() {
        return notifyApplicationService.markAllAsRead();
    }

    @DeleteMapping("/api/v1/notifications/{id}")
    public R<Void> deleteNotification(@PathVariable Long id) {
        return notifyApplicationService.deleteNotification(id);
    }

    // ==================== Notification Templates (Admin) ====================

    @PostMapping("/api/v1/admin/notification-templates")
    public R<NotificationTemplateEntity> createTemplate(@RequestBody NotificationTemplateEntity template) {
        return notifyApplicationService.createTemplate(template);
    }

    @GetMapping("/api/v1/admin/notification-templates")
    public R<?> listTemplates() {
        return R.ok();
    }

    @PutMapping("/api/v1/admin/notification-templates/{id}")
    public R<NotificationTemplateEntity> updateTemplate(@PathVariable Long id,
                                                         @RequestBody NotificationTemplateEntity template) {
        return notifyApplicationService.updateTemplate(id, template);
    }

    // ==================== Internal API ====================

    @PostMapping("/internal/v1/notifications/send")
    public R<Void> sendNotification(@RequestBody Map<String, Object> params) {
        return notifyApplicationService.sendNotification(params);
    }
}
