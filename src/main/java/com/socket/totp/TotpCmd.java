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
}
