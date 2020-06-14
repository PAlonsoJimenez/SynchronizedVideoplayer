package com.example.demo.model;

public class Message {
    //play or pause
    private String senderId;
    private String action;
    private double videoTimeStamp;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

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
