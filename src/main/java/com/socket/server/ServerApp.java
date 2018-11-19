package com.socket.server;

import com.socket.server.dao.UserDao;
import com.socket.server.entity.AppUser;
import com.socket.server.entity.NotificationMessage;
import com.socket.server.thread.ClientThread;
import com.socket.server.thread.NotificationThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerApp {

    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;
    private static final int DEFAULT_PORT = 9091;
    public volatile Set<ClientThread> clientThreadSet;
    public volatile Map<String, ClientThread> onlineThreadMap;
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
            clientThreadSet = new HashSet<>();
            onlineThreadMap = new HashMap<>();
            messagesToSendQueue = new ConcurrentLinkedQueue<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationThread notificationThread = new NotificationThread(this);
        notificationThread.start();

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(this, clientSocket);
                clientThread.start();
                clientThreadSet.add(clientThread);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(ClientThread sender, String message) {
        for (ClientThread client : onlineThreadMap.values()) {
            if (client != sender) {
                try {
                    client.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addMessage(NotificationMessage notificationMessage) {
        if (notificationMessage != null) {
            messagesToSendQueue.offer(notificationMessage);
            // TODO store message into database
        }
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

    public void addOnlineThread(String token, ClientThread clientThread) {
        onlineThreadMap.put(token, clientThread);
    }

    public void removeOnlineThread(String token) {
        if (onlineThreadMap.containsKey(token)) {
            broadcast(onlineThreadMap.remove(token), String.format("User %s has been offline."));
        }
    }

    public void addClientThread(ClientThread clientThread) {
        clientThreadSet.add(clientThread);
    }
}
