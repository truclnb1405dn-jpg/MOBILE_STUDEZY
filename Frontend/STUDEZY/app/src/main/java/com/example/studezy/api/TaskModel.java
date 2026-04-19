package com.example.studezy.api;
import com.google.gson.annotations.SerializedName;

public class TaskModel {
    private int id;
    private String title;
    private String description;
    @SerializedName("deadline_date") // Tên trường trả về từ Django
    private String deadlineDate;
    @SerializedName("time_left")
    private String timeLeft;
    private int status; // 0: Đang làm, 1: Hoàn thành

    public TaskModel() {
    }

    // Constructor dùng để tạo mới một Nhiệm vụ (từ Dialog Android)
    public TaskModel(String title, String description, String deadlineDate) {
        this.title = title;
        this.description = description;
        this.deadlineDate = deadlineDate;
        this.status = 0; // Mặc định khi tạo mới là 0 (Đang làm)
    }

    // Getters & Setters
    // ==========================================
    // BỔ SUNG: Hàm getId() để sửa lỗi "cannot find symbol"
    // ==========================================
    public int getId() { return id; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadlineDate() { return deadlineDate; }
    public String getTimeLeft() { return timeLeft; }
    public int getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadlineDate(String deadlineDate) { this.deadlineDate = deadlineDate; }
    public void setTimeLeft(String timeLeft) { this.timeLeft = timeLeft; }
    public void setStatus(int status) { this.status = status; }

    // Giữ nguyên hàm getDate() đã bổ sung trước đó
    public String getDate() {
        return deadlineDate;
    }
}