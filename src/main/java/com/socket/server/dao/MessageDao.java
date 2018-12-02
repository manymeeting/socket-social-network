package com.socket.server.dao;

import com.socket.server.entity.Message;

import java.util.Date;
import java.util.List;

public class MessageDao {
    MongoDBManager dbManager;

    public MessageDao() {
        this.dbManager = new MongoDBManager();
    }

    /**
     * Get one's wall
     * @return List<Message>
     */
    public List<Message> getMessageOnUser(String userId) {
        // TODO
        return null;
    }

    /**
     * Save message to DB
     * @param message
     */
    public void saveMessage(Message message) {
        dbManager.addMessageInfo(message.getToUserId(), message.getFromUesrId(),
                message.getContent(), message.getTimeStamp());
    }


}
