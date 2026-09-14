package xyz.wystudio.yincai.auth.interfaces.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String roleCode;
    private Set<String> permissions;
    private long expiresIn;
}
