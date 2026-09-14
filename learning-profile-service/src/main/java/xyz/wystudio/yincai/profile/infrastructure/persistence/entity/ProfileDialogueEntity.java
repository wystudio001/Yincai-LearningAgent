package xyz.wystudio.yincai.profile.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_profile.profile_dialogue")
public class ProfileDialogueEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;

    private String role;

    private String content;

    private String metadata;

    private LocalDateTime createdAt;
}
