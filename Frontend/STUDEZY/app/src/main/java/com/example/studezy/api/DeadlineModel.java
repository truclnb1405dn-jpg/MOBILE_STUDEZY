package com.example.studezy.api;

import com.google.gson.annotations.SerializedName;

public class DeadlineModel {
    private int id;
    private String title;

    // Gắn thẻ để Gson tự động khớp với dữ liệu từ Django gửi về
    @SerializedName("remaining_text")
    private String remaining_text;

    @SerializedName("is_urgent")
    private boolean is_urgent;

    @SerializedName("is_completed")
    private boolean isCompleted;

    // --- GETTER & SETTER ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getRemainingText() {
        return remaining_text;
    }

    public boolean isUrgent() {
        return is_urgent;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }
}