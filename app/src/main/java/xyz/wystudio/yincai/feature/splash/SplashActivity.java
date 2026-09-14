package xyz.wystudio.yincai.feature.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.feature.auth.AuthActivity;
import xyz.wystudio.yincai.feature.main.MainActivity;

public final class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler(Looper.getMainLooper()).postDelayed(this::route, 450);
    }
    private void route() {
        YinCaiApplication app = (YinCaiApplication) getApplication();
        boolean signedIn = app.sessionManager().hasSession() && app.sessionManager().isStudent();
        startActivity(new Intent(this, signedIn ? MainActivity.class : AuthActivity.class));
        finish();
    }
}
