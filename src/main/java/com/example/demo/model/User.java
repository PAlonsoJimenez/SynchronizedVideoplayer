package com.example.demo.model;

public class User {
    private final String userId;
    private String userName;
    private String connectionId;

    public User(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.connectionId = null;
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
