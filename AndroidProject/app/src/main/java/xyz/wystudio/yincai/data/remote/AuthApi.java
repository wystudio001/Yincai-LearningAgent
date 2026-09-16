package xyz.wystudio.yincai.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import xyz.wystudio.yincai.core.common.ApiResponse;
import xyz.wystudio.yincai.data.dto.LoginRequest;
import xyz.wystudio.yincai.data.dto.LoginResponse;
import xyz.wystudio.yincai.data.dto.RefreshTokenRequest;

import java.util.Map;

public interface AuthApi {
    @POST("api/v1/auth/login") Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);
    @POST("api/v1/auth/refresh") Call<ApiResponse<LoginResponse>> refresh(@Body RefreshTokenRequest request);
    @POST("api/v1/auth/logout") Call<ApiResponse<Void>> logout();
    @GET("api/v1/auth/me") Call<ApiResponse<Map<String, Object>>> currentUser();
}
