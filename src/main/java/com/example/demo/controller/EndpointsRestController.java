package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointsRestController {

    @PostMapping(value = "/createRoom" , produces = "application/json")
    public Room createRoom(@RequestParam(name = "userId") String userId){
        Room newRoom = createNewRoom(userId);
        return newRoom;
    }


    //For now these methods are in the same class as the endpoints, but they will be moved to a new class.
    private Room createNewRoom(String userId) {
        //TODO: check if the user id is already in use, if is possible to get etc...
        //TODO: Maybe already set an userId from the server side
        userId = userId.trim();
        if(userId.isEmpty()) userId = "XxXlittle_weeperxXx";
        if(userId.length() > 20) userId = userId.substring(0,20);

        User roomCreator = new User(userId);
        //DO I add the user here or not...?
        return new Room(roomCreator);
    }

}
