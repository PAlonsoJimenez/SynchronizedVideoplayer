package com.example.demo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class Room {
    private final User creator;
    private double videoDuration;
    private final HashSet<User> members;
    private final String roomId;
    private final ArrayList<String> videoControllerSubscribers;
    private final ArrayList<String> roomInfoSubscribers;

    public Room(User creator, double videoDuration) {
        this.creator = creator;
        this.videoDuration = videoDuration;
        members = new HashSet<>();
        roomId = createRoomId();
        videoControllerSubscribers = new ArrayList<>();
        roomInfoSubscribers = new ArrayList<>();
    }

    public boolean addMember(User newMember){
        return members.add(newMember);
    }

    public User getCreator() {
        return creator;
    }

    public double getVideoDuration(){
        return videoDuration;
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

    public boolean addVideoControllerSubscriber(String connectionId){
        if(videoControllerSubscribers.contains(connectionId)){
            return false;
        }else{
            videoControllerSubscribers.add(connectionId);
            return true;
        }
    }

    public boolean removeVideoControllerSubscriber(String connectionId){
        return videoControllerSubscribers.remove(connectionId);
    }

    public boolean isVideoControllerSubscriber(String connectionId){
        return videoControllerSubscribers.contains(connectionId);
    }

    public boolean addRoomInfoSubscriber(String connectionId){
        if(roomInfoSubscribers.contains(connectionId)){
            return false;
        }else{
            roomInfoSubscribers.add(connectionId);
            return true;
        }
    }

    public boolean removeRoomInfoSubscriber(String connectionId){
        return roomInfoSubscribers.remove(connectionId);
    }

    public boolean isRoomInfoSubscriber(String connectionId){
        return roomInfoSubscribers.contains(connectionId);
    }
}
