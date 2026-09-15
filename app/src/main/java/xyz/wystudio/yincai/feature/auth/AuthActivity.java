package xyz.wystudio.yincai.feature.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.UiState;
import xyz.wystudio.yincai.data.repository.AuthRepository;
import xyz.wystudio.yincai.feature.main.MainActivity;
import xyz.wystudio.yincai.ui.theme.LineUi;

public final class AuthActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView errorText;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        LineUi.styleScreen(findViewById(android.R.id.content));
        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progress_login);
        errorText = findViewById(R.id.text_error);
        findViewById(R.id.button_backend_settings).setOnClickListener(v -> showBackendUrlDialog());
        AuthRepository repository = new AuthRepository(
                ((YinCaiApplication) getApplication()).apiClient().authApi(),
                ((YinCaiApplication) getApplication()).sessionManager());
        AuthViewModel viewModel = new ViewModelProvider(this, new AuthViewModelFactory(repository)).get(AuthViewModel.class);
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText() == null ? "" : usernameInput.getText().toString().trim();
            String password = passwordInput.getText() == null ? "" : passwordInput.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                errorText.setText(getString(R.string.login_input_required));
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            errorText.setVisibility(View.GONE);
            viewModel.login(username, password, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        });
        viewModel.loginState().observe(this, state -> {
            boolean loading = state != null && state.getStatus() == UiState.Status.LOADING;
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!loading);
            if (state != null && state.getStatus() == UiState.Status.ERROR) {
                errorText.setText(state.getMessage());
                errorText.setVisibility(View.VISIBLE);
            } else if (state != null && state.getStatus() == UiState.Status.SUCCESS) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void showBackendUrlDialog() {
        YinCaiApplication app = (YinCaiApplication) getApplication();
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("例如 http://10.0.2.2:8081/");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(app.sessionManager().getBackendUrl());
        input.setSelectAllOnFocus(true);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        container.setPadding(padding, 0, padding, 0);
        container.addView(input);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("后端地址")
                .setMessage("后续登录和学习请求都会使用此地址")
                .setView(container)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            String value = input.getText() == null ? "" : input.getText().toString().trim();
                            String normalized = normalizeBackendUrl(value);
                            if (normalized == null) {
                                input.setError("请输入有效的 http 或 https 地址");
                                return;
                            }
                            app.sessionManager().setBackendUrl(normalized);
                            Toast.makeText(this, "后端地址已保存", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }));
        dialog.show();
    }

    private String normalizeBackendUrl(String value) {
        if (value.isEmpty()) return null;
        Uri uri = Uri.parse(value);
        String scheme = uri.getScheme();
        if ((scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)))
                || uri.getHost() == null || uri.getHost().isEmpty()) return null;
        if (uri.getQuery() != null || uri.getFragment() != null) return null;
        return value.endsWith("/") ? value : value + "/";
    }
}
