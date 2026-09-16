package xyz.wystudio.yincai.exam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_exam.exam_exercise_question")
public class ExerciseQuestionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long exerciseId;
    private Long questionId;
    private Integer sortOrder;
    private Integer score;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
