package xyz.wystudio.yincai.path.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_path.learning_path")
public class LearningPathEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;

    private Long courseId;

    /**
     * ACTIVE / COMPLETED / ARCHIVED
     */
    private String status;

    private Integer version;

    /**
     * SYSTEM / INSTRUCTOR
     */
    private String generatedBy;

    private Long currentNodeId;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
