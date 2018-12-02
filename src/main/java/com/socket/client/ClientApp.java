package com.socket.client;

import com.socket.totp.TotpClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class ClientApp {

    private static final Logger logger = LogManager.getLogger(ClientApp.class);
    private static final String SERVER_NAME = "localhost";
    private static final int SERVER_SERVICE_PORT = 9091;
    private static final int SERVER_NOTIFICATION_PORT = 9092;

    private static Socket socket = null;
    private static Socket notiSocket = null;
    private static TotpClient clientTotp = null;

    private static Scanner scanner = null;
    private static DataInputStream notiDis = null;
    private static DataOutputStream notiDos = null;

    private static volatile boolean isTerminated = false;
    /**
     *
     * The following function is designed that once the result of a previous command is displayed,
     * the user can issue another command. However, notification can be displayed anytime.
     *
     * */
    public static void main(String[] args) {
        try {
            // Initialize socket
            socket = new Socket(SERVER_NAME, SERVER_SERVICE_PORT);
            clientTotp = new TotpClient(socket);

            logger.log(Level.INFO, String.format("Client started, connecting to server %s:%d", SERVER_NAME, SERVER_SERVICE_PORT));

            notiSocket = new Socket(SERVER_NAME, SERVER_NOTIFICATION_PORT);
            notiDis = new DataInputStream(notiSocket.getInputStream());
            notiDos = new DataOutputStream(notiSocket.getOutputStream());
            scanner = new Scanner(System.in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationListenerThread notiThread = new NotificationListenerThread();
        try {
            notiThread.start();

            // Prompt for username and password
            System.out.println("Please input username: ");
            String username = scanner.nextLine().trim();
            System.out.println("Please input password: ");
            String password = scanner.nextLine().trim();
            String token = clientTotp.login(username, password);
            System.out.println("Token: " + token);

            while (scanner.hasNextLine()) {
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
                else if(command.equals("exit")) {
                    if(params.length != 1) {
                        System.out.println("Error: Wrong parameter format.");
                        continue;
                    }
                    clientTotp.goodbye();
                    break;
                }
            }
        } catch (EOFException e) {
            // Server closed
            logger.log(Level.INFO, String.format("Server closed."));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isTerminated = true;
            shutdownAll();
            try {
                notiThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private static void shutdownAll() {
        try {
            clientTotp.close();
            socket.close();
            scanner.close();

            notiDis.close();
            notiDos.close();
            notiSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class NotificationListenerThread extends Thread {
        @Override
        public void run() {
            String notification;
            try {
                while ((notification = notiDis.readUTF()) != null) {
                    System.out.println(notification);
                }
            } catch (IOException e) {
                if(!isTerminated) {
                    // Print out stack if not properly terminated by main thread
                    e.printStackTrace();
                }

            }
        }
    }
}
