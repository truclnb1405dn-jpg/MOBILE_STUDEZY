package com.example.studezy.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/login/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("api/register/")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);
}
