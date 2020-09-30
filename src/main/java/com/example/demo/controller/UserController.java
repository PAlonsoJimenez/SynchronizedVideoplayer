package com.example.demo.controller;

import com.example.demo.model.User;
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
        //TODO: Somehow update the userName when the user changes it.
        User newUser = new User(userId, "Anonymous");
        DatabaseManager.addActiveUser(newUser);
        return "{\"id\": \"" + userId + "\"}";
    }

    public boolean isUserActive(String userId){
        User user = DatabaseManager.getUser(userId);
        return (user != null);
    }

    public boolean isUserIdValid(String userId) {
        Pattern p = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        return p.matcher(userId).matches();
    }

    public String validateUserName(String userName) {
        userName = userName.trim();
        if(userName.isEmpty()) userName = "XxXlittle_weeperxXx";
        if(userName.length() > 20) userName = userName.substring(0,20);
        return userName;
    }

    public boolean setUserConnectionId(String userId, String connectionId){
        return DatabaseManager.setUserConnectionId(userId, connectionId);
    }
}
