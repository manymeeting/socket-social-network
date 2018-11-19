package com.socket.server.entity;

public class NotificationMessage {

    private String token;
    private String message;

    public NotificationMessage(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }
}
