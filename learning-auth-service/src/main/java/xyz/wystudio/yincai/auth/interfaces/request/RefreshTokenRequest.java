package xyz.wystudio.yincai.auth.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "RefreshToken 不能为空")
    private String refreshToken;
}
