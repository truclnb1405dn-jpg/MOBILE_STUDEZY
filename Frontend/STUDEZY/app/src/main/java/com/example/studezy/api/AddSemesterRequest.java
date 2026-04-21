package com.example.studezy.api;

public class AddSemesterRequest {
    private String name;
    private String start_date;
    private String end_date;

    public AddSemesterRequest(String name, String start_date, String end_date) {
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
    }
}