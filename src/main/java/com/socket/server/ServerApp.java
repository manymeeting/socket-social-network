package com.socket.server;

import com.socket.server.dao.MessageDao;
import com.socket.server.dao.UserDao;
import com.socket.server.entity.AppUser;
import com.socket.server.entity.Message;
import com.socket.server.entity.NotificationMessage;
import com.socket.server.entity.OnlineUser;
import com.socket.server.thread.NotificationThread;
import com.socket.server.thread.SlaveThread;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ServerApp {

    private static final Logger logger = LogManager.getLogger(ServerApp.class);
    private ServerSocket serverSocket = null;
    private ServerSocket serverNotiSocket = null;
    private static final int DEFAULT_PORT = 9091;
    private static final int NOTIFICATION_PORT = 9092;
    public volatile Map<String, OnlineUser> onlineUsersMap;
    public volatile Queue<NotificationMessage> messagesToSendQueue;
    public static final UserDao USER_DAO = new UserDao();
    public static final MessageDao MESSAGE_DAO = new MessageDao();

    public static void main(String[] args) {
        ServerApp app = new ServerApp();
        app.start();
    }

    private void start() {
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            logger.log(Level.INFO, "Server started, listening on " + DEFAULT_PORT);
            serverNotiSocket = new ServerSocket(NOTIFICATION_PORT);
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

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNotification(NotificationMessage notificationMessage) {
        if (notificationMessage != null) {
            messagesToSendQueue.offer(notificationMessage);
        }
    }

    public void addMessage(Message message) {
        MESSAGE_DAO.saveMessage(message);
    }

    public void updateLastActiveTime(String username) {
        USER_DAO.updateLastActiveTime(username);
    }

    public boolean validateToken(String token) {
        return token != null && onlineUsersMap.containsKey(token);
    }

    public AppUser userLogin(String username, String password) {
        if (username != null && password != null) {
            AppUser user = USER_DAO.validate(username, password);
            return user;
        }
        return null;
    }

    public void addOnlineUser(AppUser user, SlaveThread slaveThread, Socket notiSocket) {
        OnlineUser onlineUser = new OnlineUser(slaveThread, notiSocket);
        onlineUser.setToken(user.getToken());
        onlineUser.setUserName(user.getUsername());
        onlineUsersMap.put(user.getToken(), onlineUser);
    }

    /**
     * A background is that all usernames are unique and each user can login multiple times.
     * Therefore, we simply remove duplicate names from the online users list and
     * return the list as result.
     *
     * @return List of distinct usernames
     */
    public List<String> getOnlineUsers() {
        return onlineUsersMap.values().stream()
                .map(OnlineUser::getUserName).distinct().collect(Collectors.toList());
    }

    public List<String> getMessagesOnUser(String userId) {
        return MESSAGE_DAO.getMessageOnUser(userId).stream().map(message -> {
            return String.format("%s post to %s: %s", message.getFromUesrId(),
                    message.getToUserId(), message.getContent());
        }).collect(Collectors.toList());
    }

    public void removeOnlineUser(String token) {
        if (onlineUsersMap.containsKey(token)) {
            OnlineUser user = onlineUsersMap.get(token);
            user.goOffLine();
            onlineUsersMap.remove(token);

            // Broadcast notification
            String userName = user.getUserName();
            String message = String.format("User %s has been offline.", userName);
            addNotification(new NotificationMessage(NotificationMessage.EMPTY_TOKEN, message));
        }
    }

}
