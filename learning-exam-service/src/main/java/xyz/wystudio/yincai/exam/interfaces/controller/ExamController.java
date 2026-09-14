package xyz.wystudio.yincai.exam.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.exam.application.service.ExamApplicationService;
import xyz.wystudio.yincai.exam.infrastructure.persistence.entity.*;
import xyz.wystudio.yincai.security.UserContextHolder;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExamController {

    private final ExamApplicationService examApplicationService;

    // ==================== Questions ====================

    @GetMapping("/api/v1/questions")
    public R<?> listQuestions(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String questionType) {
        return examApplicationService.listQuestions(pageNo, pageSize, courseId, questionType);
    }

    @PostMapping("/api/v1/questions")
    public R<QuestionEntity> createQuestion(@RequestBody QuestionEntity question) {
        return examApplicationService.createQuestion(question);
    }

    @GetMapping("/api/v1/questions/{id}")
    public R<QuestionEntity> getQuestion(@PathVariable Long id) {
        return examApplicationService.getQuestion(id);
    }

    @PutMapping("/api/v1/questions/{id}")
    public R<QuestionEntity> updateQuestion(@PathVariable Long id, @RequestBody QuestionEntity question) {
        return examApplicationService.updateQuestion(id, question);
    }

    @DeleteMapping("/api/v1/questions/{id}")
    public R<Void> deleteQuestion(@PathVariable Long id) {
        return examApplicationService.deleteQuestion(id);
    }

    @PostMapping("/api/v1/questions/generate")
    public R<List<QuestionEntity>> generateQuestions(@RequestBody Map<String, Object> params) {
        return examApplicationService.generateQuestions(params);
    }

    // ==================== Exercises ====================

    @GetMapping("/api/v1/exercises")
    public R<?> listExercises(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return examApplicationService.listExercises(pageNo, pageSize);
    }

    @PostMapping("/api/v1/exercises")
    public R<ExerciseEntity> createExercise(@RequestBody ExerciseEntity exercise) {
        return examApplicationService.createExercise(exercise);
    }

    @GetMapping("/api/v1/exercises/{id}")
    public R<ExerciseEntity> getExercise(@PathVariable Long id) {
        return examApplicationService.getExercise(id);
    }

    @GetMapping("/api/v1/exercises/{id}/questions")
    public R<List<Map<String, Object>>> listExerciseQuestions(@PathVariable Long id) {
        return examApplicationService.listExerciseQuestions(id);
    }

    @PostMapping("/api/v1/exercises/{id}/start")
    public R<ExerciseEntity> startExercise(@PathVariable Long id) {
        return examApplicationService.startExercise(id);
    }

    @PostMapping("/api/v1/exercises/{id}/answers")
    public R<Void> submitAnswers(@PathVariable Long id, @RequestBody List<AnswerRecordEntity> answers) {
        return examApplicationService.submitAnswers(id, answers);
    }

    @PostMapping("/api/v1/exercises/{id}/submit")
    public R<Void> submitExercise(@PathVariable Long id) {
        return examApplicationService.submitExercise(id);
    }

    @GetMapping("/api/v1/exercises/{id}/result")
    public R<Map<String, Object>> getExerciseResult(@PathVariable Long id) {
        return examApplicationService.getExerciseResult(id);
    }

    // ==================== Wrong Questions (Student) ====================

    @GetMapping("/api/v1/student/wrong-questions")
    public R<?> listWrongQuestions(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long studentId = UserContextHolder.get() != null ? UserContextHolder.get().getUserId() : null;
        return examApplicationService.listWrongQuestions(pageNo, pageSize, studentId);
    }

    @PostMapping("/api/v1/student/wrong-questions/{questionId}/reviewed")
    public R<Void> markWrongQuestionReviewed(@PathVariable Long questionId) {
        return examApplicationService.markWrongQuestionReviewed(questionId);
    }

    // ==================== Assessments ====================

    @GetMapping("/api/v1/assessments/latest")
    public R<AssessmentReportEntity> getLatestAssessment() {
        Long studentId = UserContextHolder.get() != null ? UserContextHolder.get().getUserId() : null;
        return examApplicationService.getLatestAssessment(studentId);
    }

    @GetMapping("/api/v1/assessments/{assessmentId}")
    public R<AssessmentReportEntity> getAssessment(@PathVariable Long assessmentId) {
        return examApplicationService.getAssessment(assessmentId);
    }

    @PostMapping("/api/v1/assessments/generate")
    public R<AssessmentReportEntity> generateAssessment(@RequestBody Map<String, Object> params) {
        return examApplicationService.generateAssessment(params);
    }
}
