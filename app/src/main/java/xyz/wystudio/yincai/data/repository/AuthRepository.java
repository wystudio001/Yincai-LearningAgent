package xyz.wystudio.yincai.data.repository;

import java.io.IOException;

import retrofit2.Response;
import xyz.wystudio.yincai.core.common.ApiException;
import xyz.wystudio.yincai.core.common.ApiResponse;
import xyz.wystudio.yincai.core.session.SessionManager;
import xyz.wystudio.yincai.data.dto.LoginRequest;
import xyz.wystudio.yincai.data.dto.LoginResponse;
import xyz.wystudio.yincai.data.remote.AuthApi;

public final class AuthRepository {
    private final AuthApi api;
    private final SessionManager sessionManager;
    public AuthRepository(AuthApi api, SessionManager sessionManager) { this.api = api; this.sessionManager = sessionManager; }

    public LoginResponse login(String username, String password, String deviceId) throws IOException, ApiException {
        Response<ApiResponse<LoginResponse>> response = api.login(new LoginRequest(username, password, deviceId)).execute();
        LoginResponse result = requireData(response);
        if (!"STUDENT".equalsIgnoreCase(result.getRoleCode())) throw new ApiException("当前账号不是学生账号");
        sessionManager.save(result);
        return result;
    }
    public void logout() throws IOException {
        try { api.logout().execute(); } finally { sessionManager.clear(); }
    }
    private <T> T requireData(Response<ApiResponse<T>> response) throws ApiException {
        if (!response.isSuccessful() || response.body() == null) {
            throw new ApiException(response.code(), null, "网络请求失败，请稍后重试");
        }
        ApiResponse<T> body = response.body();
        if (!body.isSuccess() || body.getData() == null) throw new ApiException(response.code(), body.getCode(), body.getMessage());
        return body.getData();
    }
}
