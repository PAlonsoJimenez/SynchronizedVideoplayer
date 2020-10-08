package com.example.demo.model;

import java.util.ArrayList;

public class User {
    private final ArrayList<String> videoControllerRoomIds;
    private final ArrayList<String> roomInfoRoomIds;
    private final String userId;
    private String userName;
    private String connectionId;

    public User(String userId, String userName) {
        videoControllerRoomIds = new ArrayList<>();
        roomInfoRoomIds = new ArrayList<>();
        this.userId = userId;
        this.userName = userName;
        connectionId = null;
    }

    public void addVideoControllerRoomId(String roomId){
        if(!videoControllerRoomIds.contains(roomId))
            videoControllerRoomIds.add(roomId);
    }

    public void removeVideoControllerRoomId(String roomId){
        if(videoControllerRoomIds.contains(roomId))
            videoControllerRoomIds.remove(roomId);
    }

    public ArrayList<String> getVideoControllerRoomIds(){
        return videoControllerRoomIds;
    }

    public void addRoomInfoRoomId(String roomId){
        if(!roomInfoRoomIds.contains(roomId))
            roomInfoRoomIds.add(roomId);
    }

    public void removeRoomInfoRoomId(String roomId){
        if(roomInfoRoomIds.contains(roomId))
            roomInfoRoomIds.remove(roomId);
    }

    public ArrayList<String> getRoomInfoRoomIds(){
        return roomInfoRoomIds;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getConnectionId(){
        return connectionId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setConnectionId(String connectionId){
        this.connectionId = connectionId;
    }
}
