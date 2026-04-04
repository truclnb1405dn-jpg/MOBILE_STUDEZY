package com.example.studezy.api;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

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
}
