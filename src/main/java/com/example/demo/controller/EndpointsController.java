package com.example.demo.controller;

import com.example.demo.model.Message;
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
    public Message send(@DestinationVariable String roomId, Message message) throws Exception {
        return message;
    }
}

