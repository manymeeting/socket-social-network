package com.socket.server.entity;

public class NotificationMessage {

    public static String EMPTY_TOKEN = "";
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
