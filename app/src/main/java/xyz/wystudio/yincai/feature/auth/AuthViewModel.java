package xyz.wystudio.yincai.feature.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.wystudio.yincai.core.common.UiState;
import xyz.wystudio.yincai.core.session.SessionManager;
import xyz.wystudio.yincai.data.dto.LoginResponse;
import xyz.wystudio.yincai.data.repository.AuthRepository;

public final class AuthViewModel extends ViewModel {
    private final MutableLiveData<UiState<LoginResponse>> loginState = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AuthRepository repository;

    public AuthViewModel(AuthRepository repository) { this.repository = repository; }
    public LiveData<UiState<LoginResponse>> loginState() { return loginState; }
    public void login(String username, String password, String deviceId) {
        loginState.postValue(UiState.loading());
        executor.execute(() -> {
            try {
                loginState.postValue(UiState.success(repository.login(username, password, deviceId)));
            } catch (Exception e) {
                String message = e.getMessage() == null ? "登录失败，请稍后重试" : e.getMessage();
                loginState.postValue(UiState.error(message));
            }
        });
    }
    @Override protected void onCleared() { executor.shutdownNow(); }
}
