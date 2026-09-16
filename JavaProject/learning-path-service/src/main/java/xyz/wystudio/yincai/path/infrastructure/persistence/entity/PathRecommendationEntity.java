package xyz.wystudio.yincai.path.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_path.path_recommendation")
public class PathRecommendationEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long pathNodeId;

    private Long resourceId;

    private String resourceType;

    private Integer priority;

    private String reason;

    private LocalDateTime createdAt;
}
