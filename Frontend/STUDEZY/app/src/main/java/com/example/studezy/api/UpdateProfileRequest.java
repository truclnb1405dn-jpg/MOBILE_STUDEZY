package com.example.studezy.api;

public class UpdateProfileRequest {
    private String full_name;
    private String email;
    private String phone_number;

    public UpdateProfileRequest(String full_name, String email, String phone_number) {
        this.full_name = full_name;
        this.email = email;
        this.phone_number = phone_number;
    }
}