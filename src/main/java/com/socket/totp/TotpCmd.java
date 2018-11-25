package com.socket.totp;

public enum TotpCmd {
    HELO("HELO"),
    PASS("PASS"),
    PUSH("PUSH"),
    SEND("SEND"),
    DATA("DATA"),
    RETR("RETR"),
    FRND("FRND"),
    HRBT("HRBT"),
    GBYE("GBYE"),
    ERROR("");

    private final String cmd;

    TotpCmd(String cmd) {
        this.cmd = cmd;
    }

//    public static TotpCmd valueOf(String command) {
//        TotpCmd cmd = resolve(command);
//        if (cmd == null) {
//            throw new IllegalArgumentException("No matching constant for [" + command + "]");
//        }
//        return cmd;
//    }
//
//    public static TotpCmd resolve(String command) {
//        for (TotpCmd cmd : values()) {
//            if (cmd.cmd == command) {
//                return cmd;
//            }
//        }
//        return null;
//    }
}
