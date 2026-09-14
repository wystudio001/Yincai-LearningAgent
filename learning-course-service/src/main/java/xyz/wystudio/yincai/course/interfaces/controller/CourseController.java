package xyz.wystudio.yincai.course.interfaces.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import xyz.wystudio.yincai.course.application.service.CourseApplicationService;
import xyz.wystudio.yincai.core.R;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CourseController {

    private final CourseApplicationService courseApplicationService;

    // ==================== Course Endpoints ====================

    @PostMapping("/courses")
    public R<Map<String, Object>> createCourse(@RequestBody Map<String, Object> body) {
        return R.ok(courseApplicationService.createCourse(body));
    }

    @GetMapping("/courses")
    public R<R.PageData> listCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(courseApplicationService.listCourses(page, size, keyword, status));
    }

    @GetMapping("/courses/{id}")
    public R<Map<String, Object>> getCourseById(@PathVariable Long id) {
        return R.ok(courseApplicationService.getCourseById(id));
    }

    @PutMapping("/courses/{id}")
    public R<Void> updateCourse(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        courseApplicationService.updateCourse(id, body);
        return R.ok();
    }

    @PostMapping("/courses/{id}/publish")
    public R<Void> publishCourse(@PathVariable Long id) {
        courseApplicationService.publishCourse(id);
        return R.ok();
    }

    @PostMapping("/courses/{id}/unpublish")
    public R<Void> unpublishCourse(@PathVariable Long id) {
        courseApplicationService.unpublishCourse(id);
        return R.ok();
    }

    @DeleteMapping("/courses/{id}")
    public R<Void> deleteCourse(@PathVariable Long id) {
        courseApplicationService.deleteCourse(id);
        return R.ok();
    }

    // ==================== Chapter Endpoints ====================

    @GetMapping("/courses/{courseId}/chapters")
    public R<List<Map<String, Object>>> listChapters(@PathVariable Long courseId) {
        return R.ok(courseApplicationService.listChapters(courseId));
    }

    @PostMapping("/chapters")
    public R<Map<String, Object>> createChapter(@RequestBody Map<String, Object> body) {
        return R.ok(courseApplicationService.createChapter(body));
    }

    @PutMapping("/chapters/{id}")
    public R<Void> updateChapter(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        courseApplicationService.updateChapter(id, body);
        return R.ok();
    }

    @DeleteMapping("/chapters/{id}")
    public R<Void> deleteChapter(@PathVariable Long id) {
        courseApplicationService.deleteChapter(id);
        return R.ok();
    }

    @PutMapping("/courses/{courseId}/chapters/reorder")
    public R<Void> reorderChapters(@PathVariable Long courseId, @RequestBody List<Map<String, Object>> chapterOrder) {
        courseApplicationService.reorderChapters(courseId, chapterOrder);
        return R.ok();
    }

    // ==================== Knowledge Point Endpoints ====================

    @GetMapping("/courses/{courseId}/knowledge-points")
    public R<List<Map<String, Object>>> listKnowledgePoints(@PathVariable Long courseId) {
        return R.ok(courseApplicationService.listKnowledgePoints(courseId));
    }

    @PostMapping("/knowledge-points")
    public R<Map<String, Object>> createKnowledgePoint(@RequestBody Map<String, Object> body) {
        return R.ok(courseApplicationService.createKnowledgePoint(body));
    }

    @PutMapping("/knowledge-points/{id}")
    public R<Void> updateKnowledgePoint(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        courseApplicationService.updateKnowledgePoint(id, body);
        return R.ok();
    }

    @DeleteMapping("/knowledge-points/{id}")
    public R<Void> deleteKnowledgePoint(@PathVariable Long id) {
        courseApplicationService.deleteKnowledgePoint(id);
        return R.ok();
    }

    // ==================== Knowledge Relation Endpoints ====================

    @PostMapping("/knowledge-relations")
    public R<Map<String, Object>> createKnowledgeRelation(@RequestBody Map<String, Object> body) {
        return R.ok(courseApplicationService.createKnowledgeRelation(body));
    }

    @GetMapping("/knowledge-relations/point/{pointId}")
    public R<List<Map<String, Object>>> listKnowledgeRelations(@PathVariable Long pointId) {
        return R.ok(courseApplicationService.listKnowledgeRelations(pointId));
    }

    // ==================== Knowledge Graph ====================

    @GetMapping("/courses/{courseId}/knowledge-graph")
    public R<Map<String, Object>> getKnowledgeGraph(@PathVariable Long courseId) {
        return R.ok(courseApplicationService.getKnowledgeGraph(courseId));
    }

    // ==================== Course Material Endpoints ====================

    @GetMapping("/courses/{courseId}/materials")
    public R<List<Map<String, Object>>> listCourseMaterials(@PathVariable Long courseId) {
        return R.ok(courseApplicationService.listCourseMaterials(courseId));
    }

    @PostMapping("/course-materials/upload")
    public R<Map<String, Object>> uploadCourseMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId) {
        return R.ok(courseApplicationService.uploadCourseMaterial(file, courseId));
    }

    @DeleteMapping("/course-materials/{id}")
    public R<Void> deleteCourseMaterial(@PathVariable Long id) {
        courseApplicationService.deleteCourseMaterial(id);
        return R.ok();
    }
}
