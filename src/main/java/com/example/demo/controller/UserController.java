package com.example.demo.controller;

import java.util.LinkedList;
import java.util.UUID;

public class UserController {
    //TODO: Remove this userList, it has to be elsewhere, like in a database.
    private LinkedList<String> userActives;

    public UserController(){
        userActives = new LinkedList<>();
    }

    public String createUserIdJsonFormat(){
        String userID = UUID.randomUUID().toString();
        userActives.add(userID);
        return "{\"id\": \"" + userID + "\"}";
    }

    protected boolean isUserActive(String userId){
        //TODO: This method
        //TODO: this is only a placeholder
        return userActives.contains(userId);
    }
}
