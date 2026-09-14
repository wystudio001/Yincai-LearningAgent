package xyz.wystudio.yincai.exam.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("learning_exam.exam_assessment_report")
public class AssessmentReportEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long studentId;
    private Long courseId;
    private String reportType;
    private String summary;
    private String weaknesses;
    private String suggestions;
    private String scoreDetail;
    private LocalDateTime generatedAt;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
