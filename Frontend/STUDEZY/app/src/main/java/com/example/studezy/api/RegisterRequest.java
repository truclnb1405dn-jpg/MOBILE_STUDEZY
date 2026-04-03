package com.example.studezy.api;

public class RegisterRequest {
    private String username;
    private String password;
    private String full_name;
    private String email;

    public RegisterRequest(String username, String password, String full_name, String email) {
        this.username = username;
        this.password = password;
        this.full_name = full_name;
        this.email = email;
    }
}