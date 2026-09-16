package xyz.wystudio.yincai.resource.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_resource.generation_step")
public class GenerationStepEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    private String agentCode;

    private String stepType;

    private String status;

    private String inputSummary;

    private Long outputResourceId;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}
