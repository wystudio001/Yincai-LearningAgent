package xyz.wystudio.yincai.core.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import xyz.wystudio.yincai.BuildConfig;
import xyz.wystudio.yincai.core.session.SessionManager;

public final class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;
    public AuthInterceptor(SessionManager sessionManager) { this.sessionManager = sessionManager; }
    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder()
                .header("X-Client-Type", BuildConfig.CLIENT_TYPE);
        // Keep endpoint-specific media types such as text/event-stream for SSE calls.
        if (request.header("Accept") == null) {
            builder.header("Accept", "application/json");
        }
        String token = sessionManager.getAccessToken();
        if (token != null && !token.isBlank()) builder.header("Authorization", "Bearer " + token);
        return chain.proceed(builder.build());
    }
}
