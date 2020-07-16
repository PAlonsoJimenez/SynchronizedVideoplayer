package com.example.demo.persistence;

import com.example.demo.model.Room;
import com.example.demo.model.User;

import java.util.LinkedList;

public class DatabaseManager {
    //TODO: This is almost a mock class for now. Write this class using a database

    private final static LinkedList<String> activeUsers = new LinkedList<>();
    //TODO: Close a Room when there is no user in it.
    private final static LinkedList<Room> openedRooms = new LinkedList<>();


    public static void addActiveUser(String userId){
        activeUsers.add(userId);
    }

    public static void addCreatedRoom(Room room){
        openedRooms.add(room);
    }

    public static boolean isUserActive(String userId){
        return activeUsers.contains(userId);
    }

    public static Room getRoom(String roomId){
        return (openedRooms.stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null));
    }

    public static boolean addUserToRoom(String roomId, User user){
        //TODO: add user if and only if is not already in the room
        //TODO: Improve this with a single query to the database from here
        getRoom(roomId).addMember(user);
        return true;
    }
}
