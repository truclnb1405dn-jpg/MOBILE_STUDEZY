package com.example.studezy.api;

public class ScheduleModel {
    private int id;
    private String subject_name;
    private String day_of_week;
    private String time_string;
    private String room;
    private String color_hex;
    private String note;

    // Getters
    public int getId() { return id; }
    public String getSubjectName() { return subject_name; }
    public String getDayOfWeek() { return day_of_week; }
    public String getTimeString() { return time_string; }
    public String getRoom() { return room; }
    public String getColorHex() { return color_hex != null ? color_hex : "#4385F4"; }
    public String getNote() { return note; }
}