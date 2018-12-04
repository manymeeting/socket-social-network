package com.socket.server.thread;

import com.socket.server.ServerApp;
import com.socket.server.entity.NotificationMessage;
import com.socket.server.entity.OnlineUser;
import com.socket.totp.TotpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotificationThread extends Thread {

    private final ServerApp server;
    private static final Logger logger = LogManager.getLogger(NotificationThread.class);

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
                            sendNotification(message.getMessage(), onlineUser);
                            // User last active timestamp is updated every time a notification is sent
                            // to ensure users won't read duplicate notification after a server crash
                            server.updateLastActiveTime(onlineUser.getUserName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // One-to-one notification
                else if (server.onlineUsersMap.containsKey(message.getToken())) {
                    try {
                        OnlineUser onlineUser = server.onlineUsersMap.get(message.getToken());
                        sendNotification(message.getMessage(), onlineUser);
                        server.updateLastActiveTime(onlineUser.getUserName());
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

    public void sendNotification(String notificationMsg, OnlineUser onlineUser) throws IOException {
        TotpServer notiTotp = onlineUser.getNotiTotp();
        List<String> notiMsgs = new ArrayList<>();
        notiMsgs.add(notificationMsg);
        notiTotp.push(notiMsgs);
        if(notiTotp.hasError()) {
            logger.error(notiTotp.getErrorMsg());
        }
    }

}
