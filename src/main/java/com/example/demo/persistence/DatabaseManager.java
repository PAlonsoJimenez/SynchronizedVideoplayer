package com.example.demo.persistence;

import com.example.demo.model.Room;
import com.example.demo.model.User;

import java.util.LinkedList;

public class DatabaseManager {
    //TODO: This is almost a mock class for now. Write this class using a database

    private final static LinkedList<User> activeUsers = new LinkedList<>();
    //TODO: Close a Room when there is no user in it.
    private final static LinkedList<Room> openedRooms = new LinkedList<>();


    public static void addActiveUser(User user){
        activeUsers.add(user);
    }

    public static void addCreatedRoom(Room room){
        openedRooms.add(room);
    }

    public static User getUser(String userId){
        return (activeUsers.stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElse(null));
    }

    public static Room getRoom(String roomId){
        return (openedRooms.stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null));
    }

    public static boolean addUserToRoom(String roomId, User user){
        //TODO: Improve this with a single query to the database from here
        Room room = getRoom(roomId);
        if(room == null) return false;

        Boolean alreadyContainsUser = false;
        for (User roomUser : room.getMembers()) {
            if(roomUser.getUserId().equals(user.getUserId())){
                alreadyContainsUser = true;
                break;
            }
        }

        if(alreadyContainsUser) return false;

        room.addMember(user);
        return true;
    }

    public static boolean setUserConnectionId(String userId, String connectionId) {
        boolean connectionIdSet = false;
        for (User user : activeUsers) {
            if(user.getUserId().equals(userId)){
                user.setConnectionId(connectionId);
                connectionIdSet = true;
                break;
            }
        }

        return connectionIdSet;
    }
}
