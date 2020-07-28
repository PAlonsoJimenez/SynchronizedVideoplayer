package com.example.demo.controller;

import com.example.demo.model.StompPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomHandshakeHandler extends DefaultHandshakeHandler{
    /*
    * This class was created exclusively to give every Stomp connection a Principal, that means, an identifier
    * of the connection. This Principal would be use to send messages from the server to the creator (user) of that
    * connection, without needing to establish a new subscription from the user to a reserved endpoint for listening
    * to server message (something like /privateUser/{userId}/messages) and thus, not risking of a middleman listening
    * to the same connection, with an stolen {userId}.
    */

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return new StompPrincipal(UUID.randomUUID().toString());
    }
}
