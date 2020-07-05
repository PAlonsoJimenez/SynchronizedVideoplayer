package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.LinkedList;
import java.util.regex.Pattern;

public class RoomController {
    //TODO: returning userIds of user inside a room is a mistake. Change that. (maybe inside the userClass, something like not add the id to the json.)
    //TODO: Remove this userList, it has to be elsewhere, like in a database.
    private LinkedList<Room> roomsOpened;

    public RoomController(){
        roomsOpened = new LinkedList<>();
    }

    public Room createRoom(String userId, String userName){
        userName = validateUserName(userName);
        if(isUserIdValid(userId)) {
            //TODO: How to deal with stolen uuid...?
            Room createdRoom = new Room(new User(userId, userName));
            roomsOpened.add(createdRoom);
            return createdRoom;
        }else{
            throw new RoomException(RoomException.INVALID_USER_ID);
        }
    }

    public boolean joinRoom(String userId, String userName, String roomId, UserController userController, SimpMessagingTemplate template){
        if(!userController.isUserActive(userId)) return false;
        Room roomToJoin = roomsOpened.stream().filter(room -> room.getRoomId().equals(roomId)).findFirst().orElse(null);
        if(roomToJoin == null) return false;
        userName = validateUserName(userName);

        roomToJoin.addMember(new User(userId, userName));
        template.convertAndSend("/roomInfoController/change/" + roomId, roomToJoin);
        return true;
    }

    private boolean isUserIdValid(String userId) {
        Pattern p = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        return p.matcher(userId).matches();
    }

    private String validateUserName(String userName) {
        userName = userName.trim();
        if(userName.isEmpty()) userName = "XxXlittle_weeperxXx";
        if(userName.length() > 20) userName = userName.substring(0,20);
        return userName;
    }
}
