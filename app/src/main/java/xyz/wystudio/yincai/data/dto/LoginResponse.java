package xyz.wystudio.yincai.data.dto;

import java.util.Set;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String username;
    private String roleCode;
    private Set<String> permissions;
    private long expiresIn;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRoleCode() { return roleCode; }
    public Set<String> getPermissions() { return permissions; }
    public long getExpiresIn() { return expiresIn; }
}
