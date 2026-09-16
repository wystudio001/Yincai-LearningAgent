package xyz.wystudio.yincai.notify.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_notify.notify_push_task")
public class PushTaskEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long userId;
    private Long notificationId;
    private String channel;
    private String status;
    private Integer retryCount;
    private LocalDateTime sentAt;
    private String errorMessage;
    private LocalDateTime createdAt;
}
