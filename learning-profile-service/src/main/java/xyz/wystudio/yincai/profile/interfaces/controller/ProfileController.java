package xyz.wystudio.yincai.profile.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.profile.application.service.ProfileApplicationService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileApplicationService profileApplicationService;

    // ========== Student Profile ==========

    @GetMapping("/api/v1/student/profile")
    public R<?> getProfile(@RequestParam(required = false) Long studentId,
                           @RequestParam(required = false) Long courseId) {
        return R.ok(profileApplicationService.getProfile(studentId, courseId));
    }

    @PostMapping("/api/v1/student/profile")
    public R<?> saveOrUpdateProfile(@RequestBody Map<String, Object> body) {
        return R.ok(profileApplicationService.saveOrUpdateProfile(body));
    }

    @PostMapping("/api/v1/student/profile/dialogues")
    public R<?> postDialogue(@RequestBody Map<String, Object> body) {
        return R.ok(profileApplicationService.postDialogue(body));
    }

    @GetMapping("/api/v1/student/profile/dialogues")
    public R<?> getDialogueHistory(@RequestParam(required = false) Long studentId,
                                   @RequestParam(required = false) Long courseId,
                                   @RequestParam(defaultValue = "50") int limit) {
        return R.ok(profileApplicationService.getDialogueHistory(studentId, courseId, limit));
    }

    @PostMapping("/api/v1/student/profile/init")
    public R<?> initProfile(@RequestBody Map<String, Object> body) {
        return R.ok(profileApplicationService.initProfile(body));
    }

    // ========== Profile Management ==========

    @GetMapping("/api/v1/profiles")
    public R<?> listProfiles(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Long courseId,
                             @RequestParam(required = false) String keyword) {
        return R.ok(profileApplicationService.listProfiles(page, size, courseId, keyword));
    }

    @GetMapping("/api/v1/profiles/{profileId}/dimensions")
    public R<?> getDimensions(@PathVariable Long profileId) {
        return R.ok(profileApplicationService.getDimensions(profileId));
    }

    @PutMapping("/api/v1/profiles/dimensions/{dimensionId}")
    public R<?> updateDimensionOverride(@PathVariable Long dimensionId,
                                        @RequestBody Map<String, Object> body) {
        return R.ok(profileApplicationService.updateDimensionOverride(dimensionId, body));
    }

    @GetMapping("/api/v1/profiles/{profileId}/versions")
    public R<?> getVersions(@PathVariable Long profileId,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size) {
        return R.ok(profileApplicationService.getVersions(profileId, page, size));
    }

    @GetMapping("/api/v1/profiles/{profileId}/radar")
    public R<?> getRadarData(@PathVariable Long profileId) {
        return R.ok(profileApplicationService.getRadarData(profileId));
    }

    // ========== Students ==========

    @GetMapping("/api/v1/students")
    public R<?> listStudents(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Long courseId,
                             @RequestParam(required = false) String keyword) {
        return R.ok(profileApplicationService.listProfiles(page, size, courseId, keyword));
    }

    // ========== Internal ==========

    @PostMapping("/internal/v1/profiles/events")
    public R<?> handleProfileEvent(@RequestBody Map<String, Object> body) {
        return R.ok(profileApplicationService.handleProfileEvent(body));
    }
}
