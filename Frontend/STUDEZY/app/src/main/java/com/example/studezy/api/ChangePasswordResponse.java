package com.example.studezy.api;

public class ChangePasswordResponse {
    private String status;
    private String message;
    private String token;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}