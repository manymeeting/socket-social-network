package com.socket.totp.test;

import com.socket.totp.TotpCmd;
import com.socket.totp.TotpField;
import com.socket.totp.TotpServer;
import com.socket.totp.TotpStatus;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

class TotpServerTest {
    @Test
    public void loginTest() throws IOException {
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpField.COMMAND));
        totpServer.response(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpField.COMMAND));
        assertEquals("user1", req.get(TotpField.USER));
        assertEquals("password", req.get(TotpField.PASSWORD));
        totpServer.response(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");
    }

    @Test
    public void postTest() throws IOException {
        // Login
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpField.COMMAND));
        totpServer.response(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpField.COMMAND));
        assertEquals("user1", req.get(TotpField.USER));
        assertEquals("password", req.get(TotpField.PASSWORD));
        totpServer.response(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        // Post
        req = totpServer.receiveReq();
        assertEquals("123456789012345678901234567890123456", req.get(TotpField.TOKEN_ID));
        assertEquals("SEND", req.get(TotpField.COMMAND));
        assertEquals("user2", req.get(TotpField.USER));
        assertEquals("wall", req.get(TotpField.MSGBOX));
        totpServer.response(TotpCmd.SEND, TotpStatus.valueOf(330), req.get(TotpField.USER), req.get(TotpField.MSGBOX));
        req = totpServer.receiveReq();
        assertEquals("This is a wall message.", req.get(TotpField.MESSAGE));
        totpServer.response(TotpCmd.DATA, TotpStatus.valueOf(250));
    }

    @Test
    public void retrieveWallTest() throws IOException {
        // Login
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpField.COMMAND));
        totpServer.response(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpField.COMMAND));
        assertEquals("user1", req.get(TotpField.USER));
        assertEquals("password", req.get(TotpField.PASSWORD));
        totpServer.response(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        // Retrieve
        req = totpServer.receiveReq();
        assertEquals("123456789012345678901234567890123456", req.get(TotpField.TOKEN_ID));
        assertEquals("RETR", req.get(TotpField.COMMAND));
        assertEquals("user2", req.get(TotpField.USER));
        assertEquals("wall", req.get(TotpField.MSGBOX));
        List<String> wallMsgs = new ArrayList<>();
        wallMsgs.add("First message"); //13 chars
        wallMsgs.add("Second message"); //14 chars
        totpServer.response(TotpCmd.RETR, TotpStatus.valueOf(331), 2, 27, wallMsgs);
    }

    @Test
    public void getFriendListTest() throws IOException {
        // Login
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpField.COMMAND));
        totpServer.response(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpField.COMMAND));
        assertEquals("user1", req.get(TotpField.USER));
        assertEquals("password", req.get(TotpField.PASSWORD));
        totpServer.response(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        // Get friend list
        req = totpServer.receiveReq();
        assertEquals("123456789012345678901234567890123456", req.get(TotpField.TOKEN_ID));
        assertEquals("FRND", req.get(TotpField.COMMAND));
        List<String> friends = new ArrayList<>();
        friends.add("user1"); //13 chars
        friends.add("user2"); //14 chars
        totpServer.response(TotpCmd.RETR, TotpStatus.valueOf(331), 2, 10, friends);
    }

    @Test
    public void heartBeatTest() throws IOException {
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpField, String> req = totpServer.receiveReq();
        assertEquals("123456789012345678901234567890123456", req.get(TotpField.TOKEN_ID));
        assertEquals("HRBT", req.get(TotpField.COMMAND));
        totpServer.response(TotpCmd.HRBT, TotpStatus.valueOf(200));
    }

    @Test
    public void closeTest() throws IOException {
        // Login
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        Map<TotpField, String> req = totpServer.receiveReq();
        assertEquals("HELO", req.get(TotpField.COMMAND));
        totpServer.response(TotpCmd.HELO, TotpStatus.valueOf(541));
        req = totpServer.receiveReq();
        assertEquals("PASS", req.get(TotpField.COMMAND));
        assertEquals("user1", req.get(TotpField.USER));
        assertEquals("password", req.get(TotpField.PASSWORD));
        totpServer.response(TotpCmd.PASS, TotpStatus.valueOf(200), "123456789012345678901234567890123456");

        // Close
        req = totpServer.receiveReq();
        assertEquals("123456789012345678901234567890123456", req.get(TotpField.TOKEN_ID));
        assertEquals("GBYE", req.get(TotpField.COMMAND));
        totpServer.response(TotpCmd.GBYE, TotpStatus.valueOf(200), "user1");
        socket.close();
    }

    @Test
    public void pushTest() throws IOException {
        ServerSocket servSocket = new ServerSocket(9999);
        Socket socket = servSocket.accept();
        TotpServer totpServer = new TotpServer(socket);
        List<String> messages = new ArrayList<>();
        messages.add("user1 posted a message");
        messages.add("user2 posted a message");
        Map<TotpField, String> map = totpServer.push(messages);
        assertEquals("250", map.get(TotpField.STATUS));
    }
}