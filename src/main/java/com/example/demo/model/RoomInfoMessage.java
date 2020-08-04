package com.example.demo.model;

import java.util.ArrayList;
import java.util.Set;

public class RoomInfoMessage {
    //TODO: For the user do something as publicId and privateID
    public enum Action{
        JOINING, LEAVING, FULL_ROOM_INFO
    }

    private final ArrayList<User> users;
    private final Action action;

    public RoomInfoMessage(Action action, User user){
        users = new ArrayList<>();
        this.action = action;
        users.add(user);
    }

    public RoomInfoMessage(Set<User> users){
        this.users = new ArrayList<>();
        this.users.addAll(users);
        action = Action.FULL_ROOM_INFO;
    }

    public Action getAction() {
        return action;
    }

    public ArrayList<User> getUsers() {
        return users;
    }
}
