package com.socket.server.thread;

import com.socket.server.ServerApp;
import com.socket.server.entity.AppUser;
import com.socket.server.entity.Message;
import com.socket.server.entity.NotificationMessage;
import com.socket.totp.TotpCmd;
import com.socket.totp.TotpReqHeaderField;
import com.socket.totp.TotpServer;
import com.socket.totp.TotpStatus;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class SlaveThread extends Thread {

    private static final Logger logger = LogManager.getLogger(SlaveThread.class);
    private final ServerApp server;
    private Socket clientSocket;
    private Socket notiSocket;
    private TotpServer serverTotp;
    private AppUser user = null;
    private volatile OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
    private volatile ReceivingDataStatus receivingDataStatus = ReceivingDataStatus.OFF;

    public SlaveThread(ServerApp server, Socket clientSocket, Socket notiSocket) {
        this.clientSocket = clientSocket;
        this.notiSocket = notiSocket;
        this.server = server;
        this.serverTotp = new TotpServer(clientSocket);
    }

    @Override
    public void run() {
        try {

            while(true) {
                // Block for incoming requests from client
                Map<TotpReqHeaderField, String> req = serverTotp.receiveReq();

                String reqToken = req.get(TotpReqHeaderField.TOKEN_ID);
                String reqCommand = req.get(TotpReqHeaderField.COMMAND);

                // Check empty command
                if(reqCommand == null || reqCommand.equals("")) {
                    serverTotp.respond(TotpCmd.ERROR, TotpStatus.ERROR_PARAMETERS_ARGUMENTS);
                    continue;
                }
                // Check if token exists
                if(reqToken == null || reqToken.equals("")) {
                    // No token, ok for PASS and HELO, error for all other commands
                    if(reqCommand.equals(TotpCmd.HELO.toString())) {
                        serverTotp.respond(TotpCmd.HELO, TotpStatus.AUTHENTICATION_REQUIRED);
                        continue;
                    }
                    else if(reqCommand.equals(TotpCmd.PASS.toString())) {
                        String username = req.get(TotpReqHeaderField.USER);
                        String password = req.get(TotpReqHeaderField.PASSWORD);
                        user = server.userLogin(username, password);

                        if(user != null) {
                            // Successfully login
                            serverTotp.respond(TotpCmd.PASS, TotpStatus.SUCCESS, user.getToken());
                            // Update thread to be online
                            setOnlineStatus(OnlineStatus.ONLINE);
                            sendNotification(user.getToken(), String.format("Welcome %s, you have %d unread messages.",
                                    user.getUsername(), user.getUnreadMessages().size()));
                            for (String unread : user.getUnreadMessages()) {
                                sendNotification(user.getToken(), "[Unread] " + unread);
                            }

                            sendNotification(NotificationMessage.EMPTY_TOKEN,
                                    String.format("User %s has been online now.", user.getUsername()));
                        }
                        else {
                            // Login validation failed
                            serverTotp.respond(TotpCmd.PASS, TotpStatus.PERMISSION_FAILED);
                        }
                        continue;
                    }
                    else {
                        serverTotp.respond(TotpCmd.ERROR, TotpStatus.PERMISSION_FAILED);
                        continue;
                    }
                }

                // Validate token
                if(!server.validateToken(reqToken)) {
                    serverTotp.respond(TotpCmd.valueOf(reqCommand), TotpStatus.AUTHENTICATION_REQUIRED);
                    continue;
                }

                // Handle other commands
                if(reqCommand.equals(TotpCmd.GBYE.toString())) {
                    setOnlineStatus(OnlineStatus.OFFLINE);
                    break;
                }

                // SEND
                if(reqCommand.equals(TotpCmd.SEND.toString())) {
                    this.receivingDataStatus = ReceivingDataStatus.RECEIVING;
                    serverTotp.respond(TotpCmd.SEND, TotpStatus.READY_LIST_RECEIVING,
                            req.get(TotpReqHeaderField.USER), req.get(TotpReqHeaderField.MSGBOX));
                    continue;
                }
                // DATA
                if(reqCommand.equals(TotpCmd.DATA.toString())) {
                    if(!this.receivingDataStatus.equals(ReceivingDataStatus.RECEIVING)) {
                        serverTotp.respond(TotpCmd.DATA, TotpStatus.TRANSMISSION_FAILED);
                    }
                    // Save message and broadcast notification
                    Message newMessage = new Message(
                            req.get(TotpReqHeaderField.USER),
                            user.getUsername(),
                            req.get(TotpReqHeaderField.MSGBOX),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                            req.get(TotpReqHeaderField.MESSAGE)
                    );
                    receiveMessage(newMessage);
                    sendNotification(NotificationMessage.EMPTY_TOKEN, String.format("%s posted: %s",
                            user.getUsername(), req.get(TotpReqHeaderField.MESSAGE)));
                    serverTotp.respond(TotpCmd.DATA, TotpStatus.TRANSFER_ACTION_COMPLETED);
                    continue;
                }
            }
        } catch (EOFException e) {
            // Client became offline, update client status
            logger.log(Level.DEBUG, String.format("%s is closing connection", user.getUsername()));
            setOnlineStatus(OnlineStatus.OFFLINE);

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            this.shutdown();
        }
    }

    private void shutdown() {
        try {
            serverTotp.close();
            clientSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String token, String messageStr) {
        server.addNotification(new NotificationMessage(token, messageStr));
    }

    private void receiveMessage(Message message) {
        server.addMessage(message);
    }

    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;

        if (onlineStatus == OnlineStatus.ONLINE) {
            logger.log(Level.DEBUG, String.format("User %s is online now.", user.getUsername()));
            server.addOnlineUser(user.getToken(), this, this.notiSocket);
        } else {
            logger.log(Level.DEBUG, String.format("User %s is offline now.", user.getUsername()));
            server.removeOnlineUser(user.getToken());
            // Update last active timestamp
            user.setLastActiveTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
    }

    enum OnlineStatus {
        ONLINE, OFFLINE
    }

    enum ReceivingDataStatus {
        RECEIVING, OFF
    }
}
