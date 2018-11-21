package com.socket.server.entity;

import com.socket.server.thread.SlaveThread;

import java.net.Socket;

public class OnlineUser {
    private Socket notiSocket;
    private SlaveThread slaveThread;
    // TODO Add heartbeat socket

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
}
