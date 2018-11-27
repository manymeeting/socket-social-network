package com.socket.totp;


import java.io.*;
import java.net.Socket;
import java.util.Map;

/* A connection class that support Team-One Transfer Protocol */
public abstract class TotpProtocol {
    private DataInputStream dis;
    private DataOutputStream dos;
    private String errorMsg;
    private boolean errOccured;

    //TODO: Replace Socket to SSLSocket
    /**
     * Creates a TotpProtocol that handles the TOTP based information
     * exchanging using the specified underlying socket.
     * @param socket
     */
    public TotpProtocol(Socket socket) {
        errOccured = false;
        errorMsg = "";
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            this.errorMsg = e.getMessage();
            errOccured = true;
        }
    }

    /**
     * Send a command-specific response back to the request initiator.
     * @param cmd The command that you want to response for
     * @param status The status of the request
     * @param args Other arguments varied from each command.
     *             HELO: No arguments
     *             PASS: (String) token_id
     *             DATA: No arguments
     *             RETR: (List<String>) messages
     *             FRND: (List<String>) friend_list
     *             HRBT: No arguments
     *             GBYE: (String) user
     *             ERROR: No arguments
     * @throws IOException
     */
    public void response(TotpCmd cmd, TotpStatus status, Object... args) throws IOException {
        String resp = contructResp(cmd, status, args);
        write(resp);
    }

    protected String read() throws IOException {
        String str = null;
        StringBuilder sb = new StringBuilder();
        while ((str = dis.readUTF()) != null) {
            sb.append(str);
            if (str.endsWith("\r\n")) break;
        }
        return sb.toString();
    }

    protected abstract String contructReq(TotpCmd cmd, Object... args);

    protected abstract String contructResp(TotpCmd command, TotpStatus status, Object... args);

    protected abstract TotpContent parseResp(TotpCmd cmd, String resp);

    /**
     * Check if any error occurs during message exchanging.
     * Must check after each TOTP request.
     * @return Returns true if any error occurs, false if not.
     */
    public boolean hasError() {
        return this.errOccured;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    protected void write(String msg) throws IOException {
        dos.writeUTF(msg);
    }

    protected void setError(String errorMsg) {
        this.errorMsg = errorMsg;
        this.errOccured = true;
    }

    protected void clearError() {
        this.errorMsg = "";
        this.errOccured = false;
    }
}
