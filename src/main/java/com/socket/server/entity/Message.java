package com.socket.server.entity;

public class Message {

    private String toUserId;
    private String fromUesrId;
    private String boxId;
    private String timeStamp;
    private String content;

    public Message(String toUserId, String fromUesrId, String boxId, String timeStamp, String content) {
        this.boxId = boxId;
        this.toUserId = toUserId;
        this.fromUesrId = fromUesrId;
        this.timeStamp = timeStamp;
        this.content = content;
    }

    //TODO Add other functions

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromUesrId() {
        return fromUesrId;
    }

    public void setFromUesrId(String fromUesrId) {
        this.fromUesrId = fromUesrId;
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
