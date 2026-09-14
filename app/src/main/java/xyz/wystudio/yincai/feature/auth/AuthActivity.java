package xyz.wystudio.yincai.feature.auth;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.UiState;
import xyz.wystudio.yincai.data.repository.AuthRepository;
import xyz.wystudio.yincai.feature.main.MainActivity;

public final class AuthActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView errorText;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progress_login);
        errorText = findViewById(R.id.text_error);
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
}
