package com.example.demo.controller.endpointsController;

import com.example.demo.model.Message;
import com.example.demo.model.Room;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EndpointsController {

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
    //TODO: change the type of object returned here, instead of Room, create something like 'RoomInfo'
    public Room sendRoomInfo(@DestinationVariable String roomId, Room roomInfo){
        return roomInfo;
    }
}

