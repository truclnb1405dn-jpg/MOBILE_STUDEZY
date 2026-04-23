package com.example.studezy.api;

public class ClassModel {
    private String subject_name;
    private String room;
    private String time_string;

    // Thêm biến này vào ClassModel
    private String day_of_week;

    // Thêm hàm GET
    public String getDayOfWeek() {
        return day_of_week;
    }

    public String getSubjectName() { return subject_name; }
    public String getRoom() { return room; }
    public String getTimeString() { return time_string; }
}