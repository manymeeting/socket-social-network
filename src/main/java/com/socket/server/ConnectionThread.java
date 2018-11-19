package com.socket.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class ConnectionThread extends Thread {

    private Socket clientSocket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ConnectionThread(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        this.dos = new DataOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {
                received = dis.readUTF();
                System.out.println(String.format("received from client %s: %s", clientSocket.getInetAddress().getHostAddress(), received));
                if(received.equals("Exit"))
                {
                    System.out.println("Client " + this.clientSocket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.clientSocket.close();
                    System.out.println("Connection closed");
                    break;
                }
                dos.writeUTF("hello from server " + new Date().toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (dis != null) dis.close();
                    if (dos != null) dos.close();
                    clientSocket.close();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
