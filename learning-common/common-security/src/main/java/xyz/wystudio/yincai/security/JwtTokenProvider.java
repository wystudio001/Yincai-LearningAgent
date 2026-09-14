package xyz.wystudio.yincai.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpireMs;
    private final long refreshTokenExpireMs;

    public JwtTokenProvider(
            @Value("${yincai.security.jwt.secret:yincai-zhixue-default-secret-key-for-jwt-signing-2026}") String secret,
            @Value("${yincai.security.jwt.access-token-expire-ms:7200000}") long accessTokenExpireMs,
            @Value("${yincai.security.jwt.refresh-token-expire-ms:604800000}") long refreshTokenExpireMs) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8)));
        this.accessTokenExpireMs = accessTokenExpireMs;
        this.refreshTokenExpireMs = refreshTokenExpireMs;
    }

    public String generateAccessToken(Long userId, Long tenantId, String username, String roleCode, Set<String> permissions) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tenantId", tenantId)
                .claim("username", username)
                .claim("roleCode", roleCode)
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpireMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpireMs))
                .signWith(secretKey)
                .compact();
    }

    public CurrentUser parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Set<String> permissions = parsePermissions(claims.get("permissions"));
            return CurrentUser.builder()
                    .userId(Long.valueOf(claims.getSubject()))
                    .tenantId(parseLong(claims.get("tenantId")))
                    .username(claims.get("username", String.class))
                    .roleCode(claims.get("roleCode", String.class))
                    .permissions(permissions)
                    .build();
        } catch (JwtException e) {
            log.warn("JWT parse failed: {}", e.getMessage());
            return null;
        }
    }

    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Set<String> parsePermissions(Object value) {
        if (value == null) {
            return Set.of();
        }
        if (value instanceof Collection<?> collection) {
            Set<String> permissions = new LinkedHashSet<>();
            collection.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .forEach(permissions::add);
            return permissions;
        }
        return Set.of(String.valueOf(value));
    }
}
