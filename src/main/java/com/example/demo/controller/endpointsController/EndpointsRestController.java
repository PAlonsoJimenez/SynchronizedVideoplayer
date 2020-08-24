package com.example.demo.controller.endpointsController;

import com.example.demo.controller.RoomController;
import com.example.demo.controller.UserController;
import com.example.demo.model.Room;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointsRestController {
    UserController userController = new UserController();
    RoomController roomController = new RoomController();

    @GetMapping(value = "/getUserId", produces = "application/json")
    public String getUserId(){
        return userController.createUserIdJsonFormat();
    }

    @PostMapping(value = "/createRoom", produces = "application/json")
    public Room createRoom(@RequestParam(name = "userId") String userId,
                           @RequestParam(name = "userName") String userName,
                           @RequestParam(name = "videoDuration") double videoDuration){
        return roomController.createRoom(userId, userName, videoDuration, userController);
    }
}
