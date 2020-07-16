package com.example.demo.controller;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

public class MessagesInterceptor implements ChannelInterceptor {
    private final UserController userController;
    private final RoomController roomController;

    public MessagesInterceptor(){
        userController = new UserController();
        roomController = new RoomController();
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        /*
        * With this I think we get the connect events. The problem is that this method will be called for ALL the
        * message send by any client to be broadcast to other clients, so is more code and more actions before sending
        * a response or broadcasting the message, which I think is worse for a "Real Time" application.
        */

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        //I'm assuming you can send a StompCommand 'SEND' only after being connected and subscribed...
        if(StompCommand.SEND.equals(headerAccessor.getCommand())){
            return message;
        }

        if(StompCommand.CONNECT.equals(headerAccessor.getCommand())){
            return tryingToConnect(message);
        }

        if(StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())){
            return tryingToSubscribe(message, channel);
        }

        if(StompCommand.DISCONNECT.equals(headerAccessor.getCommand())){
            //TODO: clean after disconnecting
            //System.out.println("DISCONNECTING");
        }

        return message;
    }

    private Message<?> tryingToConnect(Message<?> message) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        //The important part is that if I return null, it doesn't connect.
        return ((userId == null) ? null : message);
    }

    private Message<?> tryingToSubscribe(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        String destination = headerAccessor.getDestination();
        if(destination == null){
            //TODO: log error
            return null;
        }

        String[] destinationPages = destination.split("/");
        //Todo: Magic number
        if(destinationPages.length != 4){
            //TODO: log error
            return null;
        }

        //Todo: Magic number
        String destinationPrefix = destinationPages[1];

        switch(destinationPrefix){
            case "videoController":
                return videoControllerSubscribingAttempt(headerAccessor, destinationPages, message);
            case "roomInfoController":
                return roomInfoControllerSubscribingAttempt(headerAccessor, destinationPages, message);
            default:
                return null;
        }
    }

    private Message<?> videoControllerSubscribingAttempt(StompHeaderAccessor headerAccessor, String[] destinationPages, Message<?> message) {
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        if(userId == null) return null;
        return ((userId == null) ? null : message);
    }

    private Message<?> roomInfoControllerSubscribingAttempt(StompHeaderAccessor headerAccessor, String[] destinationPages, Message<?> message) {
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        if(userId == null) return null;
        String roomId = destinationPages[3];
        //TODO: send the userName in the StompHeader in the js and get the userName here.
        boolean successfullyJoin = roomController.joinRoom(userId, "john", roomId, userController);
        return ((successfullyJoin) ? message : null);
    }

    private String validateUserIdInHeaderAccessor(StompHeaderAccessor headerAccessor){
        if(!headerAccessor.containsNativeHeader("userId")){
            //TODO: log error
            return null;
        }

        String userId = headerAccessor.getFirstNativeHeader("userId");
        if(!userController.isUserIdValid(userId)){
            //TODO: log error
            return null;
        }

        if(!userController.isUserActive(userId)){
            //TODO: log error
            return null;
        }
        return userId;
    }
}
