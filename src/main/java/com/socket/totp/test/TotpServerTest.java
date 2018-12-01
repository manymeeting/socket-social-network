package com.socket.totp.test;

import com.socket.totp.TotpCmd;
import com.socket.totp.TotpReqHeaderField;
import com.socket.totp.TotpServer;
import com.socket.totp.TotpStatus;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests listed here are independent to each other.
 * To run the tests, run the tests with the same function name
 * on both server and client side.
 * Note: Should run the server side first then the client side.
 *
 * This test file is only for your reference when developing
 * your business logic. It should be deleted before merging
 * in to the master branch.
 */
class TotpServerTest {

    @Test
    public void loginTest() throws IOException {
        // Create new socket
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        // Create server protocol
        TotpServer totpServer = new TotpServer(socket);
        // Block for incoming requests from client
        Map<TotpReqHeaderField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpReqHeaderField.COMMAND));

        //
        // Validate the token_id sent with HELO...
        // If token_id is "" or token_id is not valid,
        // respond 541 to requests user and password
        //

        // Respond to the client
        totpServer.respond(TotpCmd.HELO, TotpStatus.valueOf(541));
        // Block for incoming requests from client
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("user1", req.get(TotpReqHeaderField.USER));
        assertEquals("password", req.get(TotpReqHeaderField.PASSWORD));
        // Respond to the client
        totpServer.respond(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");
    }

    @Test
    public void postTest() throws IOException {
        /* Login */
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpReqHeaderField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpReqHeaderField.COMMAND));
        totpServer.respond(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("user1", req.get(TotpReqHeaderField.USER));
        assertEquals("password", req.get(TotpReqHeaderField.PASSWORD));
        totpServer.respond(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        /* Post */
        req = totpServer.receiveReq();
        // Validate the token_id from database
        assertEquals("123456789012345678901234567890123456", req.get(TotpReqHeaderField.TOKEN_ID));
        assertEquals("SEND", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("user2", req.get(TotpReqHeaderField.USER));
        assertEquals("wall", req.get(TotpReqHeaderField.MSGBOX));
        // Respond to the client
        totpServer.respond(TotpCmd.SEND, TotpStatus.valueOf(330), req.get(TotpReqHeaderField.USER), req.get(TotpReqHeaderField.MSGBOX));
        // Block for incoming requests
        req = totpServer.receiveReq();
        assertEquals("DATA", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("This is a wall message.", req.get(TotpReqHeaderField.MESSAGE));

        //
        // Save the message to the database...
        //

        totpServer.respond(TotpCmd.DATA, TotpStatus.valueOf(250));
    }

    @Test
    public void retrieveWallTest() throws IOException {
        /* Login */
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpReqHeaderField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpReqHeaderField.COMMAND));
        totpServer.respond(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("user1", req.get(TotpReqHeaderField.USER));
        assertEquals("password", req.get(TotpReqHeaderField.PASSWORD));
        totpServer.respond(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        /* Retrieve */
        // Block for incoming requests
        req = totpServer.receiveReq();
        // Validate the token_id from database
        assertEquals("123456789012345678901234567890123456", req.get(TotpReqHeaderField.TOKEN_ID));
        assertEquals("RETR", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("user2", req.get(TotpReqHeaderField.USER));
        assertEquals("wall", req.get(TotpReqHeaderField.MSGBOX));
        // Retrieve the messages from database...
        List<String> wallMsgs = new ArrayList<>();
        wallMsgs.add("First message"); //13 chars
        wallMsgs.add("Second message"); //14 chars
        // Respond the messages back to the client
        totpServer.respond(TotpCmd.RETR, TotpStatus.valueOf(331), 2, 27, wallMsgs);
    }

    @Test
    public void retrieveFriendListTest() throws IOException {
        /* Login */
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpReqHeaderField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpReqHeaderField.COMMAND));
        totpServer.respond(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("user1", req.get(TotpReqHeaderField.USER));
        assertEquals("password", req.get(TotpReqHeaderField.PASSWORD));
        totpServer.respond(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        /* Retrieve friend list */
        // Block for incoming requests
        req = totpServer.receiveReq();
        // Validate the token_id from database
        assertEquals("123456789012345678901234567890123456", req.get(TotpReqHeaderField.TOKEN_ID));
        assertEquals("FRND", req.get(TotpReqHeaderField.COMMAND));
        // Retrieve user list from the database
        List<String> friends = new ArrayList<>();
        friends.add("user1"); // 5 chars
        friends.add("user2"); // 5 chars
        // Respond the user list to the client
        totpServer.respond(TotpCmd.RETR, TotpStatus.valueOf(331), 2, 10, friends);
    }

    @Test
    public void heartBeatTest() throws IOException {
        // Initialize
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        // Block for incoming requests
        Map<TotpReqHeaderField, String> req = totpServer.receiveReq();
        // Validate the token_id from database
        assertEquals("123456789012345678901234567890123456", req.get(TotpReqHeaderField.TOKEN_ID));
        assertEquals("HRBT", req.get(TotpReqHeaderField.COMMAND));
        // Respond to the client
        totpServer.respond(TotpCmd.HRBT, TotpStatus.valueOf(200));
    }

    @Test
    public void closeTest() throws IOException {
        /* Login */
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpReqHeaderField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpReqHeaderField.COMMAND));
        totpServer.respond(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpReqHeaderField.COMMAND));
        assertEquals("user1", req.get(TotpReqHeaderField.USER));
        assertEquals("password", req.get(TotpReqHeaderField.PASSWORD));
        totpServer.respond(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        /* Close */
        // Block for incoming requests
        req = totpServer.receiveReq();
        // Validate the token_id from database
        assertEquals("123456789012345678901234567890123456", req.get(TotpReqHeaderField.TOKEN_ID));
        assertEquals("GBYE", req.get(TotpReqHeaderField.COMMAND));

        //
        // Retrieve the username from the database
        //

        // Respond to the client
        totpServer.respond(TotpCmd.GBYE, TotpStatus.valueOf(200), "user1");
        // Close the socket
        socket.close();
    }

    @Test
    public void pushTest() throws IOException {
        // Initialize
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        // Retrieve messages to push from the database
        List<String> messages = new ArrayList<>();
        messages.add("user1 posted a message");
        messages.add("user2 posted a message");
        // Push the messages
        Map<TotpReqHeaderField, String> map = totpServer.push(messages);
        // Check the response status from the client
        assertEquals("250", map.get(TotpReqHeaderField.STATUS));
    }
}