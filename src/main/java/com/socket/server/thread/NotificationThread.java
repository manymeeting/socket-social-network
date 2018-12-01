package com.socket.server.thread;

import com.socket.server.ServerApp;
import com.socket.server.entity.NotificationMessage;
import com.socket.server.entity.OnlineUser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

                // Broadcast notification
                if(message.getToken().equals(NotificationMessage.EMPTY_TOKEN)) {
                    for (OnlineUser onlineUser : server.onlineUsersMap.values()) {
                        try {
                            sendNotification(message.getMessage(), onlineUser.getNotiSocket());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // One-to-one notification
                else if (server.onlineUsersMap.containsKey(message.getToken())) {
                    try {
                        OnlineUser user = server.onlineUsersMap.get(message.getToken());
                        sendNotification(message.getMessage(), user.getNotiSocket());
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

    public void sendNotification(String notificationMsg, Socket clientNotiSocket) throws IOException {
        // Check if client notification socket has been initialized
        if(clientNotiSocket == null) return;

        DataOutputStream dos = new DataOutputStream(clientNotiSocket.getOutputStream());
        dos.writeUTF(notificationMsg);
    }
}
