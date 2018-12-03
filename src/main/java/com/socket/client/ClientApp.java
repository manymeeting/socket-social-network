package com.socket.client;

import com.socket.totp.TotpClient;
import com.socket.totp.TotpCmd;
import com.socket.totp.TotpStatus;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static TotpClient notiTotp = null;

    private static Scanner scanner = null;

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
            notiTotp = new TotpClient(notiSocket);

            scanner = new Scanner(System.in);
        } catch (Exception e) {
            logger.error("Could not connect to server.");
            System.exit(0);
        }

        NotificationListenerThread notiThread = new NotificationListenerThread();
        try {
            notiThread.start();

            boolean isLoginSucess = false;
            // Prompt for username and password until a successful login
            while(!isLoginSucess) {
                System.out.println("Please input username: ");
                String username = scanner.nextLine().trim();
                System.out.println("Please input password: ");
                String password = scanner.nextLine().trim();
                String token = clientTotp.login(username, password);
                if(hasClientTotpError()) {
                    System.out.println(clientTotp.getErrorMsg());
                    continue;
                }
                System.out.println("Token: " + token);
                isLoginSucess = true;
            }
            printHelp();
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
                    hasClientTotpError();
                    continue;
                }
                else if(command.equals("exit")) {
                    if(params.length != 1) {
                        System.out.println("Error: Wrong parameter format.");
                        continue;
                    }
                    clientTotp.goodbye();
                    hasClientTotpError();
                    break;
                }
                else if(command.equals("users")) {
                    if(params.length != 1) {
                        System.out.println("Error: Wrong parameter format.");
                        continue;
                    }
                    String[] allOnlineUsers = clientTotp.retrieveFriendList();
                    if(hasClientTotpError()) {
                        continue;
                    }
                    for (String userName : allOnlineUsers) {
                        System.out.println(userName);
                    }
                    continue;
                }
                else if(command.equals("wall")) {
                    if(params.length < 2) {
                        System.out.println("Error: Wrong parameter format.");
                        continue;
                    }
                    String userId = params[1];
                    String boxId = params.length >= 3 ? params[2] : "wall";
                    String wallMessages[] = clientTotp.retrieve(userId, boxId);
                    if(hasClientTotpError()) {
                        continue;
                    }
                    for (String wallMsg : wallMessages) {
                        System.out.println(wallMsg);
                    }
                    continue;
                }
                else if(command.equals("help")) {
                    printHelp();
                    continue;
                }
                else {
                    System.out.println("Invalid command, type <help> for instruction");
                    continue;
                }
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, String.format("Internal error. See stack trace for more information."));
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
    }

    /**
     *
     * @return true: If error exists, otherwise return false.
     *
     */
    private static boolean hasClientTotpError() {
        if(clientTotp.hasError()) {
            logger.error(clientTotp.getErrorMsg());
            return true;
        }
        return false;
    }

    private static void shutdownAll() {
        try {
            scanner.close();
            clientTotp.close();
            socket.close();
            notiTotp.close();
            notiSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println(" ----------------------------------");
        System.out.println("|                                  |");
        System.out.println("|       TOTP Social Network        |");
        System.out.println("|                                  |");
        System.out.println("|          Instructions            |");
        System.out.println("| 1. Send: send <User> <Box> <Msg> |");
        System.out.println("| 2. Wall: wall <User>             |");
        System.out.println("| 3. Users: users                  |");
        System.out.println("| 4. Goodbye: exit                 |");
        System.out.println("| 5. Instructions: help            |");
        System.out.println("|                                  |");
        System.out.println(" ----------------------------------");

    }

    static class NotificationListenerThread extends Thread {
        @Override
        public void run() {
            try {
                while (!isTerminated) {
                    String[] notifications = (String[]) notiTotp.receiveReq();
                    if(notiTotp.hasError()) {
                        logger.error(notiTotp.getErrorMsg());
                        notiTotp.respond(TotpCmd.PUSH, TotpStatus.TRANSMISSION_FAILED);
                        continue;
                    }
                    // Respond Success
                    notiTotp.respond(TotpCmd.PUSH, TotpStatus.TRANSFER_ACTION_COMPLETED);
                    for (String notiStr : notifications) {
                        System.out.println("[NOTIFICATION]: " + notiStr);
                    }

                }
            } catch (EOFException e) {
                logger.log(Level.INFO, "Notification thread terminated due to server closed.");
            } catch (IOException e) {
                if(isTerminated) {
                    logger.log(Level.INFO, "Notification thread terminated.");
                }
                else {
                    // Print out stack if not properly terminated by main thread
                    e.printStackTrace();
                }
            }
        }
    }
}
