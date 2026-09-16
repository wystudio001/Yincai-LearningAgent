package xyz.wystudio.yincai.auth.application.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.wystudio.yincai.auth.domain.model.UserSession;
import xyz.wystudio.yincai.auth.infrastructure.persistence.entity.AuthLoginLogEntity;
import xyz.wystudio.yincai.auth.infrastructure.persistence.entity.AuthUserEntity;
import xyz.wystudio.yincai.auth.infrastructure.persistence.mapper.AuthLoginLogMapper;
import xyz.wystudio.yincai.auth.infrastructure.persistence.mapper.AuthUserMapper;
import xyz.wystudio.yincai.auth.interfaces.request.LoginRequest;
import xyz.wystudio.yincai.auth.interfaces.request.RefreshTokenRequest;
import xyz.wystudio.yincai.auth.interfaces.response.LoginResponse;
import xyz.wystudio.yincai.core.BizException;
import xyz.wystudio.yincai.core.ErrorCode;
import xyz.wystudio.yincai.security.CurrentUser;
import xyz.wystudio.yincai.security.JwtTokenProvider;
import xyz.wystudio.yincai.security.UserContextHolder;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthUserMapper authUserMapper;
    private final AuthLoginLogMapper authLoginLogMapper;
    private final JwtTokenProvider jwtTokenProvider;

    // In-memory session store — in production use MySQL table
    private final Map<String, UserSession> refreshTokenStore = new HashMap<>();

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = resolveClientIp(httpRequest);
        AuthUserEntity user = authUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            recordLoginLog(null, request.getUsername(), request.getClientType(), ipAddress, false, "用户名或密码错误");
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!Objects.equals(user.getPasswordHash(), request.getPassword())) {
            recordLoginLog(user.getId(), user.getUsername(), request.getClientType(), ipAddress, false, "用户名或密码错误");
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            recordLoginLog(user.getId(), user.getUsername(), request.getClientType(), ipAddress, false, "账号已停用");
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号已停用");
        }
        if (isManagementClient(request.getClientType()) && !isManagementRole(user.getRoleCode())) {
            recordLoginLog(user.getId(), user.getUsername(), request.getClientType(), ipAddress, false, "当前账号无管理端登录权限");
            throw new BizException(ErrorCode.FORBIDDEN, "当前账号无管理端登录权限");
        }

        Set<String> permissions = resolvePermissions(user);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getTenantId(), user.getUsername(), user.getRoleCode(), permissions);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        refreshTokenStore.put(refreshToken, new UserSession(user.getId(), request.getDeviceId(), refreshToken));
        recordLoginLog(user.getId(), user.getUsername(), request.getClientType(), ipAddress, true, null);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .roleCode(user.getRoleCode())
                .permissions(permissions)
                .expiresIn(7200)
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        Long userId = jwtTokenProvider.parseUserId(request.getRefreshToken());
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "RefreshToken 无效或已过期");
        }
        UserSession session = refreshTokenStore.get(request.getRefreshToken());
        if (session == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "会话已失效，请重新登录");
        }

        AuthUserEntity user = authUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }

        Set<String> permissions = resolvePermissions(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getTenantId(), user.getUsername(), user.getRoleCode(), permissions);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        refreshTokenStore.remove(request.getRefreshToken());
        refreshTokenStore.put(newRefreshToken, new UserSession(user.getId(), session.getDeviceId(), newRefreshToken));

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .roleCode(user.getRoleCode())
                .permissions(permissions)
                .expiresIn(7200)
                .build();
    }

    public void logout() {
        CurrentUser currentUser = UserContextHolder.get();
        if (currentUser != null) {
            refreshTokenStore.values().removeIf(s -> Objects.equals(s.getUserId(), currentUser.getUserId()));
        }
    }

    public Map<String, Object> currentUser() {
        CurrentUser user = UserContextHolder.get();
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("tenantId", user.getTenantId());
        result.put("roleCode", user.getRoleCode());
        result.put("permissions", user.getPermissions());
        return result;
    }

    public Set<String> currentPermissions() {
        CurrentUser user = UserContextHolder.get();
        return user != null ? user.getPermissions() : Set.of();
    }

    public Map<String, String> generateCaptcha() {
        String code = String.valueOf(1000 + new Random().nextInt(9000));
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='120' height='40'>"
                + "<rect width='120' height='40' fill='#f8fafc'/>"
                + "<text x='18' y='27' font-size='22' font-family='Arial' fill='#0f172a'>" + code + "</text>"
                + "</svg>";
        String image = "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return Map.of("captchaKey", UUID.randomUUID().toString(), "captchaImage", image, "captchaCode", code);
    }

    public void changePassword(String oldPassword, String newPassword) {
        CurrentUser user = UserContextHolder.get();
        if (user == null) throw new BizException(ErrorCode.UNAUTHORIZED);
        AuthUserEntity entity = authUserMapper.selectById(user.getUserId());
        if (entity == null) throw new BizException(ErrorCode.NOT_FOUND);
        if (!Objects.equals(entity.getPasswordHash(), oldPassword)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "原密码错误");
        }
        entity.setPasswordHash(newPassword);
        authUserMapper.updateById(entity);
    }

    private Set<String> resolvePermissions(AuthUserEntity user) {
        // Role permissions are fixed here until the role-permission table is wired into auth.
        return switch (user.getRoleCode()) {
            case "TEACHER" -> Set.of("teacher:*", "course:*", "resource:*", "exam:*", "student:profile");
            case "STUDENT" -> Set.of("student:profile", "student:resource", "student:path", "student:tutor", "student:exam");
            case "ADMIN", "PLATFORM_ADMIN" -> Set.of("admin:*", "ai:*", "system:*");
            default -> Set.of();
        };
    }

    private boolean isManagementClient(String clientType) {
        return clientType != null && Set.of("WEB", "WEB_ADMIN", "ADMIN").contains(clientType.trim().toUpperCase(Locale.ROOT));
    }

    private boolean isManagementRole(String roleCode) {
        return roleCode != null && Set.of("ADMIN", "PLATFORM_ADMIN", "TEACHER").contains(roleCode.trim().toUpperCase(Locale.ROOT));
    }

    private void recordLoginLog(Long userId, String username, String clientType, String ipAddress, boolean success, String failReason) {
        try {
            AuthLoginLogEntity logEntity = new AuthLoginLogEntity();
            logEntity.setUserId(userId);
            logEntity.setUsername(username);
            logEntity.setClientType(normalizeClientType(clientType));
            logEntity.setIpAddress(ipAddress);
            logEntity.setSuccess(success ? 1 : 0);
            logEntity.setFailReason(failReason);
            logEntity.setCreatedAt(LocalDateTime.now());
            authLoginLogMapper.insert(logEntity);
        } catch (Exception ex) {
            log.warn("Failed to record login log for username: {}", username, ex);
        }
    }

    private String normalizeClientType(String clientType) {
        if (clientType == null || clientType.isBlank()) {
            return "UNKNOWN";
        }
        return clientType.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
