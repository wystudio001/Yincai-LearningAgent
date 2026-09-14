package xyz.wystudio.yincai.data.dto;

public class LoginRequest {
    private final String username;
    private final String password;
    private final String captchaKey;
    private final String captchaCode;
    private final String clientType;
    private final String deviceId;

    public LoginRequest(String username, String password, String deviceId) {
        this(username, password, null, null, "ANDROID_STUDENT", deviceId);
    }
    public LoginRequest(String username, String password, String captchaKey, String captchaCode,
                        String clientType, String deviceId) {
        this.username = username;
        this.password = password;
        this.captchaKey = captchaKey;
        this.captchaCode = captchaCode;
        this.clientType = clientType;
        this.deviceId = deviceId;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCaptchaKey() { return captchaKey; }
    public String getCaptchaCode() { return captchaCode; }
    public String getClientType() { return clientType; }
    public String getDeviceId() { return deviceId; }
}
