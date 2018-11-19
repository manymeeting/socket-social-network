package com.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9091);
            System.out.println("Server started, listening on 9091");
            while(true){
                Socket clientSocket = serverSocket.accept();
                ConnectionThread notificationThread = new ConnectionThread(clientSocket);
                notificationThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
