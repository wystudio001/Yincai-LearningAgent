package xyz.wystudio.yincai.exam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_exam.exam_wrong_question")
public class WrongQuestionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long studentId;
    private Long questionId;
    private Long exerciseId;
    private Boolean reviewStatus;
    private Integer wrongCount;
    private LocalDateTime lastWrongAt;
    private LocalDateTime reviewedAt;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
