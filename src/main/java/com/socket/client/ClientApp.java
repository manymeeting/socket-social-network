package com.socket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {

        InetAddress ip = null;
        Scanner scn = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            ip = InetAddress.getByName("localhost");
            Socket socket = new Socket(ip, 9091);
            System.out.println("Client started, connecting to server localhost:9091");
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            scn = new Scanner(System.in);
            while (true) {
                String tosend = scn.nextLine();
                System.out.println("tosend = " + tosend);
                dos.writeUTF(tosend);
                if (tosend.equals("Exit")) {
                    System.out.println("Closing this connection : " + socket);
                    socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                String received = dis.readUTF();
                System.out.println(received);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scn != null) scn.close();
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
