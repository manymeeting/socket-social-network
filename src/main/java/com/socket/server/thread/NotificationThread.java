package com.socket.server.thread;

import com.socket.server.ServerApp;
import com.socket.server.entity.NotificationMessage;

import java.io.IOException;

public class NotificationThread extends Thread {

    private final ServerApp server;

    public NotificationThread(ServerApp serverApp) {
        server = serverApp;
    }

    @Override
    public void run() {
        while (true) {
            while (!server.messagesToSendQueue.isEmpty()) {
                NotificationMessage message = server.messagesToSendQueue.poll();
                if (server.onlineThreadMap.containsKey(message.getToken())) {
                    try {
                        server.onlineThreadMap.get(message.getToken()).send(message.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
