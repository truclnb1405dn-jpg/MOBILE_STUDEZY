package com.example.studezy.api;

public class UpdateStatusRequest {
    private boolean is_completed;

    public UpdateStatusRequest(boolean is_completed) {
        this.is_completed = is_completed;
    }
}