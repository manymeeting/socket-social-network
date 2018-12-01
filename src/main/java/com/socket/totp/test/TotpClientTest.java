package com.socket.totp.test;

import com.socket.totp.TotpClient;
import com.socket.totp.TotpCmd;
import com.socket.totp.TotpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

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
class TotpClientTest {
    @Test
    public void loginTest() throws IOException {
        // Create new socket
        Socket socket = new Socket("127.0.0.1", 9999);
        // Create client protocol
        TotpClient totpClient = new TotpClient(socket);
        // Do login
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
    }

    @Test
    public void postTest() throws IOException {
        // Create new socket
        Socket socket = new Socket("127.0.0.1", 9999);
        // Create client protocol
        TotpClient totpClient = new TotpClient(socket);
        // Do login
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        // Post a message to user2's wall through SEND command
        totpClient.send("user2", "wall", "This is a wall message.");
        assertFalse(totpClient.hasError());
    }

    @Test
    public void retrieveWallTest() throws IOException {
        // Initialize
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        // Login
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        // Retrieve user2's wall messages
        String[] msgs = totpClient.retrieve("user2", "wall");
        assertArrayEquals(new String[]{"First message", "Second message"}, msgs);
    }

    @Test
    public void retrieveFriendListTest() throws IOException {
        // Initialize
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        // Login
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        // Retrieve user list
        String[] friends = totpClient.retrieveFriendList();
        assertArrayEquals(new String[]{"user1", "user2"}, friends);
    }

    @Test void heartBeatTest() throws IOException {
        // Initialize
        Socket socket = new Socket("127.0.0.1", 9999);
        String token_id = "123456789012345678901234567890123456";
        TotpClient totpClient = new TotpClient(socket, token_id);
        // Send heart beat
        totpClient.heartBeat();
        assertFalse(totpClient.hasError());
    }

    @Test void closeTest() throws IOException {
        // Initialize
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        // Login
        String token_id = totpClient.login("user1", "password");
        assertEquals("123456789012345678901234567890123456", token_id);
        // Send GBYE
        String user = totpClient.goodbye();
        assertEquals("user1", user);
        socket.close();
    }

    @Test void pushTest() throws IOException {
        // Initialize
        Socket socket = new Socket("127.0.0.1", 9999);
        TotpClient totpClient = new TotpClient(socket);
        // Block for incoming requests from server
        String[] msgs = (String[]) totpClient.receiveReq();
        Assertions.assertFalse(totpClient.hasError());
        assertArrayEquals(new String[]{"user1 posted a message", "user2 posted a message"}, msgs);
        // Respond to the server
        totpClient.respond(TotpCmd.PUSH, TotpStatus.valueOf(250));
    }
}