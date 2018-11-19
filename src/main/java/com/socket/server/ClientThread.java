package com.socket.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {

    private Socket clientSocket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
    private AppUser user = null;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private void broadcast(String message) {
        for (ClientThread client : ServerApp.onlineClientThreads) {
            if (client != this) {
                try {
                    client.dos.writeUTF(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            dos = new DataOutputStream(clientSocket.getOutputStream());

            do{
                dos.writeUTF("Please login with correct username and password");
                String readbuf = dis.readUTF().trim();
                user = ServerApp.userDao.validate(readbuf);
            } while(user == null);

            dos.writeUTF(String.format("Welcome %s, you have %d unread messages.", user.getUsername(), user.getUnreadMessages().size()));
            for(String unread: user.getUnreadMessages()){
                dos.writeUTF("[Unread] " + unread);
            }
            broadcast(String.format("User %s has been online now.", user.getUsername()));
            while (true) {
                String readbuf = dis.readUTF();
                System.out.println(String.format("received from client %s: %s", clientSocket.getInetAddress().getHostAddress(), readbuf));
                if ("exit".equals(readbuf)) {
                    turnOffline();
                    System.out.println("Closing this connection.");
                    clientSocket.close();
                    System.out.println("Connection closed");
                    break;
                }
                broadcast(String.format("%s posted: %s", user.getUsername(), readbuf));
            }
            ServerApp.onlineClientThreads.remove(this);
            dis.close();
            dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void turnOffline() {
        broadcast(String.format("User %s has been offline."));
        ServerApp.userDao.turnOffline(user);
        try {
            dos.writeUTF("exit");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
