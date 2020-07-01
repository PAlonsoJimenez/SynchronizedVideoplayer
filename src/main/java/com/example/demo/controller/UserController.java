package com.example.demo.controller;

import java.util.UUID;

public class UserController {

    public String createUserIdJsonFormat(){
        return "{\"id\": \"" + UUID.randomUUID().toString() + "\"}";
    }
}
