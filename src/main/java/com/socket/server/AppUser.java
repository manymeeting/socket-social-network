package com.socket.server;

import java.util.Date;
import java.util.List;

public class AppUser {

    private String username;
    private String password;
    private Status status;
    private List<String> unreadMessages;
    private Date lastOfflineTime;

    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
        status = Status.ONLINE;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (this.status == Status.ONLINE && status == Status.OFFLINE) {
            lastOfflineTime = new Date();
        }
        this.status = status;
    }

    enum Status {
        ONLINE, OFFLINE
    }

}
