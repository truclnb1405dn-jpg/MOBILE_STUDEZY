package com.example.studezy.api;

public class DeadlineModel {
    private String title;
    private String remaining_text;
    private boolean is_urgent;

    public String getTitle() { return title; }
    public String getRemainingText() { return remaining_text; }
    public boolean isUrgent() { return is_urgent; }
}