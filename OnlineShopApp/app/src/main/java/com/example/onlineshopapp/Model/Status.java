package com.example.onlineshopapp.Model;

public class Status {
    private String status;
    private String date;

    public Status(String status, String date) {
        this.status = status;
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }
}
