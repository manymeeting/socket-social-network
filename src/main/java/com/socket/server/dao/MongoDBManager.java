package com.socket.server.dao;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


import java.util.ArrayList;
import java.util.List;


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


    /**
     * For functional test only
     */
    public static void main(String[] args) {
        MongoDBManager db = new MongoDBManager();
        //testing when user1 login successfully
        System.out.println(db.isValidUser("user1", "123"));
        //testing when user2 login unsuccessfully
        System.out.println(db.isValidUser("user2", "12"));
        //testing when user4 login unsuccessfully
        System.out.println(db.isValidUser("user4", "123"));
    }
}
