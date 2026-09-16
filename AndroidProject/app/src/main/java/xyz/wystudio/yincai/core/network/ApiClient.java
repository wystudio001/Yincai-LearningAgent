package xyz.wystudio.yincai.core.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.wystudio.yincai.BuildConfig;
import xyz.wystudio.yincai.core.session.SessionManager;
import xyz.wystudio.yincai.data.remote.AuthApi;

public final class ApiClient {
    private final Retrofit retrofit;
    private final AuthApi authApi;

    public ApiClient(SessionManager sessionManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    if (chain.request().url().encodedPath().contains("/messages/stream")) {
                        return chain.withReadTimeout(0, TimeUnit.MILLISECONDS).proceed(chain.request());
                    }
                    return chain.proceed(chain.request());
                })
                .addInterceptor(new BackendUrlInterceptor(sessionManager))
                .addInterceptor(new AuthInterceptor(sessionManager))
                .authenticator(new TokenAuthenticator(sessionManager))
                .addInterceptor(logging)
                .build();
        retrofit = new Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
                .client(client).addConverterFactory(GsonConverterFactory.create()).build();
        authApi = retrofit.create(AuthApi.class);
    }
    public AuthApi authApi() { return authApi; }
    public <T> T create(Class<T> service) { return retrofit.create(service); }
}
