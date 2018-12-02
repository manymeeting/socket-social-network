package com.socket.client;

import com.socket.totp.TotpClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class ClientApp {

    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_SERVICE_PORT = 9091;
    private static final int SERVER_NOTIFICATION_PORT = 9092;

    private static Socket clientSocket = null;
    private static TotpClient clientTotp = null;

    private static Scanner scanner = null;
    private static DataInputStream notiDis = null;
    private static DataOutputStream notiDos = null;

    private static boolean serverClosed = false;


    /**
     *
     * The following function is designed that once the result of a previous command is displayed,
     * the user can issue another command. However, notification can be displayed anytime.
     *
     * */
    public static void main(String[] args) {
        try {
            // Initialize socket
            Socket socket = new Socket(SERVER_NAME, SERVER_SERVICE_PORT);
            clientTotp = new TotpClient(socket);

            System.out.println(String.format("Client started, connecting to server %s:%d", SERVER_NAME, SERVER_SERVICE_PORT));

            Socket notiSocket = new Socket(SERVER_NAME, SERVER_NOTIFICATION_PORT);
            notiDis = new DataInputStream(notiSocket.getInputStream());
            notiDos = new DataOutputStream(notiSocket.getOutputStream());
            scanner = new Scanner(System.in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            new NotificationListenerThread().start();

            // Prompt for username and password
            System.out.println("Please input username: ");
            String username = scanner.nextLine().trim();
            System.out.println("Please input password: ");
            String password = scanner.nextLine().trim();
            String token = clientTotp.login(username, password);
            System.out.println("Token: " + token);

            while (!serverClosed) {
                String[] params = scanner.nextLine().trim().split(" ");
                if(params.length < 1) {
                    System.out.println("Error: Invalid command.");
                    continue;
                }
                String command = params[0];

                if(command.equals("send")) {
                    if(params.length < 4) {
                        System.out.println("Error: Wrong parameter format.");
                        continue;
                    }
                    String toUserId = params[1];
                    String msgBoxId = params[2];
                    String message = String.join(" ", Arrays.copyOfRange(params, 3, params.length));
                    clientTotp.send(toUserId, msgBoxId, message);
                    continue;
                }
            }

            notiDis.close();
            notiDos.close();
            clientTotp.close();
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
}
