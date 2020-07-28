package com.example.demo;

import com.example.demo.controller.CustomHandshakeHandler;
import com.example.demo.controller.MessagesInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/videoController", "/roomInfoController", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/room/{roomId}", "/roomInfo/{roomId}", "/privateUserEndpoint").setHandshakeHandler(new CustomHandshakeHandler());
        registry.addEndpoint("/room/{roomId}", "/roomInfo/{roomId}", "/privateUserEndpoint").setHandshakeHandler(new CustomHandshakeHandler()).withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new MessagesInterceptor());
    }

}