package com.example.demo.controller;

public class RoomException extends RuntimeException{
    public static int INVALID_USER_ID = 0;
    public static int INVALID_ROOM_ID = 1;

    public RoomException(int type){
        super(generateMessage(type, ""));
    }

    public RoomException(int type, String additionalMessage){
        super(generateMessage(type, additionalMessage));
    }

    private static String generateMessage(int type, String additionalMessage) {
        String exceptionMessage = getExceptionTypeMessage(type) + " " + additionalMessage;
        return exceptionMessage;
    }

    private static String getExceptionTypeMessage(int type) {
        switch (type){
            case 0:
                return "Invalid userId parameter.";
            case 1:
                return "Invalid roomId parameter.";
            default:
                return "Room Exception.";
        }
    }
}
