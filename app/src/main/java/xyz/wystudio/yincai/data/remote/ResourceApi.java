package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface ResourceApi {
    @GET("api/v1/resources") Call<ApiResponse<CourseApi.PageData<Map<String, Object>>>> list(@Query("page") int page, @Query("size") int size, @Query("keyword") String keyword);
    @GET("api/v1/resources/{id}") Call<ApiResponse<Map<String, Object>>> detail(@Path("id") String id);
    @GET("api/v1/resources/{id}/download-url") Call<ApiResponse<Map<String, Object>>> downloadUrl(@Path("id") String id);
    @POST("api/v1/resources/{id}/favorite") Call<ApiResponse<Void>> favorite(@Path("id") String id);
    @DELETE("api/v1/resources/{id}/favorite") Call<ApiResponse<Void>> unfavorite(@Path("id") String id);
    @GET("api/v1/resources/favorites") Call<ApiResponse<List<Map<String, Object>>>> favorites();
    @GET("api/v1/resources/recent") Call<ApiResponse<List<Map<String, Object>>>> recent();
    @GET("api/v1/resources/search") Call<ApiResponse<CourseApi.PageData<Map<String, Object>>>> search(@Query("keyword") String keyword, @Query("page") int page, @Query("size") int size);
}
