package com.socket.totp;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TotpServer extends TotpProtocol {
    private int tokenLen = 36;

    /**
     * Creates a TotpProtocol with server functionality
     * that handles the TOTP based information exchanging
     * using the specified underlying socket. The length
     * of token_id is set to a default value 36.
     * @param socket A socket for connecting to the server
     */
    public TotpServer(Socket socket) {
        this(socket, 36);
    }

    /**
     * Creates a TotpProtocol with server functionality
     * that handles the TOTP based information exchanging
     * using the specified underlying socket.
     * @param socket A socket for connecting to the server
     * @param tokenLen The length of token_id
     */
    public TotpServer(Socket socket, int tokenLen) {
        super(socket);
        this.tokenLen = tokenLen;
    }

    @Override
    protected String contructReq(TotpCmd cmd, Object... args) {
        switch (cmd) {
            case PUSH:
                String msgs = String.join("\r\n", (List<String>) args[2]);
                return String.format("PUSH %d %d\r\n%s\r\n.\r\n", args[0], args[1], msgs);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected String contructResp(TotpCmd cmd, TotpStatus status, Object... args) {
        switch (cmd) {
            case HELO:
                return String.format("%d\r\n", status.getValue());
            case PASS:
                return String.format("%d %s\r\n", status.getValue(), args[0]);
            case SEND:
                return String.format("%d %s:%s\r\n", status.getValue(), args[0], args[1]);
            case DATA:
                return String.format("%d\r\n", status.getValue());
            case RETR:
                String msgs = String.join("\r\n", (List<String>) args[2]);
                return String.format("%d %d %d\r\n%s\r\n.\r\n", status.getValue(), args[0], args[1], msgs);
            case FRND:
                String friends = String.join("\r\n", (List<String>) args[2]);
                return String.format("%d %d %d\r\n%s\r\n.\r\n", status.getValue(), args[0], args[1], friends);
            case HRBT:
                return String.format("%d HRBT\r\n", status.getValue());
            case GBYE:
                return String.format("%d GBYE %s\r\n", status.getValue(), args[0]);
            case ERROR:
                return String.format("%d\r\n", status.getValue());
            default:
                return null;
        }
    }

    /**
     * Push notifications to the client.
     * @param messages A list of messages you want to push
     * @return Returns a map contains a STATUS key
     * @throws IOException
     */
    public Map<TotpField, String> push(List<String> messages) throws IOException {
        Map<TotpField, String> map = new HashMap<>();
        TotpContent totpContent;
        String req, resp;
        clearError();
        int size = 0;
        for (String message : messages) size += message.length();
        req = contructReq(TotpCmd.PUSH, messages.size(), size, messages);
        write(req);
        resp = read();
        totpContent = parseResp(TotpCmd.PUSH, resp);
        if (totpContent.status != TotpStatus.valueOf(250)) {
            setError(totpContent.status.getReasonPhrase());
        }
        map.put(TotpField.STATUS, String.valueOf(totpContent.status.getValue()));
        return map;
    }

    /**
     * Block to receive incoming data.
     * @return Returns a map contains multiple return values.
     * Possible keys are defined in TotpField class.
     * The followings are the keys used by each command:
     *     HELO: COMMAND
     *     PASS: COMMAND, USER, PASSWORD
     *     SEND: COMMAND, TOKEN_ID, USER, MSGBOX
     *     DATA: COMMAND, TOKEN_ID, MESSAGE
     *     RETR: COMMAND, TOKEN_ID, USER, MSGBOX
     *     FRND: COMMAND, TOKEN_ID
     *     HRBT: COMMAMD, TOKEN_ID
     *     GBYE: COMMAND, TOKEN_ID
     * @throws IOException
     */
    public Map<TotpField, String> receiveReq() throws IOException {
        String req = read();
        return parseReq(req);
    }

    protected Map<TotpField, String> parseReq(String req) throws IOException {
        Map<TotpField, String> map = new HashMap<>();
        String[] decapReq = decapToken(req);
        map.put(TotpField.TOKEN_ID, decapReq[0]);
        req = decapReq[1];
        Pattern pattern = Pattern.compile("^(\\w+)(?:\r\n|\\s)?([\\s\\S]+)?\r\n$");
        Matcher matcher = pattern.matcher(req);
        String cmd = "", arg = "";
        if (matcher.find()) {
             cmd = matcher.group(1);
            if (matcher.groupCount() > 1)
                arg = matcher.group(2);
        } else {
            response(TotpCmd.ERROR, TotpStatus.ERROR_PARAMETERS_ARGUMENTS);
        }
        map.put(TotpField.COMMAND, cmd);
        switch (TotpCmd.valueOf(cmd)) {
            case HELO:
                break;
            case PASS:
                pattern = Pattern.compile("(\\w+\\b)\\s(\\w+)");
                matcher = pattern.matcher(arg);
                if (matcher.find()) {
                    String user = matcher.group(1);
                    String password = matcher.group(2);
                    map.put(TotpField.USER, user);
                    map.put(TotpField.PASSWORD, password);
                } else {
                    response(TotpCmd.ERROR, TotpStatus.ERROR_PARAMETERS_ARGUMENTS);
                }
                break;
            case SEND:
                pattern = Pattern.compile("(\\w+):(\\w+)");
                matcher = pattern.matcher(arg);
                if (matcher.find()) {
                    String recipient = matcher.group(1);
                    String msgbox = matcher.group(2);
                    map.put(TotpField.USER, recipient);
                    map.put(TotpField.MSGBOX, msgbox);
                } else {
                    response(TotpCmd.ERROR, TotpStatus.ERROR_PARAMETERS_ARGUMENTS);
                }
                break;
            case DATA:
                pattern = Pattern.compile("(\\d+)\\s(\\d+)\r\n([\\s\\S]+)\r\n.$");
                matcher = pattern.matcher(req);
                if (matcher.find()) {
                    int numOfMsg = Integer.valueOf(matcher.group(1));
                    int totalSize = Integer.valueOf(matcher.group(2));
                    String[] msgs = matcher.group(3).split("\r\n");
                    for (String msg : msgs) totalSize -= msg.length();
                    if (numOfMsg != msgs.length || totalSize != 0) {
                        setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                    }
                    map.put(TotpField.MESSAGE, msgs[0]);
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }

                break;
            case RETR:
                pattern = Pattern.compile("(\\w+):(\\w+)");
                matcher = pattern.matcher(arg);
                if (matcher.find()) {
                    String user = matcher.group(1);
                    String msgbox = matcher.group(2);
                    map.put(TotpField.USER, user);
                    map.put(TotpField.MSGBOX, msgbox);
                } else {
                    response(TotpCmd.ERROR, TotpStatus.ERROR_PARAMETERS_ARGUMENTS);
                }
                break;
            case FRND:
                break;
            case HRBT:
                break;
            case GBYE:
                break;
            default:
                response(TotpCmd.ERROR, TotpStatus.COMMAND_UNRECOGNIZED);
        }
        return map;
    }

    @Override
    protected TotpContent parseResp(TotpCmd cmd, String resp) {
        Pattern pattern;
        Matcher matcher;
        TotpContent totpContent = new TotpContent();
        switch (cmd) {
            case PUSH:
                pattern = Pattern.compile("(\\d+)\r\n");
                matcher = pattern.matcher(resp);
                if (matcher.find()) {
                    totpContent.status = TotpStatus.valueOf(Integer.valueOf(matcher.group(1)));
                } else {
                    setError(TotpStatus.ERROR_PARAMETERS_ARGUMENTS.getReasonPhrase());
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
        return totpContent;
    }

    private String[] decapToken(String req) {
        String[] decapReq = new String[2];
        Pattern pattern = Pattern.compile("^(\\w{" + tokenLen + "}\\b)\\s([\\s\\S]+)$");
        Matcher matcher = pattern.matcher(req);
        if (!matcher.matches()) {
            decapReq[0] = "";
            decapReq[1] = req;
        }
        else {
            decapReq[0] = matcher.group(1);
            decapReq[1] = matcher.group(2);
        }
        return decapReq;
    }

    /**
     * Set the token_id length.
     * @param length The length of token_id
     */
    public void setTokenLength(int length) {
        this.tokenLen = length;
    }
}
