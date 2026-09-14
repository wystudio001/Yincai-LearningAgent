package xyz.wystudio.yincai.notify.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_notify.notify_notification")
public class NotificationEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private String status;
    private String targetType;
    private Long targetId;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
