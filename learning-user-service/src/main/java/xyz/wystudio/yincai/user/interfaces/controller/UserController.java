package xyz.wystudio.yincai.user.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wystudio.yincai.core.R;
import xyz.wystudio.yincai.user.application.service.UserApplicationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;

    // ==================== User Endpoints ====================

    @PostMapping("/users")
    public R<Map<String, Object>> createUser(@RequestBody Map<String, Object> body) {
        return R.ok(userApplicationService.createUser(body));
    }

    @GetMapping("/users")
    public R<R.PageData> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(userApplicationService.listUsers(page, size, keyword));
    }

    @GetMapping("/users/{id}")
    public R<Map<String, Object>> getUserById(@PathVariable Long id) {
        return R.ok(userApplicationService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    public R<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        userApplicationService.updateUser(id, body);
        return R.ok();
    }

    @PatchMapping("/users/{id}/status")
    public R<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userApplicationService.updateUserStatus(id, body.get("status"));
        return R.ok();
    }

    @DeleteMapping("/users/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userApplicationService.deleteUser(id);
        return R.ok();
    }

    // ==================== User Roles ====================

    @GetMapping("/users/{id}/roles")
    public R<List<String>> getUserRoles(@PathVariable Long id) {
        return R.ok(userApplicationService.getUserRoles(id));
    }

    @PutMapping("/users/{id}/roles")
    public R<Void> updateUserRoles(@PathVariable Long id, @RequestBody List<String> roleCodes) {
        userApplicationService.updateUserRoles(id, roleCodes);
        return R.ok();
    }

    // ==================== Role Endpoints ====================

    @GetMapping("/roles")
    public R<R.PageData> listRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(userApplicationService.listRoles(page, size));
    }

    @PostMapping("/roles")
    public R<Map<String, Object>> createRole(@RequestBody Map<String, Object> body) {
        return R.ok(userApplicationService.createRole(body));
    }

    @PutMapping("/roles/{id}")
    public R<Void> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        userApplicationService.updateRole(id, body);
        return R.ok();
    }

    @DeleteMapping("/roles/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        userApplicationService.deleteRole(id);
        return R.ok();
    }

    // ==================== Permission Endpoints ====================

    @GetMapping("/permissions")
    public R<List<Object>> getPermissionTree() {
        return R.ok(userApplicationService.getPermissionTree());
    }

    @PutMapping("/roles/{id}/permissions")
    public R<Void> updateRolePermissions(@PathVariable Long id, @RequestBody List<String> permissions) {
        userApplicationService.updateRolePermissions(id, permissions);
        return R.ok();
    }
}
