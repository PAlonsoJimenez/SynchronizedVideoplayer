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

        //Documentation said: "When the client is connected to the server, it can send STOMP messages using the send() method."
        //So it seems you need to be connected to send message with send();
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
        //TODO: change this to full directions (/roomInfoController/change/{roomId})
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
                //TODO: this better:
            case "user":
                return userPrivateInfoSubscribingAttempt(headerAccessor, message);
            default:
                return null;
        }
    }

    private Message<?> userPrivateInfoSubscribingAttempt(StompHeaderAccessor headerAccessor, Message<?> message) {
        //TODO: send userId in Stomp headers in the js
        return message;
    }

    private Message<?> videoControllerSubscribingAttempt(StompHeaderAccessor headerAccessor, String[] destinationPages, Message<?> message) {
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        if(userId == null) return null;
        return ((userId == null) ? null : message);
    }

    private Message<?> roomInfoControllerSubscribingAttempt(StompHeaderAccessor headerAccessor, String[] destinationPages, Message<?> message) {
        //TODO: I'm doing the validations twice, once here and again in the joinRoom method...
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        String userName = validateUserNameInHeaderAccessor(headerAccessor);
        if(userId == null || userName == null) return null;
        String roomId = destinationPages[3];
        //TODO: Check if roomId is valid (check if a room with that id exist)

        /*
        boolean successfullyJoin = roomController.joinRoom(userId, userName, roomId, userController);
        return ((successfullyJoin) ? message : null);
        */
        return message;
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

    private String validateUserNameInHeaderAccessor(StompHeaderAccessor headerAccessor) {
        if(!headerAccessor.containsNativeHeader("userName")){
            //TODO: log error
            return null;
        }
        String userName = headerAccessor.getFirstNativeHeader("userName");
        userName = userController.validateUserName(userName);
        headerAccessor.setNativeHeader("userName", userName);
        return userName;
    }
}
