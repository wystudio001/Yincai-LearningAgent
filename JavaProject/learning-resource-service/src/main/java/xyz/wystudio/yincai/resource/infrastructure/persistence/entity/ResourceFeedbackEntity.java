package xyz.wystudio.yincai.resource.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_resource.resource_feedback")
public class ResourceFeedbackEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long resourceId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}
