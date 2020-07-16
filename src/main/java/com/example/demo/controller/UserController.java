package com.example.demo.controller;

import com.example.demo.persistence.DatabaseManager;

import java.util.UUID;
import java.util.regex.Pattern;

public class UserController {
    //TODO: Add DatabaseManager object
    //DatabaseManager databaseManager;

    /*
    public UserController(){
        databaseManager = new DatabaseManager();
    }
    */

    public String createUserIdJsonFormat(){
        String userId = UUID.randomUUID().toString();
        DatabaseManager.addActiveUser(userId);
        return "{\"id\": \"" + userId + "\"}";
    }

    protected boolean isUserActive(String userId){
        return DatabaseManager.isUserActive(userId);
    }

    protected boolean isUserIdValid(String userId) {
        Pattern p = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        return p.matcher(userId).matches();
    }

    protected String validateUserName(String userName) {
        userName = userName.trim();
        if(userName.isEmpty()) userName = "XxXlittle_weeperxXx";
        if(userName.length() > 20) userName = userName.substring(0,20);
        return userName;
    }
}
