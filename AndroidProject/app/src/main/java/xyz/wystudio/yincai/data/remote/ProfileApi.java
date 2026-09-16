package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface ProfileApi {
    @GET("api/v1/student/profile") Call<ApiResponse<Map<String, Object>>> current(@Query("studentId") String studentId);
    @POST("api/v1/student/profile/init") Call<ApiResponse<Map<String, Object>>> initialize(@Body Map<String, Object> body);
    @POST("api/v1/student/profile/dialogues") Call<ApiResponse<Map<String, Object>>> dialogue(@Body Map<String, Object> body);
    @GET("api/v1/student/profile/dialogues") Call<ApiResponse<List<Map<String, Object>>>> dialogues(@Query("studentId") String studentId, @Query("courseId") String courseId, @Query("limit") int limit);
    @GET("api/v1/profiles/{profileId}/dimensions") Call<ApiResponse<List<Map<String, Object>>>> dimensions(@Path("profileId") String profileId);
    @GET("api/v1/profiles/{profileId}/radar") Call<ApiResponse<Map<String, Object>>> radar(@Path("profileId") String profileId);
}
