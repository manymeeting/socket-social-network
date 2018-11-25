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
//    public static String decodeStatus(int status) {
//        switch (status) {
//            case 200:
//                return "General success";
//            case 210:
//                return "System status";
//            case 220:
//                return "Service ready";
//            case 221:
//                return "Service closing transmission channel";
//            case 250:
//                return "Transfer action completed";
//            case 330:
//                return "Ready for list receiving";
//            case 331:
//                return "Start list transmission";
//            case 421:
//                return "Service not available, closing transmission channel";
//            case 450:
//                return "Unknown recipient";
//            case 451:
//                return "Local processing error";
//            case 500:
//                return "Command unrecognized";
//            case 501:
//                return "Error in parameters or arguments";
//            case 502:
//                return "Command not implemented";
//            case 530:
//                return "Error in parameters or arguments";
//            case 540:
//                return "Command not implemented";
//            case 541:
//                return "Authentication required";
//            default:
//                return null;
//        }
//    }

    //TODO: Replace Socket to SSLSocket
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
