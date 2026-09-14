package xyz.wystudio.yincai.profile.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_profile.student_profile")
public class StudentProfileEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;

    private Long courseId;

    private String status;

    private String summary;

    private Double confidence;

    private LocalDateTime lastRefreshAt;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
