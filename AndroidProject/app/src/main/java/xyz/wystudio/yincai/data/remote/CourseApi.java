package xyz.wystudio.yincai.data.remote;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.wystudio.yincai.core.common.ApiResponse;

public interface CourseApi {
    @GET("api/v1/courses") Call<ApiResponse<PageData<Map<String, Object>>>> listCourses(@Query("page") int page, @Query("size") int size, @Query("keyword") String keyword, @Query("status") String status);
    @GET("api/v1/courses/{id}") Call<ApiResponse<Map<String, Object>>> detail(@Path("id") String id);
    @GET("api/v1/courses/{courseId}/chapters") Call<ApiResponse<List<Map<String, Object>>>> chapters(@Path("courseId") String courseId);
    @GET("api/v1/courses/{courseId}/knowledge-points") Call<ApiResponse<List<Map<String, Object>>>> knowledgePoints(@Path("courseId") String courseId);
    @GET("api/v1/courses/{courseId}/materials") Call<ApiResponse<List<Map<String, Object>>>> materials(@Path("courseId") String courseId);

    class PageData<T> {
        private List<T> records;
        private long pageNo;
        private long pageSize;
        private long total;
        private boolean hasNext;
        public List<T> getRecords() { return records; }
        public long getPageNo() { return pageNo; }
        public long getPageSize() { return pageSize; }
        public long getTotal() { return total; }
        public boolean isHasNext() { return hasNext; }
    }
}
