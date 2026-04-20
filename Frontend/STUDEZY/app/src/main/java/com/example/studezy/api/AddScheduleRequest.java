package com.example.studezy.api;

public class AddScheduleRequest {
    private String subject_name;
    private String day_of_week;
    private String start_time;
    private String room;
    private String note;

    public AddScheduleRequest(String subject_name, String day_of_week, String start_time, String room, String note) {
        this.subject_name = subject_name;
        this.day_of_week = day_of_week;
        this.start_time = start_time;
        this.room = room;
        this.note = note;
    }
}