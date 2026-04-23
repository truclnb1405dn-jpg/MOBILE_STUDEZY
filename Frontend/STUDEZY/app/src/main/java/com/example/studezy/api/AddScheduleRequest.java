package com.example.studezy.api;

public class AddScheduleRequest {
    private int semester_id;
    private String subject_name;
    private String day_of_week;
    private String start_time;
    private String room;
    private String note;

    public AddScheduleRequest(int semester_id, String subject_name, String day_of_week, String start_time, String room, String note) {
        this.semester_id = semester_id;
        this.subject_name = subject_name;
        this.day_of_week = day_of_week;
        this.start_time = start_time;
        this.room = room;
        this.note = note;
    }
}
