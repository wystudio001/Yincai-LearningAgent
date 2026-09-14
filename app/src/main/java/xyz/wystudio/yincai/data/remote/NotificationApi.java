package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface NotificationApi {
    @GET("api/v1/notifications") Call<ApiResponse<CourseApi.PageData<Map<String, Object>>>> list(@retrofit2.http.Query("pageNo") int pageNo, @retrofit2.http.Query("pageSize") int pageSize);
    @GET("api/v1/notifications/unread-count") Call<ApiResponse<Map<String, Object>>> unreadCount();
    @PATCH("api/v1/notifications/{notificationId}/read") Call<ApiResponse<Void>> markRead(@Path("notificationId") String notificationId);
    @PATCH("api/v1/notifications/read-all") Call<ApiResponse<Void>> markAllRead();
    @DELETE("api/v1/notifications/{notificationId}") Call<ApiResponse<Void>> delete(@Path("notificationId") String notificationId);
}
