package xyz.wystudio.yincai.exam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_exam.exam_question")
public class QuestionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String questionType;
    private String title;
    private String content;
    private String options;
    private String answer;
    private String explanation;
    private String tags;
    private Integer difficulty;
    private Long courseId;
    private Long knowledgePointId;
    private String source;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
