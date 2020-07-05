package com.example.demo.controller.endpointsController;

import com.example.demo.controller.RoomController;
import com.example.demo.controller.UserController;
import com.example.demo.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EndpointsRestController {
    UserController userController = new UserController();
    RoomController roomController = new RoomController();
    @Autowired
    private SimpMessagingTemplate template;

    @GetMapping(value = "/getUserId", produces = "application/json")
    public String getUserId(){
        return userController.createUserIdJsonFormat();
    }

    @PostMapping(value = "/createRoom", produces = "application/json")
    public Room createRoom(@RequestParam(name = "userId") String userId,
                           @RequestParam(name = "userName") String userName){
        return roomController.createRoom(userId, userName);
    }

    @PostMapping(value = "/joinRoom", produces = "text/plain")
    public String joinRoom(@RequestParam(name = "userId") String userId,
                           @RequestParam(name = "userName") String userName,
                           @RequestParam(name = "roomId") String roomId){
        boolean joined = roomController.joinRoom(userId, userName, roomId, userController, template);
        //I think, due to the error throwed inside the roomController, is not possible to return "Unable to Join",
        //which is not a bad thing. (just wanted to remember it)
        return ((joined) ? "User Joined" : "Unable to Join");
    }
}
