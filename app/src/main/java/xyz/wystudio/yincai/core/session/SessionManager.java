package xyz.wystudio.yincai.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import xyz.wystudio.yincai.data.dto.LoginResponse;

/** 仅保存后端会话，不保存任何上游模型配置。 */
public final class SessionManager {
    private static final String PREFS = "student_session";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String USER_ID = "user_id";
    private static final String USERNAME = "username";
    private static final String ROLE_CODE = "role_code";
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    public synchronized void save(LoginResponse response) {
        preferences.edit()
                .putString(ACCESS_TOKEN, response.getAccessToken())
                .putString(REFRESH_TOKEN, response.getRefreshToken())
                .putString(USER_ID, response.getUserId())
                .putString(USERNAME, response.getUsername())
                .putString(ROLE_CODE, response.getRoleCode())
                .apply();
    }
    public synchronized String getAccessToken() { return preferences.getString(ACCESS_TOKEN, null); }
    public synchronized String getRefreshToken() { return preferences.getString(REFRESH_TOKEN, null); }
    public synchronized String getUserId() { return preferences.getString(USER_ID, null); }
    public synchronized String getUsername() { return preferences.getString(USERNAME, null); }
    public synchronized String getRoleCode() { return preferences.getString(ROLE_CODE, null); }
    public synchronized boolean hasSession() { return getAccessToken() != null && getRefreshToken() != null; }
    public synchronized boolean isStudent() { return "STUDENT".equalsIgnoreCase(getRoleCode()); }
    public synchronized void clear() { preferences.edit().clear().apply(); }
}
