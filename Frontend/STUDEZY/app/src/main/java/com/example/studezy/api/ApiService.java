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
}
