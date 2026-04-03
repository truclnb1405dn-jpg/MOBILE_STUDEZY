package com.example.studezy.api;

public class LoginResponse {
    private String status;
    private String message;
    private String token;
    private String full_name;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getFullName() { return full_name; }
}
