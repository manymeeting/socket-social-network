package com.socket.totp;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TotpClient extends TotpProtocol {
    private String token_id;
    Timer timer;

    /**
     * Creates a TotpProtocol with client functionality
     * that handles the TOTP based information exchanging
     * using the specified underlying socket.
     * @param socket A socket for connecting to the server
     */
    public TotpClient(Socket socket) {
        this(socket, "");
    }

    /**
     * Creates a TotpProtocol with client functionality
     * that handles the TOTP based information exchanging
     * using the specified underlying socket.
     * @param socket A socket for connecting to the server
     * @param token_id The token_id acquired previously from the server if any.
     */
    public TotpClient(Socket socket, String token_id) {
        super(socket);
        this.token_id = token_id;
        this.timer = new Timer();
    }

    /**
     * Login to the server with the given user and password.
     * The token_id acquired from the server will be stored
     * in this instance.
     * @param user Username
     * @param password Password
     * @return
     */
    public String login(String user, String password) {
        TotpContent totpContent;
        String req, resp;
        clearError();
        try {
            /* Send HELO command */
            req = contructReq(TotpCmd.HELO);
            write(req);
            resp = read();
            totpContent = parseResp(TotpCmd.HELO, resp);
            if (totpContent.status == TotpStatus.AUTHENTICATION_REQUIRED) {
                /* Send PASS command */
                req = contructReq(TotpCmd.PASS, user, password);
                write(req);
                resp = read();
                totpContent = parseResp(TotpCmd.PASS, resp);
            } else if (totpContent.status != TotpStatus.SUCCESS) {
                setError(totpContent.status.getReasonPhrase());
                return "";
            }
            return (String)totpContent.content;
        } catch (IOException e) {
            setError(e.getMessage());
            return "";
        }
    }

    /**
     * Send a message to a specific message-box of a given user.
     * @param user The target recipient
     * @param msgbox The target message-box
     * @param content The message to send
     */
    public void send(String user, String msgbox, String content) {
        TotpContent totpContent;
        String req, resp;
        clearError();
        try {
            req = contructReq(TotpCmd.SEND, user, msgbox);
            write(req);
            resp = read();
            totpContent = parseResp(TotpCmd.SEND, resp);
            if (totpContent.status != TotpStatus.READY_LIST_RECEIVING) {
                setError(totpContent.status.getReasonPhrase());
                return;
            }
            String[] dest = ((String)totpContent.content).split(":");
            if (!dest[0].equals(user) || !dest[1].equals(msgbox)) {
                setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                return;
            }
            req = contructReq(TotpCmd.DATA, 1, content.length(), content);
            write(req);
            resp = read();
            totpContent = parseResp(TotpCmd.DATA, resp);
            if (totpContent.status.getValue() != 250) {
                setError(totpContent.status.getReasonPhrase());
            }
        } catch (IOException e) {
            setError(e.getMessage());
        }
    }

    /**
     * Retrieve all the messages from the specific message-box of the given user.
     * @param user The user who owns the messages
     * @param msgbox The message-box that contains the messages
     * @return Returns a list of messages that stores in a String array
     */
    public String[] retrieve(String user, String msgbox) {
        TotpContent totpContent;
        String req, resp;
        clearError();
        try {
            req = contructReq(TotpCmd.RETR, user, msgbox);
            write(req);
            resp = read();
            totpContent = parseResp(TotpCmd.RETR, resp);
            if (totpContent.status != TotpStatus.START_LIST_TRANSMISSION) {
                setError(totpContent.status.getReasonPhrase());
                return null;
            }
            return (String[]) totpContent.content;
        } catch (IOException e) {
            setError(e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve the friend list of the current user.
     * @return A list of friends that stores in a String array
     */
    public String[] retrieveFriendList() {
        TotpContent totpContent;
        String req, resp;
        clearError();
        try {
            req = contructReq(TotpCmd.FRND);
            write(req);
            resp = read();
            totpContent = parseResp(TotpCmd.FRND, resp);
            if (totpContent.status != TotpStatus.START_LIST_TRANSMISSION) {
                setError(totpContent.status.getReasonPhrase());
                return null;
            }
            return (String[]) totpContent.content;
        } catch (IOException e) {
            setError(e.getMessage());
            return null;
        }
    }

    /**
     * Send a heart-beat to the server.
     * @throws IOException
     */
    public void heartBeat() throws IOException {
        TotpContent totpContent;
        String req, resp;
        clearError();
        req = contructReq(TotpCmd.HRBT);
        write(req);
        resp = read();
        totpContent = parseResp(TotpCmd.HRBT, resp);
        if (totpContent.status != TotpStatus.SUCCESS) {
            setError(totpContent.status.getReasonPhrase());
        }
    }

    /**
     * Issue the server a protocol close request.
     * @return The closed user sent from the server
     * @throws IOException
     */
    public String goodbye() throws IOException {
        TotpContent totpContent;
        String req, resp;
        clearError();
        req = contructReq(TotpCmd.GBYE);
        write(req);
        resp = read();
        totpContent = parseResp(TotpCmd.GBYE, resp);
        if (totpContent.status != TotpStatus.SUCCESS) {
            setError(totpContent.status.getReasonPhrase());
        }
        return (String) totpContent.content;
    }

    /**
     * Block and wait for requests from server.
     * @return Returns the content of the request. May vary from different requests.
     *         PUSH: (String[]) A list of notification message.
     * @throws IOException
     */
    public Object receiveReq() throws IOException {
        String req = read();
        return parseReq(req).content;
    }

    protected String contructReq(TotpCmd cmd, Object... args) {
        String req = null;
        switch (cmd) {
            case HELO:
                req = "HELO\r\n";
                break;
            case PASS:
                req = String.format("PASS %s %s\r\n", args[0], args[1]);
                break;
            case SEND:
                req = String.format("SEND %s:%s\r\n", args[0], args[1]);
                break;
            case DATA:
                String msg = args[2] + "\r\n";
                req = String.format("DATA %d %d\r\n%s\r\n.\r\n", args[0], args[1], msg);
                break;
            case RETR:
                req = String.format("RETR %s:%s\r\n", args[0], args[1]);
                break;
            case FRND:
                req = "FRND\r\n";
                break;
            case HRBT:
                req = "HRBT\r\n";
                break;
            case GBYE:
                req = "GBYE\r\n";
                break;
            default:
                throw new IllegalArgumentException();
        }
        return req;
    }

    @Override
    protected String contructResp(TotpCmd cmd, TotpStatus status, Object... args) {
        switch (cmd) {
            case PUSH:
                return String.format("%d\r\n", status.getValue());
            default:
                throw new IllegalArgumentException();
        }
    }

    protected TotpContent parseReq(String req) {
        TotpContent totpContent = new TotpContent();
        Pattern pattern = Pattern.compile("^(\\w+)(?:\r\n|\\s)?([\\s\\S]+)?\r\n$");
        Matcher matcher = pattern.matcher(req);
        String cmd = "", arg = "";
        if (matcher.find()) {
            cmd = matcher.group(1);
            if (matcher.groupCount() > 1)
                arg = matcher.group(2);
        } else {
            setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
        }
        totpContent.cmd = TotpCmd.valueOf(cmd);
        switch (TotpCmd.valueOf(cmd)) {
            case PUSH:
                pattern = Pattern.compile("(\\d+)\\s(\\d+)\r\n([\\s\\S]+)\r\n.$");
                matcher = pattern.matcher(arg);
                if (matcher.find()) {
                    int numOfMsg = Integer.valueOf(matcher.group(1));
                    int totalSize = Integer.valueOf(matcher.group(2));
                    String[] msgs = matcher.group(3).split("\r\n");
                    for (String msg : msgs) totalSize -= msg.length();
                    if (numOfMsg != msgs.length || totalSize != 0) {
                        setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                    }
                    totpContent.content = msgs;
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
        return totpContent;
    }

    protected TotpContent parseResp(TotpCmd cmd, String resp) {
        TotpContent totpContent = new TotpContent();
        Pattern pattern;
        Matcher matcher;
        switch (cmd) {
            case HELO:
                pattern = Pattern.compile("(\\d+)\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            case PASS:
                pattern = Pattern.compile("(\\d+\\b)\\s(\\w+)\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                    token_id = matcher.group(2);
                    totpContent.content = token_id;
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            case SEND:
                pattern = Pattern.compile("(\\d+\\b)\\s(\\w+:\\w+)\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                    totpContent.content = matcher.group(2);
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            case DATA:
                pattern = Pattern.compile("(\\d+)\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            case RETR:
                pattern = Pattern.compile("(\\d+)\\s(\\d+)\\s(\\d+)\r\n([\\s\\S]+)\r\n.\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                    int numOfMsg = Integer.valueOf(matcher.group(2));
                    int totalSize = Integer.valueOf(matcher.group(3));
                    String[] msgs = matcher.group(4).split("\r\n");
                    for (String msg : msgs) totalSize -= msg.length();
                    if (numOfMsg != msgs.length || totalSize != 0) {
                        setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                    }
                    totpContent.content = msgs;
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            case FRND:
                pattern = Pattern.compile("(\\d+)\\s(\\d+)\\s(\\d+)\r\n([\\s\\S]+)\r\n.\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                    int numOfFriend = Integer.valueOf(matcher.group(2));
                    int totalSize = Integer.valueOf(matcher.group(3));
                    String[] friends = matcher.group(4).split("\r\n");
                    for (String msg : friends) totalSize -= msg.length();
                    if (numOfFriend != friends.length || totalSize != 0) {
                        setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                    }
                    totpContent.content = friends;
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            case HRBT:
                pattern = Pattern.compile("(\\d+)\\sHRBT\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            case GBYE:
                pattern = Pattern.compile("(\\d+)\\sGBYE\\s(\\w+)\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                    totpContent.content = matcher.group(2);
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            default:
                return null;
        }
        return totpContent;
    }

    /**
     * Get the token_id acquired from the server.
     * @return
     */
    public String getTokenId() {
        return this.token_id;
    }

    private String encapToken(String req) {
        return this.token_id + " " + req;
    }

    protected void write(String msg) throws IOException {
        if (!token_id.equals("")) msg = encapToken(msg);
        super.write(msg);
    }
}
