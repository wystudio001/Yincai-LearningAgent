package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import xyz.wystudio.yincai.core.common.ApiResponse;

/** 资源生成任务接口，任务执行和模型调用均由 Java 后端负责。 */
public interface GenerationApi {
    @POST("api/v1/resource-generation-tasks")
    Call<ApiResponse<Map<String, Object>>> create(@Body Map<String, Object> body);

    @GET("api/v1/resource-generation-tasks/{id}")
    Call<ApiResponse<Map<String, Object>>> detail(@Path("id") String id);

    @GET("api/v1/resource-generation-tasks/{id}/steps")
    Call<ApiResponse<List<Map<String, Object>>>> steps(@Path("id") String id);

    @GET("api/v1/resource-generation-tasks/{id}/stream")
    Call<ApiResponse<Map<String, Object>>> stream(@Path("id") String id);

    @POST("api/v1/resource-generation-tasks/{id}/cancel")
    Call<ApiResponse<Map<String, Object>>> cancel(@Path("id") String id);

    @POST("api/v1/resource-generation-tasks/{id}/retry")
    Call<ApiResponse<Map<String, Object>>> retry(@Path("id") String id);

    @GET("api/v1/resource-generation-tasks/{id}/outputs")
    Call<ApiResponse<List<Map<String, Object>>>> outputs(@Path("id") String id);
}
