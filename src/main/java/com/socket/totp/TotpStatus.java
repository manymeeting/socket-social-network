package com.socket.totp;

public enum TotpStatus {
    SUCCESS(200, "General Success"),
    SYSTEM_STATUS(210, "System Status"),
    SERVICE_READY(2210, "Service Ready"),
    SERVICE_CLOSE(221, "Service Closing Transmission Channel"),
    TRANSFER_ACTION_COMPLETED(250, "Transfer Action Completed"),
    READY_LIST_RECEIVING(330, "Ready For List Receiving; End With <CRLF>.<CRLF>"),
    START_LIST_TRANSMISSION(331, "Start List Transmission; End With <CRLF>.<CRLF>"),
    SERVICE_NOT_AVAILABLE(421, "Service Not Available, Closing Transmission Channel"),
    UNKNOWN_RECIPIENT(450, "Unknown Recipient"),
    LOCAL_PROCESSING_ERROR(451, "Local Processing Error"),
    COMMAND_UNRECOGNIZED(500, "Command Unrecognized"),
    ERROR_PARAMETERS_ARGUMENTS(501, "Error In Parameters Or Arguments"),
    COMMAND_NOT_IMPLEMENTED(502, "Command Not Implemented"),
    TRANSMISSION_FAILED(530, "Transmission Failed"),
    PERMISSION_FAILED(540, "Permission_FAILED"),
    AUTHENTICATION_REQUIRED(541, "Authentication Required");

    private final int value;
    private final String reasonPhrase;

    TotpStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    public int getValue() { return value; }

    public int value() {
        return this.value;
    }

    public String getReasonPhrase() { return reasonPhrase; }

    public static TotpStatus valueOf(int statusCode) {
        TotpStatus status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        }
        return status;
    }

    public static TotpStatus resolve(int statusCode) {
        for (TotpStatus status : values()) {
            if (status.value == statusCode) {
                return status;
            }
        }
        return null;
    }

}
