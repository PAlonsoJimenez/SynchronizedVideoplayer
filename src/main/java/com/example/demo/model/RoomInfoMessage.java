package com.example.demo.model;

public class RoomInfoMessage {
    //TODO: For the user do something as publicId and privateID
    public enum Action{
        JOINING, LEAVING
    }

    private final Action action;
    private final User user;

    public RoomInfoMessage(Action action, User user){
        this.action = action;
        this.user = user;
    }

    public Action getAction() {
        return action;
    }

    public User getUser() {
        return user;
    }
}
