package com.example.studezy.api;

public class SemesterModel {
    private String status;
    private int id;
    private String name;
    private String start_date;
    private String end_date;
    private String message;

    public String getStatus() { return status; }
    public String getName() { return name; }
    public String getStartDate() { return start_date; }
    public String getEndDate() { return end_date; }

    public Object getId() {
        return id;
    }
}