package xyz.wystudio.yincai.course.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_course.knowledge_relation")
public class KnowledgeRelationEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long pointId;

    private Long relatedPointId;

    private String relationType;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
