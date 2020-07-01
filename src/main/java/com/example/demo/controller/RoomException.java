package com.example.demo.controller;

public class RoomException extends RuntimeException{
    public static int INVALID_USER_ID = 0;

    public RoomException(int type){
        super(generateMessage(type));
    }

    private static String generateMessage(int type) {
        String exceptionMessage = getExceptionTypeMessage(type) + " exception. Unable to create new Room.";
        return exceptionMessage;
    }

    private static String getExceptionTypeMessage(int type) {
        switch (type){
            case 0:
                return "Invalid userId parameter";
            default:
                return "Room Exception";
        }
    }
}
