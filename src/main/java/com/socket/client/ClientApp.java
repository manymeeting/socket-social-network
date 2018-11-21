package com.socket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {

    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_SERVICE_PORT = 9091;
    private static final int SERVER_NOTIFICATION_PORT = 9092;

    private static Socket clientSocket = null;

    private static Scanner scanner = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;
    private static DataInputStream notiDis = null;
    private static DataOutputStream notiDos = null;

    private static boolean closed = false;


    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_NAME, SERVER_SERVICE_PORT);
            System.out.println(String.format("Client started, connecting to server %s:%d", SERVER_NAME, SERVER_SERVICE_PORT));
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            Socket notiSocket = new Socket(SERVER_NAME, SERVER_NOTIFICATION_PORT);
            notiDis = new DataInputStream(notiSocket.getInputStream());
            notiDos = new DataOutputStream(notiSocket.getOutputStream());

            scanner = new Scanner(System.in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            new ResponseListenerThread().start();
            new NotificationListenerThread().start();

            while (!closed) {
                dos.writeUTF(scanner.nextLine().trim());
            }
            dos.close();
            dis.close();
            notiDis.close();
            notiDos.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class NotificationListenerThread  extends Thread {
        @Override
        public void run() {
            String notification;
            try {
                while ((notification = notiDis.readUTF()) != null) {
                    System.out.println(notification);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ResponseListenerThread extends Thread {

        @Override
        public void run() {
            String responseLine;
            try {
                while ((responseLine = dis.readUTF()) != null) {
                    System.out.println(responseLine);
                    if ("exit".equals(responseLine))
                        break;
                }
                closed = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
