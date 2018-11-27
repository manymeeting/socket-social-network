package com.socket.totp.test;

import com.socket.totp.TotpClient;
import com.socket.totp.TotpCmd;
import com.socket.totp.TotpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class TotpClientTest {
    @Test
    public void loginTest() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
    }

    @Test
    public void postTest() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        totpClient.send("user2", "wall", "This is a wall message.");
        assertFalse(totpClient.hasError());
    }

    @Test
    public void retrieveWallTest() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        String[] msgs = totpClient.retrieve("user2", "wall");
        assertArrayEquals(new String[]{"First message", "Second message"}, msgs);
    }

    @Test
    public void getFriendListTest() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        String[] msgs = totpClient.retrieveFriendList();
        assertArrayEquals(new String[]{"user1", "user2"}, msgs);
    }

    @Test void heartBeatTest() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        String token_id = "123456789012345678901234567890123456";
        TotpClient totpClient = new TotpClient(socket, token_id);
        totpClient.heartBeat();
        assertFalse(totpClient.hasError());
        socket.close();
    }

    @Test void closeTest() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        String user = totpClient.goodbye();
        assertEquals("user1", user);
        socket.close();
    }

    @Test void pushTest() throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        String[] msgs = (String[]) totpClient.receiveReq();
        Assertions.assertFalse(totpClient.hasError());
        assertArrayEquals(new String[]{"user1 posted a message", "user2 posted a message"}, msgs);
        totpClient.response(TotpCmd.PUSH, TotpStatus.valueOf(250));
    }
}