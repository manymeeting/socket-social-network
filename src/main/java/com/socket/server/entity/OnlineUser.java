package com.socket.server.entity;

import com.socket.server.thread.SlaveThread;
import com.socket.totp.TotpServer;

import java.io.IOException;
import java.net.Socket;

public class OnlineUser {
    private Socket notiSocket;
    private TotpServer notiTotp;
    private SlaveThread slaveThread;
    private String token;
    private String userName;

    public OnlineUser(SlaveThread slaveThread, Socket notiSocket) {
        this.notiSocket = notiSocket;
        this.slaveThread = slaveThread;
        this.notiTotp = new TotpServer(notiSocket);
    }

    /**
     * Call this function to set a user offline.
     * This function closes socket and notification TOTP for current user
     */
    public void goOffLine() {
        try {
            this.notiTotp.close();
            this.notiSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public TotpServer getNotiTotp() { return notiTotp; }

    public void setToken(String token) {
        this.token = token;
    }
    public String getUserName() {return this.userName;}
    public void setUserName(String userName) {this.userName = userName;}
}
