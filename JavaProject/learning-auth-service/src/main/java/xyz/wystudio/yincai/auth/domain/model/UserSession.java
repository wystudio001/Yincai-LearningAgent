package xyz.wystudio.yincai.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSession {
    private Long userId;
    private String deviceId;
    private String refreshToken;
}
