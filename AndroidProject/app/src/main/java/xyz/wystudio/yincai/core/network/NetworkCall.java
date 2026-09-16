package xyz.wystudio.yincai.core.network;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.wystudio.yincai.core.common.ApiResponse;

/** 将 Retrofit 的 HTTP 错误和后端业务错误统一转换为页面可消费的结果。 */
public final class NetworkCall {
    private NetworkCall() { }

    public interface Result<T> {
        void success(T data);
        void failure(String message);
    }

    public static <T> void enqueue(Call<ApiResponse<T>> call, Result<T> result) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override public void onResponse(Call<ApiResponse<T>> c, Response<ApiResponse<T>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    result.failure("网络请求失败（" + response.code() + "）");
                    return;
                }
                ApiResponse<T> body = response.body();
                if (!body.isSuccess()) {
                    result.failure(body.getMessage() == null ? "服务暂时不可用" : body.getMessage());
                    return;
                }
                result.success(body.getData());
            }

            @Override public void onFailure(Call<ApiResponse<T>> c, Throwable throwable) {
                String message = throwable.getMessage();
                result.failure(message == null || message.trim().isEmpty() ? "网络连接失败，请检查网络后重试" : message);
            }
        });
    }
}
