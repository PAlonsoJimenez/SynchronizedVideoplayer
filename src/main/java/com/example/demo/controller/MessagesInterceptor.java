package com.example.demo.controller;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import java.nio.charset.StandardCharsets;

public class MessagesInterceptor implements ChannelInterceptor {
    private final String INCORRECT_VIDEO_FILE = "INCORRECT_VIDEO_FILE";

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

        //Documentation says: "When the client is connected to the server, it can send STOMP messages using the send() method."
        //So it seems you need to be connected to send message with send();
        if(StompCommand.SEND.equals(headerAccessor.getCommand())){
            /*
            * I end up checking if the user that send the message is a valid user
            * and if is subscribed to the channel they trying to se the message to,
            * because otherwise a user that knows the room code of the channel, even if
            * they are unable to subscribe to that channel without proper permission/validation,
            * they would have been able to send message to that channel.
            */
            return tryingToSend(message);
        }

        if(StompCommand.CONNECT.equals(headerAccessor.getCommand())){
            return tryingToConnect(message);
        }

        if(StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())){
            return tryingToSubscribe(message, channel);
        }

        if(StompCommand.UNSUBSCRIBE.equals(headerAccessor.getCommand())){
            return tryingToUnsubscribe(message);
        }

        if(StompCommand.DISCONNECT.equals(headerAccessor.getCommand())){
            disconnectCleanup(message);
            return message;
        }

        return null;
    }

    private void disconnectCleanup(Message<?> message) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        //TODO THIS METHOD
        String connectionId = headerAccessor.getUser().getName();
    }

    private Message<?> tryingToSend(Message<?> message) {
        //TODO clean this method
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        String destination = headerAccessor.getDestination();
        if(destination == null){
            //TODO: log error
            return null;
        }

        String[] destinationPages = destination.split("/");
        //Todo: Magic number
        //destination pages here: /app/room/{roomId} or /roomInfo/{roomId}
        if(destinationPages.length != 4){
            //TODO: log error
            return null;
        }

        //Todo: Magic number
        String destinationPrefix = destinationPages[2];
        String roomId = destinationPages[3];
        String connectionId = headerAccessor.getUser().getName();

        switch(destinationPrefix){
            case "room":
                return ((roomController.isVideoControllerSubscriber(roomId, connectionId)) ? message :  null);
            case "roomInfo":
                return ((roomController.isRoomInfoSubscriber(roomId, connectionId)) ? message :  null);
            default:
                return null;
        }
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
                return videoControllerSubscribingAttempt(headerAccessor, destinationPages, message, channel);
            case "roomInfoController":
                return roomInfoControllerSubscribingAttempt(headerAccessor, destinationPages, message);
                //TODO: this better:
            case "user":
                return userPrivateInfoSubscribingAttempt(headerAccessor, message);
            default:
                return null;
        }
    }

    private Message<?> tryingToUnsubscribe(Message<?> message) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if(validateUserIdInHeaderAccessor(headerAccessor) == null) return null;

        if(validateUserNameInHeaderAccessor(headerAccessor) == null) return null;

        String channelToUnsubscribe = getChannelToUnsubscribe(headerAccessor);
        if(channelToUnsubscribe == null) return null;

        String roomId = getRoomId(headerAccessor);
        if(roomId == null) return null;

        String connectionId = headerAccessor.getUser().getName();
        boolean unsubscribedSuccessfully = false;
        switch(channelToUnsubscribe){
            case "videoControllerChannel":
                unsubscribedSuccessfully = roomController.unsubscribeFromVideoControllerChannel(roomId, connectionId);
                break;
            case "roomInfoChannel":
                unsubscribedSuccessfully = roomController.unsubscribeFromRoomInfoChannel(roomId, connectionId);
                break;
            default:
                return null;
        }

        return ((unsubscribedSuccessfully) ? message : null);
    }

    private Message<?> userPrivateInfoSubscribingAttempt(StompHeaderAccessor headerAccessor, Message<?> message) {
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        if(userId == null) return null;
        String connectionId = headerAccessor.getUser().getName();
        if(!userController.setUserConnectionId(userId, connectionId)) return null;
        return message;
    }

    private Message<?> videoControllerSubscribingAttempt(StompHeaderAccessor headerAccessor, String[] destinationPages, Message<?> message, MessageChannel channel) {
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        if(userId == null) return null;
        String connectionId = headerAccessor.getUser().getName();

        //TODO: magic number
        String roomId = destinationPages[3];
        if(!validateRoomVideoDuration(roomId, headerAccessor)){
            //Send the user a message
            byte[] userPrivateMessage = INCORRECT_VIDEO_FILE.getBytes(StandardCharsets.UTF_8);
            SimpMessagingTemplate template = new SimpMessagingTemplate(channel);
            template.convertAndSendToUser(connectionId, "/queue/reply", userPrivateMessage);
            return null;
        }

        if(!roomController.addVideoControllerSubscriber(roomId, connectionId)) return null;

        return message;
    }

    private Message<?> roomInfoControllerSubscribingAttempt(StompHeaderAccessor headerAccessor, String[] destinationPages, Message<?> message) {
        //TODO: I'm doing the validations twice, once here and again in the joinRoom method...
        String userId = validateUserIdInHeaderAccessor(headerAccessor);
        String userName = validateUserNameInHeaderAccessor(headerAccessor);
        if(userId == null || userName == null) return null;

        //TODO: magic number
        String roomId = destinationPages[3];
        if(!validateRoomVideoDuration(roomId, headerAccessor)) return null;

        String connectionId = headerAccessor.getUser().getName();
        if(!roomController.addRoomInfoSubscriber(roomId, connectionId)) return null;

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

    private boolean validateRoomVideoDuration(String roomId, StompHeaderAccessor headerAccessor) {
        if(!headerAccessor.containsNativeHeader("videoDuration")){
            //TODO: log error
            return false;
        }

        String videoDurationStringValue = headerAccessor.getFirstNativeHeader("videoDuration");
        double videoDuration = parseVideoDuration(videoDurationStringValue);
        if(videoDuration < 0) return false;

        return roomController.isSameVideoDuration(roomId, videoDuration);
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

    private double parseVideoDuration(String videoDurationStringValue) {
        try{
            double videoDuration = Double.parseDouble(videoDurationStringValue);
            return videoDuration;
        }catch (NumberFormatException | NullPointerException e){
            return -1;
        }
    }

    private String getChannelToUnsubscribe(StompHeaderAccessor headerAccessor) {
        if(!headerAccessor.containsNativeHeader("channelToUnsubscribe")){
            //TODO: log error
            return null;
        }

        String channelToUnsubscribe = headerAccessor.getFirstNativeHeader("channelToUnsubscribe");
        switch(channelToUnsubscribe){
            case "videoControllerChannel":
            case "roomInfoChannel":
                return channelToUnsubscribe;
            default:
                return null;
        }
    }

    private String getRoomId(StompHeaderAccessor headerAccessor) {
        if(!headerAccessor.containsNativeHeader("roomId")){
            //TODO: log error
            return null;
        }

        String roomId = headerAccessor.getFirstNativeHeader("roomId");
        return roomId;
    }
}
