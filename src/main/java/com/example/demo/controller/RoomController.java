package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.User;

import java.util.regex.Pattern;

public class RoomController {

    public Room createRoom(String userId, String userName){
        userName = validateUserName(userName);
        if(isUserIdValid(userId)) {
            //TODO: How to deal with stolen uuid...?
            return (new Room(new User(userId, userName)));
        }else{
            throw new RoomException(RoomException.INVALID_USER_ID);
        }
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
