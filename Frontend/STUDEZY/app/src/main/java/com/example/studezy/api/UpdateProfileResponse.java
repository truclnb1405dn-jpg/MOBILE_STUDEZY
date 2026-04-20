package com.example.studezy.api;

public class UpdateProfileResponse {
    private String status;
    private String message;
    private String full_name;
    private String email;
    private String phone_number;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getFullName() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phone_number;
    }
}