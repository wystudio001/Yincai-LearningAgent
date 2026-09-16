package xyz.wystudio.yincai.profile.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_profile.profile_version")
public class ProfileVersionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long profileId;

    private Integer versionNo;

    private String snapshot;

    private String triggerType;

    private LocalDateTime createdAt;
}
