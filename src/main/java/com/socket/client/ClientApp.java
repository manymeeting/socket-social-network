package com.socket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientApp {

    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_PORT = 9091;

    private static Socket clientSocket = null;

    private static Scanner scanner = null;
    private static DataInputStream dis = null;
    private static DataOutputStream dos = null;

    private static boolean closed = false;


    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_NAME, SERVER_PORT);
            System.out.println(String.format("Client started, connecting to server %s:%d", SERVER_NAME, SERVER_PORT));
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            scanner = new Scanner(System.in);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            new PullThread().start();

            while (!closed) {
                dos.writeUTF(scanner.nextLine().trim());
            }
            dos.close();
            dis.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PullThread extends Thread {

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
