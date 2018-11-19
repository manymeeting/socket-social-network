package com.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerApp {

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static final int DEFAULT_PORT = 9091;
    static List<ClientThread> onlineClientThreads;
    static UserDao userDao = new UserDao();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("Server started, listening on " + DEFAULT_PORT);
            onlineClientThreads = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clientSocket);
                onlineClientThreads.add(clientThread);
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
