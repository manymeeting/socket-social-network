package com.socket.server.entity;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AppUser {

    private String token;
    private String username;
    private String password;
    private String lastActiveTimestamp;
    private List<String> unreadMessages;
    private Date lastOfflineTime;

    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
        token = UUID.randomUUID().toString();
    }

    public String getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }
    public void setLastActiveTimestamp(String lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(List<String> unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public String getToken() {
        return token;
    }

}
