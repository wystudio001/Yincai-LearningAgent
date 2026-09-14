package xyz.wystudio.yincai.path.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_path.path_intervention")
public class PathInterventionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long studentId;

    private Long pathId;

    private Long instructorId;

    private String action;

    private String reason;

    private String result;

    private LocalDateTime createdAt;
}
