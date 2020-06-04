package com.example.demo.model;

import java.util.HashSet;

public class Room {
    private final User creator;
    private final HashSet<User> members;
    private final long roomId;

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

    public long getRoomId() {
        return roomId;
    }

    private long createRoomId() {
        //TODO: create an unique room id
        return 7;
    }
}
