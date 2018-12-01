package com.socket.server;

import com.socket.server.dao.UserDao;
import com.socket.server.entity.AppUser;
import com.socket.server.entity.NotificationMessage;
import com.socket.server.entity.OnlineUser;
import com.socket.server.thread.SlaveThread;
import com.socket.server.thread.NotificationThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerApp {

    private ServerSocket serverSocket = null;
    private ServerSocket serverNotiSocket = null;
    private static final int DEFAULT_PORT = 9091;
    private static final int NOTIFICATION_PORT = 9092;
    public volatile Set<SlaveThread> slaveThreadSet;
    public volatile Map<String, OnlineUser> onlineUsersMap;
    public volatile Queue<NotificationMessage> messagesToSendQueue;
    public static final UserDao USER_DAO = new UserDao();

    public static void main(String[] args) {
        ServerApp app = new ServerApp();
        app.start();
    }

    private void start() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("Server started, listening on " + DEFAULT_PORT);
            serverNotiSocket = new ServerSocket(NOTIFICATION_PORT);
            slaveThreadSet = new HashSet<>();
            onlineUsersMap = new HashMap<>();
            messagesToSendQueue = new ConcurrentLinkedQueue<>();
        } catch (IOException e) {
            e.printStackTrace();
        }


        NotificationThread notificationThread = new NotificationThread(this);
        notificationThread.start();

        while (true) {
            try {
                // Listen for clients service connection request
                Socket clientSocket = serverSocket.accept();
                // Listen for client notification connection request
                Socket clientNotiSocket = serverNotiSocket.accept();

                SlaveThread slaveThread = new SlaveThread(this, clientSocket, clientNotiSocket);
                slaveThread.start();
                slaveThreadSet.add(slaveThread);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void addMessage(NotificationMessage notificationMessage) {
        if (notificationMessage != null) {
            messagesToSendQueue.offer(notificationMessage);
        }
    }

    public void sendMsgToDB(String message, Date timeStamp) {
            USER_DAO.addMsgToDB(message, timeStamp);
    }

    public AppUser validate(String readbuf) {
        if (readbuf != null) {
            String[] strings = readbuf.split(" ");
            if (strings.length == 2) {
                String username = strings[0];
                String password = strings[1];
                AppUser user = USER_DAO.validate(username, password);
                return user;
            }
        }
        return null;
    }

    public void addOnlineUser(String token, SlaveThread slaveThread, Socket notiSocket) {
        onlineUsersMap.put(token, new OnlineUser(slaveThread, notiSocket));
    }

    public void removeOnlineUser(String token) {
        if (onlineUsersMap.containsKey(token)) {
            onlineUsersMap.remove(token);
            String message = String.format("User %s has been offline.", token);
            addMessage(new NotificationMessage(NotificationMessage.EMPTY_TOKEN, message));
        }
    }

}
