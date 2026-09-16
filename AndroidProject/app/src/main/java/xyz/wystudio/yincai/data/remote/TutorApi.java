package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Streaming;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface TutorApi {
    @POST("api/v1/tutor/sessions") Call<ApiResponse<Map<String, Object>>> createSession(@Body Map<String, Object> body);
    @GET("api/v1/tutor/sessions") Call<ApiResponse<List<Map<String, Object>>>> sessions();
    @POST("api/v1/tutor/sessions/{sessionId}/messages") Call<ApiResponse<Map<String, Object>>> sendMessage(@Path("sessionId") String sessionId, @Body Map<String, Object> body);
    @Streaming
    @Headers("Accept: text/event-stream")
    @POST("api/v1/tutor/sessions/{sessionId}/messages/stream") Call<ResponseBody> streamMessage(@Path("sessionId") String sessionId, @Body Map<String, Object> body);
    @GET("api/v1/tutor/sessions/{sessionId}/stream") Call<ApiResponse<Map<String, Object>>> stream(@Path("sessionId") String sessionId);
    @GET("api/v1/tutor/sessions/{sessionId}/messages") Call<ApiResponse<List<Map<String, Object>>>> messages(@Path("sessionId") String sessionId);
    @GET("api/v1/tutor/recommended-questions") Call<ApiResponse<List<String>>> recommendedQuestions();
    @POST("api/v1/tutor/messages/{messageId}/feedback") Call<ApiResponse<Void>> feedback(@Path("messageId") String messageId, @Body Map<String, Object> body);
}
