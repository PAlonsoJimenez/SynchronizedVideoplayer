package com.example.demo.controller;

import com.example.demo.model.RoomInfoMessage;
import com.example.demo.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

public class SubscriptionController {
    private final RoomController roomController;

    public SubscriptionController(){
        roomController = new RoomController();
    }

    public void afterSubscribeEventHandler (SessionSubscribeEvent subscribeEvent, SimpMessagingTemplate template){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(subscribeEvent.getMessage());
        String destination = headerAccessor.getDestination();
        if(destination.contains("roomInfoController")){
            RoomInfoMessage allRoomInfoMessage = getAllRoomInfoMessage(destination);
            if(allRoomInfoMessage != null){
                String userSessionId = headerAccessor.getUser().getName();
                template.convertAndSendToUser(userSessionId, "/queue/reply", allRoomInfoMessage);
            }
            //At this point, the user (userId and userName) and room have been validated by the MessageInterceptor
            addUserToRoom(headerAccessor);

            String userId = headerAccessor.getFirstNativeHeader("userId");
            String userName = headerAccessor.getFirstNativeHeader("userName");
            template.convertAndSend(destination, new RoomInfoMessage(RoomInfoMessage.Action.JOINING, new User(userId, userName)));
        }
    }

    public void afterUnsubscribeEventHandler (SessionUnsubscribeEvent unsubscribeEvent, SimpMessagingTemplate template){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(unsubscribeEvent.getMessage());
        String channelToUnsubscribe = headerAccessor.getFirstNativeHeader("channelToUnsubscribe");
        if(channelToUnsubscribe.equals("roomInfoChannel")){
            String roomId = headerAccessor.getFirstNativeHeader("roomId");
            String userId = headerAccessor.getFirstNativeHeader("userId");
            String userName = headerAccessor.getFirstNativeHeader("userName");
            String destination = "/roomInfoController/change/" + roomId;
            template.convertAndSend(destination, new RoomInfoMessage(RoomInfoMessage.Action.LEAVING, new User(userId, userName)));
        }
    }

    private RoomInfoMessage getAllRoomInfoMessage(String destination) {
        //TODO magic number...
        String[] fullEndpointDestination = destination.split("/");
        if(fullEndpointDestination.length != 4){
            //TODO log problem...
            return null;
        }
        String roomId = fullEndpointDestination[3];
        return roomController.getRoomMembersInfo(roomId);
    }

    private void addUserToRoom(StompHeaderAccessor headerAccessor) {
        //At this point, the user (userId and userName) and room have been validated by the MessageInterceptor
        String destination = headerAccessor.getDestination();
        String[] destinationPages = destination.split("/");
        //Todo: Magic number
        if(destinationPages.length != 4){
            //TODO: log error
            return;
        }

        String userId = headerAccessor.getFirstNativeHeader("userId");
        String userName = headerAccessor.getFirstNativeHeader("userName");
        //TODO: magic number...
        String roomId = destinationPages[3];

        roomController.joinRoom(userId, userName, roomId, new UserController());
    }
}
