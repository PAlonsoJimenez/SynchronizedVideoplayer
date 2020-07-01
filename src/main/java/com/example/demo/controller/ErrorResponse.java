package com.example.demo.controller;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final HttpStatus status;
    private final String error;
    private final String debugMessage;
    private final String timestamp;

    public ErrorResponse(HttpStatus status, String error, String debugMessage){
        this.status = status;
        this.error = error;
        this.debugMessage = debugMessage;
        timestamp = LocalDateTime.now().toString();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
