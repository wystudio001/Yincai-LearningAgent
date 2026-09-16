package xyz.wystudio.yincai.course.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_course.knowledge_point")
public class KnowledgePointEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long courseId;

    private Long chapterId;

    private String name;

    private String description;

    private Integer difficulty;

    private String tags;

    private Long vectorId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
