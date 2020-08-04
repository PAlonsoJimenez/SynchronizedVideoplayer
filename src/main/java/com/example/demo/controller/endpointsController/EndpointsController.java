package com.example.demo.controller.endpointsController;

import com.example.demo.controller.SubscriptionController;
import com.example.demo.model.Message;
import com.example.demo.model.RoomInfoMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Controller
public class EndpointsController {
    @Autowired
    private SimpMessagingTemplate template;
    private final SubscriptionController subscriptionController = new SubscriptionController();

    @GetMapping(value = {"/index", "/", " * "})
    public String index(){
        return "index";
    }

    @MessageMapping("/room/{roomId}")
    @SendTo("/videoController/change/{roomId}")
    public Message send(@DestinationVariable String roomId, Message message){
        return message;
    }

    @MessageMapping("/roomInfo/{roomId}")
    @SendTo("/roomInfoController/change/{roomId}")
    public RoomInfoMessage sendRoomInfo(@DestinationVariable String roomId, RoomInfoMessage update){
        return update;
    }

    /**
     * This method will trigger when a user successfully subscribe to a channel (the MessageInterceptor return a
     * message instead of null) the user wouldn't be able to subscribe if the data sent (roomId, userId or userName)
     * were invalid, so I'm assuming all info is already validated and this method is not doing the validation again.
     * @param sessionSubscribeEvent Session subscribe event
     */
    @EventListener
    private void afterSubscribeEventHandler (SessionSubscribeEvent sessionSubscribeEvent){
        subscriptionController.afterSubscribeEventHandler(sessionSubscribeEvent, template);
    }
}

