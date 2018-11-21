package com.socket.server.thread;

import com.socket.server.ServerApp;
import com.socket.server.entity.AppUser;
import com.socket.server.entity.NotificationMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SlaveThread extends Thread {

    private final ServerApp server;
    private Socket clientSocket;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private AppUser user = null;
    public volatile OnlineStatus onlineStatus = OnlineStatus.OFFLINE;

    public SlaveThread(ServerApp server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            dos = new DataOutputStream(clientSocket.getOutputStream());

            // user authentication
            do {
                dos.writeUTF("Please login with correct username and password");
                String readbuf = dis.readUTF().trim();
                user = server.validate(readbuf);
            } while (user == null);

            // User successfully login, update thread to be online
            setOnlineStatus(OnlineStatus.ONLINE);
            sendMessage(user.getToken(), String.format("Welcome %s, you have %d unread messages.", user.getUsername(), user.getUnreadMessages().size()));
            for (String unread : user.getUnreadMessages()) {
                sendMessage(user.getToken(), "[Unread] " + unread);
            }

            server.broadcast(this, String.format("User %s has been online now.", user.getUsername()));

            while (true) {
                String readbuf = dis.readUTF();
                // TODO exit command
                if ("exit".equals(readbuf)) {
                    setOnlineStatus(OnlineStatus.OFFLINE);
                    System.out.println("Closing this connection.");
                    clientSocket.close();
                    System.out.println("Connection closed");
                    break;
                }
                server.broadcast(null, String.format("%s posted: %s", user.getUsername(), readbuf));
            }
            dis.close();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String token, String message) {
        server.addMessage(new NotificationMessage(token, message));
    }

    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;

        if (onlineStatus == OnlineStatus.ONLINE) {
            server.addOnlineThread(user.getToken(), this);
        } else {
            server.removeOnlineThread(user.getToken());

            try {
                dos.writeUTF("exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String message) throws IOException {
        dos.writeUTF(message);
    }

    enum OnlineStatus {
        ONLINE, OFFLINE
    }
}
