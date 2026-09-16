package xyz.wystudio.yincai.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_auth.auth_login_log")
public class AuthLoginLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String clientType;
    private String ipAddress;
    private Integer success;
    private String failReason;
    private LocalDateTime createdAt;
}
