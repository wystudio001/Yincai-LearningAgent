package xyz.wystudio.yincai.auth.interfaces.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wystudio.yincai.auth.application.service.AuthApplicationService;
import xyz.wystudio.yincai.auth.interfaces.request.LoginRequest;
import xyz.wystudio.yincai.auth.interfaces.request.RefreshTokenRequest;
import xyz.wystudio.yincai.auth.interfaces.response.LoginResponse;
import xyz.wystudio.yincai.core.R;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return R.ok(authApplicationService.login(request, httpRequest));
    }

    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return R.ok(authApplicationService.refreshToken(request));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authApplicationService.logout();
        return R.ok();
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(authApplicationService.currentUser());
    }

    @GetMapping("/permissions")
    public R<Set<String>> permissions() {
        return R.ok(authApplicationService.currentPermissions());
    }

    @GetMapping("/captcha")
    public R<Map<String, String>> captcha() {
        return R.ok(authApplicationService.generateCaptcha());
    }

    @PostMapping("/password/change")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        authApplicationService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return R.ok();
    }
}
