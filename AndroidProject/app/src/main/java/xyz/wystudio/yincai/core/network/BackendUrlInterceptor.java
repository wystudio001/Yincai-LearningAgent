package xyz.wystudio.yincai.core.network;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import xyz.wystudio.yincai.core.session.SessionManager;

/** 在请求发送前替换为用户配置的 Java 后端地址。 */
public final class BackendUrlInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public BackendUrlInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl configured = HttpUrl.parse(sessionManager.getBackendUrl());
        if (configured == null || configured.host() == null) return chain.proceed(request);

        HttpUrl original = request.url();
        String prefix = configured.encodedPath();
        if (prefix == null || prefix.isEmpty()) prefix = "/";
        if (!prefix.endsWith("/")) prefix += "/";
        String path = original.encodedPath();
        if (path.startsWith("/")) path = path.substring(1);
        String joinedPath = prefix + path;
        if (!joinedPath.startsWith("/")) joinedPath = "/" + joinedPath;

        HttpUrl target = configured.newBuilder()
                .encodedPath(joinedPath)
                .encodedQuery(original.encodedQuery())
                .build();
        return chain.proceed(request.newBuilder().url(target).build());
    }
}
