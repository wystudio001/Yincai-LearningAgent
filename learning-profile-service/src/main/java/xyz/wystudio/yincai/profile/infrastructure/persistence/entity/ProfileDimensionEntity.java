package xyz.wystudio.yincai.profile.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_profile.profile_dimension")
public class ProfileDimensionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long profileId;

    private String dimensionCode;

    private String dimensionName;

    private Integer score;

    private String label;

    private String evidence;

    private Double confidence;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}
