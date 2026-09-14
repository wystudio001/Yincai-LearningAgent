package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface ExerciseApi {
    @GET("api/v1/exercises") Call<ApiResponse<List<Map<String, Object>>>> list();
    @GET("api/v1/exercises/{id}") Call<ApiResponse<Map<String, Object>>> detail(@Path("id") String id);
    @POST("api/v1/exercises/{id}/start") Call<ApiResponse<Map<String, Object>>> start(@Path("id") String id);
    @POST("api/v1/exercises/{id}/answers") Call<ApiResponse<Void>> answer(@Path("id") String id, @Body Map<String, Object> body);
    @POST("api/v1/exercises/{id}/submit") Call<ApiResponse<Map<String, Object>>> submit(@Path("id") String id);
    @GET("api/v1/exercises/{id}/result") Call<ApiResponse<Map<String, Object>>> result(@Path("id") String id);
    @GET("api/v1/student/wrong-questions") Call<ApiResponse<List<Map<String, Object>>>> wrongQuestions();
}
