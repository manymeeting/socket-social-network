package com.socket.server.entity;

import com.socket.server.thread.SlaveThread;

import java.net.Socket;

public class OnlineUser {
    private Socket notiSocket;
    private SlaveThread slaveThread;
    private String token;
    private String userName;

    public OnlineUser(SlaveThread slaveThread, Socket notiSocket) {
        this.notiSocket = notiSocket;
        this.slaveThread = slaveThread;
    }
    public Socket getNotiSocket() {
        return this.notiSocket;
    }
    public SlaveThread getSlaveThread() {
        return this.slaveThread;
    }
    public String getToken() {
        return this.token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getUserName() {return this.userName;}
    public void setUserName(String userName) {this.userName = userName;}
}
