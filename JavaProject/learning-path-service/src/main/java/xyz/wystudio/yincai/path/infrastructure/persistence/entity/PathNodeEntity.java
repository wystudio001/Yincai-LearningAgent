package xyz.wystudio.yincai.path.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_path.learning_path_node")
public class PathNodeEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long pathId;

    private Long knowledgePointId;

    private Integer nodeOrder;

    private String target;

    /**
     * PENDING / IN_PROGRESS / COMPLETED / SKIPPED
     */
    private String status;

    private String completionRule;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
