package com.example.studezy.api;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.DELETE;
import java.util.Map;

public interface ApiService {
    @POST("api/login/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("api/register/")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);

    @GET("api/home-summary/")
    Call<HomeSummaryResponse> getHomeSummary(@Header("Authorization") String token);

    @GET("api/classes-today/")
    Call<List<ClassModel>> getClassesToday(@Header("Authorization") String token);

    @GET("api/top-deadlines/")
    Call<List<DeadlineModel>> getTopDeadlines(@Header("Authorization") String token);

    @GET("api/classes-by-date/")
    Call<List<ClassModel>> getClassesByDate(
            @Header("Authorization") String token,
            @Query("date") String date // Tham số date truyền lên server
    );

    @GET("api/deadlines-by-date/")
    Call<List<DeadlineModel>> getDeadlinesByDate(
            @Header("Authorization") String token,
            @Query("date") String date // Tham số date truyền lên server
    );

    @POST("api/deadlines/{id}/toggle/")
    Call<ResponseBody> toggleDeadline(
            @Header("Authorization") String token,
            @Path("id") int deadlineId,
            @Body UpdateStatusRequest request
    );

    @GET("api/tasks/")
    Call<List<TaskModel>> getTasks(
            @Header("Authorization") String token, // Thêm dòng này để gửi Token
            @Query("search") String keyword,
            @Query("filter") String timeFilter
    );

    @POST("api/tasks/{id}/update-status/")
    Call<Void> updateTaskStatus(
            @Header("Authorization") String token,
            @Path("id") int taskId,
            @Body java.util.Map<String, Integer> body
    );

    @POST("api/tasks/")
    Call<TaskModel> createTask(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );

    @POST("api/tasks/{id}/edit/")
    Call<TaskModel> editTask(
            @Header("Authorization") String token,
            @Path("id") int taskId,
            @Body Map<String, String> body
    );

    @DELETE("api/tasks/{id}/delete/")
    Call<Void> deleteTask(
            @Header("Authorization") String token,
            @Path("id") int taskId
    );
    
   @POST("api/add-class-schedule/")
    Call<RegisterResponse> addClassSchedule(
            @Header("Authorization") String token,
            @Body AddScheduleRequest request
    );

    @GET("api/class-schedules/")
    Call<java.util.List<ScheduleModel>> getAllClassSchedules(@Header("Authorization") String token);

    @DELETE("api/class-schedules/{id}/")
    Call<RegisterResponse> deleteClassSchedule(
            @Header("Authorization") String token,
            @Path("id") int scheduleId
    );

    @PUT("api/class-schedules/{id}/")
    Call<RegisterResponse> updateClassSchedule(
            @Header("Authorization") String token,
            @Path("id") int scheduleId,
            @Body AddScheduleRequest request
    ); 
    @GET("api/profile/")
    Call<ProfileResponse> getProfile(@Header("Authorization") String token);

    @PUT("api/update-profile/")
    Call<UpdateProfileResponse> updateProfile(
            @Header("Authorization") String token,
            @Body UpdateProfileRequest request
    );

    @POST("api/change-password/")
    Call<ChangePasswordResponse> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );

    @GET("api/semester/")
    Call<SemesterModel> getCurrentSemester(@Header("Authorization") String token);

    @POST("api/semester/")
    Call<SemesterModel> createSemester(@Header("Authorization") String token, @Body AddSemesterRequest request);
}
