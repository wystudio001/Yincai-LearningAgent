package xyz.wystudio.yincai.course.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_course.chapter")
public class ChapterEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long courseId;

    private Long parentId;

    private String name;

    private Integer sortOrder;

    private String description;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
