package xyz.wystudio.yincai;

import android.app.Application;

import xyz.wystudio.yincai.core.network.ApiClient;
import xyz.wystudio.yincai.core.session.SessionManager;

public class YinCaiApplication extends Application {
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Override public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
        apiClient = new ApiClient(sessionManager);
    }
    public SessionManager sessionManager() { return sessionManager; }
    public ApiClient apiClient() { return apiClient; }
}
