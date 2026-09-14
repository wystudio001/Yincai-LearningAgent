package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface LearningPathApi {
    @GET("api/v1/student/learning-path/current") Call<ApiResponse<Map<String, Object>>> current();
    @POST("api/v1/student/learning-path/generate") Call<ApiResponse<Map<String, Object>>> generate(@Body Map<String, Object> body);
    @GET("api/v1/student/learning-path/recommendations") Call<ApiResponse<List<Map<String, Object>>>> recommendations();
    @GET("api/v1/learning-paths/{pathId}") Call<ApiResponse<Map<String, Object>>> detail(@Path("pathId") String pathId);
    @GET("api/v1/learning-paths/{pathId}/nodes") Call<ApiResponse<List<Map<String, Object>>>> nodes(@Path("pathId") String pathId);
    @PATCH("api/v1/learning-path-nodes/{nodeId}/status") Call<ApiResponse<Void>> updateStatus(@Path("nodeId") String nodeId, @Body Map<String, Object> body);
    @POST("api/v1/learning-path-nodes/{nodeId}/complete") Call<ApiResponse<Void>> complete(@Path("nodeId") String nodeId);
    @POST("api/v1/learning-path-nodes/{nodeId}/skip") Call<ApiResponse<Void>> skip(@Path("nodeId") String nodeId);
}
