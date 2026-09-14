package xyz.wystudio.yincai.core.network;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.wystudio.yincai.core.common.ApiResponse;
import xyz.wystudio.yincai.core.session.SessionManager;
import xyz.wystudio.yincai.data.dto.LoginResponse;
import xyz.wystudio.yincai.data.dto.RefreshTokenRequest;
import xyz.wystudio.yincai.data.remote.AuthApi;

/** 串行刷新 Token，避免并发请求产生刷新风暴。 */
public final class TokenAuthenticator implements Authenticator {
    private final SessionManager sessionManager;
    private final AuthApi refreshApi;
    private final ReentrantLock lock = new ReentrantLock();

    public TokenAuthenticator(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(xyz.wystudio.yincai.BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        refreshApi = retrofit.create(AuthApi.class);
    }

    @Override public Request authenticate(Route route, Response response) {
        if (responseCount(response) >= 2) return null;
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) return null;
        lock.lock();
        try {
            String currentToken = sessionManager.getAccessToken();
            String sentToken = response.request().header("Authorization");
            if (currentToken != null && sentToken != null && !sentToken.endsWith(currentToken)) {
                return response.request().newBuilder().header("Authorization", "Bearer " + currentToken).build();
            }
            retrofit2.Response<ApiResponse<LoginResponse>> refreshed = refreshApi.refresh(new RefreshTokenRequest(refreshToken)).execute();
            if (!refreshed.isSuccessful() || refreshed.body() == null || !refreshed.body().isSuccess()
                    || refreshed.body().getData() == null) {
                sessionManager.clear();
                return null;
            }
            LoginResponse data = refreshed.body().getData();
            sessionManager.save(data);
            return response.request().newBuilder().header("Authorization", "Bearer " + data.getAccessToken()).build();
        } catch (Exception ignored) {
            sessionManager.clear();
            return null;
        } finally { lock.unlock(); }
    }
    private int responseCount(Response response) {
        int count = 1;
        while ((response = response.priorResponse()) != null) count++;
        return count;
    }
}
