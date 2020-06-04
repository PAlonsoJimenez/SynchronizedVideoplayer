package com.example.demo.model;

public class Message {
    //play or pause
    private String action;
    private double videoTimeStamp;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public double getVideoTimeStamp() {
        return videoTimeStamp;
    }

    public void setVideoTimeStamp(double videoTimeStamp) {
        this.videoTimeStamp = videoTimeStamp;
    }
}
