package com.socket.server.dao;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
    MongoDB admin info:
    username: admin
    password: pwd123
    ----------------------
    MongoDB "user" Collection:
    username: "user1"
    password: "123"

    username: "user2"
    password: "123"

    username: "user3"
    password: "123"
 */
public class MongoDBManager {

    MongoClient dbClient;
    MongoDatabase db;

    //connecting DB
    public MongoDBManager() {
        try {
            this.dbClient = new MongoClient(new MongoClientURI("mongodb://admin:pwd123@ds243963.mlab.com:43963/cmpe207"));
        } catch (Exception e) {
            System.out.println("Failed to connect to mongo client.");
        }

        this.db = dbClient.getDatabase("cmpe207");
    }

    /**
     * Using "user" collection to store login info
     */
    public boolean isValidUser(String userName, String pwd) {
        MongoCollection userGroup = db.getCollection("user");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("username", userName);
        List<Document> docs = (List<Document>)userGroup.find(whereQuery).into(new ArrayList<>());

        if (docs == null) return false;
        for (Document doc : docs) {
            if (doc.get("password").equals(pwd)) {
                return true;
            }
        }
        return false;
    }

    public boolean addMessageInfo(String receiver, String sender, String message, Date timeStamp) {
        Document doc = new Document();
        doc.append("message", message);
        doc.append("timeStamp", timeStamp);
        doc.append("from", sender);
        doc.append("to", receiver);
        MongoCollection msgGroup = db.getCollection("message");
        if (msgGroup == null) {
            db.createCollection("message");
            msgGroup = db.getCollection("message");
        }
        msgGroup.insertOne(doc);
        return true;
    }

    public List<String> getUnreadMsg(Date timeStamp) {
        if (timeStamp == null) {
            return new ArrayList<>();
        }
        MongoCollection msgGroup = this.db.getCollection("message");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("timeStamp", new BasicDBObject("$gte", timeStamp));
        List<Document> docs = (List<Document>)msgGroup.find(whereQuery).into(new ArrayList<>());
        System.out.println(docs.size());
        List<String> result = new ArrayList<>();
        for (Document doc : docs) {
            result.add((String) doc.get("message"));
        }

        return result;
    }

    /**
     * For functional test only
     */
    public static void main(String[] args) throws ParseException {
        MongoDBManager db = new MongoDBManager();
        //testing when user1 login successfully
        System.out.println(db.isValidUser("user1", "123"));
        //testing when user2 login unsuccessfully
        System.out.println(db.isValidUser("user2", "12"));
        //testing when user4 login unsuccessfully
        System.out.println(db.isValidUser("user4", "123"));

        //db.addMessageInfo("user1", "user2", "hello from user1", new Date());
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date date = format.parse("2018-11-29T03:19:41Z");
        System.out.println("input:" + date);
        List<String> output = db.getUnreadMsg(new Date());
        System.out.println(output);

    }
}
