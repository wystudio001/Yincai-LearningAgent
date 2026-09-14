package xyz.wystudio.yincai.resource.interfaces.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.resource.application.service.ResourceApplicationService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceApplicationService resourceApplicationService;

    // ======================== Resources ========================

    @GetMapping("/api/v1/resources")
    public R<?> pageResources(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return R.ok(resourceApplicationService.pageResources(page, size, courseId, knowledgePointId,
                resourceType, difficulty, status, keyword));
    }

    @PostMapping("/api/v1/resources")
    public R<?> createResource(@Valid @RequestBody Object request) {
        return R.ok(resourceApplicationService.createResource(request));
    }

    @GetMapping("/api/v1/resources/{id}")
    public R<?> getResourceById(@PathVariable String id) {
        return R.ok(resourceApplicationService.getResourceById(id));
    }

    @PutMapping("/api/v1/resources/{id}")
    public R<?> updateResource(@PathVariable String id, @Valid @RequestBody Object request) {
        return R.ok(resourceApplicationService.updateResource(id, request));
    }

    @DeleteMapping("/api/v1/resources/{id}")
    public R<?> deleteResource(@PathVariable String id) {
        return R.ok(resourceApplicationService.deleteResource(id));
    }

    @GetMapping("/api/v1/resources/{id}/download-url")
    public R<?> getResourceDownloadUrl(@PathVariable String id) {
        return R.ok(resourceApplicationService.getResourceDownloadUrl(id));
    }

    @PostMapping("/api/v1/resources/{id}/favorite")
    public R<?> addFavorite(@PathVariable String id) {
        return R.ok(resourceApplicationService.addFavorite(id));
    }

    @DeleteMapping("/api/v1/resources/{id}/favorite")
    public R<?> removeFavorite(@PathVariable String id) {
        return R.ok(resourceApplicationService.removeFavorite(id));
    }

    @PostMapping("/api/v1/resources/{id}/feedback")
    public R<?> submitFeedback(@PathVariable String id, @Valid @RequestBody Object request) {
        return R.ok(resourceApplicationService.submitFeedback(id, request));
    }

    @GetMapping("/api/v1/resources/favorites")
    public R<?> getUserFavorites(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        return R.ok(resourceApplicationService.getUserFavorites(page, size));
    }

    @GetMapping("/api/v1/resources/recent")
    public R<?> getRecentResources(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return R.ok(resourceApplicationService.getRecentResources(limit));
    }

    @GetMapping("/api/v1/resources/search")
    public R<?> searchResources(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String difficulty) {
        return R.ok(resourceApplicationService.searchResources(keyword, page, size, resourceType, difficulty));
    }

    @PostMapping("/api/v1/resources/semantic-search")
    public R<?> semanticSearch(@RequestBody Object request) {
        return R.ok(resourceApplicationService.semanticSearch(request));
    }

    // ======================== Resource Packages ========================

    @GetMapping("/api/v1/resource-packages")
    public R<?> listPackages(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) Long courseId) {
        return R.ok(resourceApplicationService.listPackages(page, size, courseId));
    }

    @PostMapping("/api/v1/resource-packages")
    public R<?> createPackage(@Valid @RequestBody Object request) {
        return R.ok(resourceApplicationService.createPackage(request));
    }

    @GetMapping("/api/v1/resource-packages/{id}")
    public R<?> getPackageById(@PathVariable String id) {
        return R.ok(resourceApplicationService.getPackageById(id));
    }

    @PostMapping("/api/v1/resource-packages/{id}/publish")
    public R<?> publishPackage(@PathVariable String id) {
        return R.ok(resourceApplicationService.publishPackage(id));
    }

    // ======================== Generation Tasks ========================

    @PostMapping("/api/v1/resource-generation-tasks")
    public R<?> createGenerationTask(@Valid @RequestBody Object request) {
        return R.ok(resourceApplicationService.createGenerationTask(request));
    }

    @GetMapping("/api/v1/resource-generation-tasks")
    public R<?> listGenerationTasks(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String taskType) {
        return R.ok(resourceApplicationService.listGenerationTasks(page, size, status, taskType));
    }

    @GetMapping("/api/v1/resource-generation-tasks/{id}")
    public R<?> getGenerationTaskById(@PathVariable String id) {
        return R.ok(resourceApplicationService.getGenerationTaskById(id));
    }

    @GetMapping("/api/v1/resource-generation-tasks/{id}/steps")
    public R<?> getGenerationTaskSteps(@PathVariable String id) {
        return R.ok(resourceApplicationService.getGenerationTaskSteps(id));
    }

    @GetMapping("/api/v1/resource-generation-tasks/{id}/stream")
    public R<?> streamGenerationTask(@PathVariable String id) {
        return R.ok(resourceApplicationService.streamGenerationTask(id));
    }

    @PostMapping("/api/v1/resource-generation-tasks/{id}/cancel")
    public R<?> cancelGenerationTask(@PathVariable String id) {
        return R.ok(resourceApplicationService.cancelGenerationTask(id));
    }

    @PostMapping("/api/v1/resource-generation-tasks/{id}/retry")
    public R<?> retryGenerationTask(@PathVariable String id) {
        return R.ok(resourceApplicationService.retryGenerationTask(id));
    }

    @GetMapping("/api/v1/resource-generation-tasks/{id}/outputs")
    public R<?> getGenerationTaskOutputs(@PathVariable String id) {
        return R.ok(resourceApplicationService.getGenerationTaskOutputs(id));
    }

    @PostMapping("/api/v1/resource-generation-tasks/{id}/publish")
    public R<?> publishGenerationTaskOutputs(@PathVariable String id) {
        return R.ok(resourceApplicationService.publishGenerationTaskOutputs(id));
    }

    // ======================== Templates ========================

    @GetMapping("/api/v1/resource-generation-templates")
    public R<?> listTemplates(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String taskType) {
        return R.ok(resourceApplicationService.listTemplates(page, size, taskType));
    }

    @PostMapping("/api/v1/resource-generation-templates")
    public R<?> createTemplate(@Valid @RequestBody Object request) {
        return R.ok(resourceApplicationService.createTemplate(request));
    }

    @PutMapping("/api/v1/resource-generation-templates/{id}")
    public R<?> updateTemplate(@PathVariable String id, @Valid @RequestBody Object request) {
        return R.ok(resourceApplicationService.updateTemplate(id, request));
    }

    @DeleteMapping("/api/v1/resource-generation-templates/{id}")
    public R<?> deleteTemplate(@PathVariable String id) {
        return R.ok(resourceApplicationService.deleteTemplate(id));
    }

    // ======================== Files ========================

    @PostMapping("/api/v1/files/upload-token")
    public R<?> getUploadToken() {
        return R.ok(resourceApplicationService.getUploadToken());
    }

    @PostMapping("/api/v1/files")
    public R<?> uploadFile(@RequestBody Object request) {
        return R.ok(resourceApplicationService.uploadFile(request));
    }

    @GetMapping("/api/v1/files/{id}")
    public R<?> getFileInfo(@PathVariable String id) {
        return R.ok(resourceApplicationService.getFileInfo(id));
    }

    @GetMapping("/api/v1/files/{id}/download-url")
    public R<?> getFileDownloadUrl(@PathVariable String id) {
        return R.ok(resourceApplicationService.getFileDownloadUrl(id));
    }

    @DeleteMapping("/api/v1/files/{id}")
    public R<?> deleteFile(@PathVariable String id) {
        return R.ok(resourceApplicationService.deleteFile(id));
    }

    @PostMapping("/api/v1/files/{id}/scan")
    public R<?> scanFile(@PathVariable String id) {
        return R.ok(resourceApplicationService.scanFile(id));
    }

    @GetMapping("/api/v1/files/{id}/preview-url")
    public R<?> getFilePreviewUrl(@PathVariable String id) {
        return R.ok(resourceApplicationService.getFilePreviewUrl(id));
    }
}
