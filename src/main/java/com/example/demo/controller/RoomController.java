package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.RoomInfoMessage;
import com.example.demo.model.User;
import com.example.demo.persistence.DatabaseManager;

public class RoomController {
    //TODO: returning userIds of user inside a room is a mistake. Change that. (maybe inside the userClass, something like not add the id to the json.)
    //TODO: Add DatabaseManager object
    //DatabaseManager databaseManager;

    /*
    public RoomController(){
        databaseManager = new DatabaseManager();
    }
    */

    public Room createRoom(String userId, String userName, double videoDuration, UserController userController){
        userName = userController.validateUserName(userName);
        if(userController.isUserIdValid(userId)) {
            //TODO: How to deal with stolen uuid...?
            Room createdRoom = new Room(new User(userId, userName), videoDuration);
            DatabaseManager.addCreatedRoom(createdRoom);
            return createdRoom;
        }else{
            throw new RoomException(RoomException.INVALID_USER_ID, "Unable to create Room");
        }
    }

    public boolean joinRoom(String userId, String userName, String roomId, UserController userController){
        if(!userController.isUserActive(userId)) return false;

        Room roomToJoin = DatabaseManager.getRoom(roomId);
        if(roomToJoin == null) return false;

        userName = userController.validateUserName(userName);
        //TODO: For the user do something as publicId and privateID
        User joiningUser = new User(userId, userName);
        DatabaseManager.addUserToRoom(roomId, joiningUser);

        return true;
    }

    public boolean addVideoControllerSubscriber(String roomId, String connectionId){
        Room room = DatabaseManager.getRoom(roomId);
        if(room == null) return false;
        return room.addVideoControllerSubscriber(connectionId);
    }

    public boolean addRoomInfoSubscriber(String roomId, String connectionId){
        Room room = DatabaseManager.getRoom(roomId);
        if(room == null) return false;
        return room.addRoomInfoSubscriber(connectionId);
    }

    public RoomInfoMessage getRoomMembersInfo(String roomId){
        Room room = DatabaseManager.getRoom(roomId);
        return new RoomInfoMessage(room.getMembers());
    }

    public boolean isSameVideoDuration(String roomId, double videoDuration){
        Room room = DatabaseManager.getRoom(roomId);
        if(room == null) return false;
        return (room.getVideoDuration() == videoDuration);
    }

    public boolean isVideoControllerSubscriber(String roomId, String connectionId){
        Room room = DatabaseManager.getRoom(roomId);
        if(room == null) return false;
        return room.isVideoControllerSubscriber(connectionId);
    }

    public boolean isRoomInfoSubscriber(String roomId, String connectionId){
        Room room = DatabaseManager.getRoom(roomId);
        if(room == null) return false;
        return room.isRoomInfoSubscriber(connectionId);
    }

    public boolean unsubscribeFromVideoControllerChannel(String roomId, String connectionId){
        Room room = DatabaseManager.getRoom(roomId);
        if(room == null) return false;
        return room.removeVideoControllerSubscriber(connectionId);
    }

    public boolean unsubscribeFromRoomInfoChannel(String roomId, String connectionId){
        Room room = DatabaseManager.getRoom(roomId);
        if(room == null) return false;
        return room.removeRoomInfoSubscriber(connectionId);
    }
}
