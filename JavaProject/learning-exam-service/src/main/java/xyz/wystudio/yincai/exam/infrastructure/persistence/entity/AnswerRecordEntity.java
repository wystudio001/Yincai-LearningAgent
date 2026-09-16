package xyz.wystudio.yincai.exam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_exam.exam_answer_record")
public class AnswerRecordEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long studentId;
    private Long exerciseId;
    private Long questionId;
    private String answerContent;
    private Integer score;
    private Boolean isCorrect;
    private String feedback;
    private Long knowledgePointId;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
