package xyz.wystudio.yincai.data.remote;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface AssessmentApi {
    @GET("api/v1/assessments/latest")
    Call<ApiResponse<Map<String, Object>>> latest();

    @GET("api/v1/assessments/{assessmentId}")
    Call<ApiResponse<Map<String, Object>>> detail(@Path("assessmentId") String assessmentId);

    @POST("api/v1/assessments/generate")
    Call<ApiResponse<Map<String, Object>>> generate(@Body Map<String, Object> body);
}
