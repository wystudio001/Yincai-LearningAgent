package xyz.wystudio.yincai.course.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_course.course_material")
public class CourseMaterialEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long courseId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String storagePath;

    private Long uploadedBy;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
