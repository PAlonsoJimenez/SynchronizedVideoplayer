package com.example.demo.model;

import java.util.HashSet;
import java.util.UUID;

public class Room {
    private final User creator;
    private final HashSet<User> members;
    private final String roomId;

    public Room(User creator) {
        this.creator = creator;
        members = new HashSet<>();
        roomId = createRoomId();
    }

    public boolean addMember(User newMember){
        return members.add(newMember);
    }

    public User getCreator() {
        return creator;
    }

    public HashSet<User> getMembers() {
        return members;
    }

    public String getRoomId() {
        return roomId;
    }

    private String createRoomId() {
        return UUID.randomUUID().toString();
    }
}
