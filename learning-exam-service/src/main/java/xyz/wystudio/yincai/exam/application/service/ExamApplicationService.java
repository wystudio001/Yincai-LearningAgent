package xyz.wystudio.yincai.exam.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.exam.infrastructure.persistence.entity.*;
import xyz.wystudio.yincai.exam.infrastructure.persistence.mapper.*;
import xyz.wystudio.yincai.security.UserContextHolder;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamApplicationService {

    private final QuestionMapper questionMapper;
    private final ExerciseMapper exerciseMapper;
    private final ExerciseQuestionMapper exerciseQuestionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final AssessmentReportMapper assessmentReportMapper;

    // ========== Questions ==========

    public R<Page<QuestionEntity>> listQuestions(int pageNo, int pageSize, Long courseId, String questionType) {
        LambdaQueryWrapper<QuestionEntity> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(QuestionEntity::getCourseId, courseId);
        }
        if (questionType != null) {
            wrapper.eq(QuestionEntity::getQuestionType, questionType);
        }
        return R.ok(questionMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
    }

    public R<QuestionEntity> createQuestion(QuestionEntity question) {
        questionMapper.insert(question);
        return R.ok(question);
    }

    public R<QuestionEntity> getQuestion(Long id) {
        return R.ok(questionMapper.selectById(id));
    }

    public R<QuestionEntity> updateQuestion(Long id, QuestionEntity question) {
        question.setId(id);
        questionMapper.updateById(question);
        return R.ok(questionMapper.selectById(id));
    }

    public R<Void> deleteQuestion(Long id) {
        questionMapper.deleteById(id);
        return R.ok();
    }

    public R<List<QuestionEntity>> generateQuestions(Map<String, Object> params) {
        log.info("Generate questions with params: {}", params);
        return R.ok(List.of());
    }

    // ========== Exercises ==========

    public R<Page<ExerciseEntity>> listExercises(int pageNo, int pageSize) {
        return R.ok(exerciseMapper.selectPage(new Page<>(pageNo, pageSize), null));
    }

    public R<ExerciseEntity> createExercise(ExerciseEntity exercise) {
        exerciseMapper.insert(exercise);
        return R.ok(exercise);
    }

    public R<ExerciseEntity> getExercise(Long id) {
        return R.ok(exerciseMapper.selectById(id));
    }

    /** 返回指定练习关联的题目，学生端不下发标准答案和解析。 */
    public R<List<Map<String, Object>>> listExerciseQuestions(Long exerciseId) {
        List<ExerciseQuestionEntity> links = exerciseQuestionMapper.selectList(
                new LambdaQueryWrapper<ExerciseQuestionEntity>()
                        .eq(ExerciseQuestionEntity::getExerciseId, exerciseId)
                        .orderByAsc(ExerciseQuestionEntity::getSortOrder));
        List<Map<String, Object>> questions = new ArrayList<>();
        for (ExerciseQuestionEntity link : links) {
            QuestionEntity question = questionMapper.selectById(link.getQuestionId());
            if (question == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", question.getId());
            item.put("questionType", question.getQuestionType());
            item.put("title", question.getTitle());
            item.put("content", question.getContent());
            item.put("options", question.getOptions());
            item.put("difficulty", question.getDifficulty());
            item.put("sortOrder", link.getSortOrder());
            item.put("score", link.getScore());
            questions.add(item);
        }
        return R.ok(questions);
    }

    public R<ExerciseEntity> startExercise(Long id) {
        ExerciseEntity exercise = exerciseMapper.selectById(id);
        return R.ok(exercise);
    }

    public R<Void> submitAnswers(Long id, List<AnswerRecordEntity> answers) {
        Long studentId = UserContextHolder.getUserId();
        if (studentId == null) {
            return R.fail(xyz.wystudio.yincai.core.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        for (AnswerRecordEntity answer : answers) {
            answer.setExerciseId(id);
            answer.setStudentId(studentId);
            answer.setSubmittedAt(java.time.LocalDateTime.now());
            answerRecordMapper.insert(answer);
        }
        return R.ok();
    }

    public R<Void> submitExercise(Long id) {
        log.info("Submit exercise: {}", id);
        return R.ok();
    }

    public R<Map<String, Object>> getExerciseResult(Long id) {
        Long studentId = UserContextHolder.getUserId();
        if (studentId == null) {
            return R.fail(xyz.wystudio.yincai.core.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        List<AnswerRecordEntity> records = answerRecordMapper.selectList(
                new LambdaQueryWrapper<AnswerRecordEntity>()
                        .eq(AnswerRecordEntity::getExerciseId, id)
                        .eq(AnswerRecordEntity::getStudentId, studentId));
        Map<String, Object> result = Map.of(
                "exerciseId", id,
                "answerCount", records.size(),
                "records", records
        );
        return R.ok(result);
    }

    // ========== Wrong Questions ==========

    public R<Page<WrongQuestionEntity>> listWrongQuestions(int pageNo, int pageSize, Long studentId) {
        LambdaQueryWrapper<WrongQuestionEntity> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(WrongQuestionEntity::getStudentId, studentId);
        }
        return R.ok(wrongQuestionMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
    }

    public R<Void> markWrongQuestionReviewed(Long questionId) {
        LambdaQueryWrapper<WrongQuestionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestionEntity::getQuestionId, questionId);
        WrongQuestionEntity entity = wrongQuestionMapper.selectOne(wrapper);
        if (entity != null) {
            entity.setReviewStatus(true);
            entity.setReviewedAt(java.time.LocalDateTime.now());
            wrongQuestionMapper.updateById(entity);
        }
        return R.ok();
    }

    // ========== Assessments ==========

    public R<AssessmentReportEntity> getLatestAssessment(Long studentId) {
        LambdaQueryWrapper<AssessmentReportEntity> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(AssessmentReportEntity::getStudentId, studentId);
        }
        wrapper.orderByDesc(AssessmentReportEntity::getGeneratedAt);
        wrapper.last("LIMIT 1");
        return R.ok(assessmentReportMapper.selectOne(wrapper));
    }

    public R<AssessmentReportEntity> getAssessment(Long assessmentId) {
        return R.ok(assessmentReportMapper.selectById(assessmentId));
    }

    public R<AssessmentReportEntity> generateAssessment(Map<String, Object> params) {
        log.info("Generate assessment with params: {}", params);
        AssessmentReportEntity report = new AssessmentReportEntity();
        report.setTenantId(0L);
        report.setStudentId(params.get("studentId") == null ? null : Long.valueOf(params.get("studentId").toString()));
        report.setCourseId(params.get("courseId") == null ? null : Long.valueOf(params.get("courseId").toString()));
        report.setReportType(String.valueOf(params.getOrDefault("reportType", "MANUAL")));
        report.setSummary(String.valueOf(params.getOrDefault("summary", "基于当前答题记录生成的阶段性测评报告")));
        report.setWeaknesses(String.valueOf(params.getOrDefault("weaknesses", "暂无明确薄弱项，请继续完成练习以积累分析数据")));
        report.setSuggestions(String.valueOf(params.getOrDefault("suggestions", "建议复习最近错题并完成下一阶段练习")));
        report.setScoreDetail(String.valueOf(params.getOrDefault("scoreDetail", "{}")));
        report.setGeneratedAt(java.time.LocalDateTime.now());
        assessmentReportMapper.insert(report);
        return R.ok(report);
    }
}
