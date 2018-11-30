package com.socket.server.dao;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.text.Document;


/**
    MongoDB admin info:
    username: admin
    password: pwd123
 */
public class MongoDBManager {

    MongoClient dbClient;


    //connecting DB
    public MongoDBManager() {
        try {
            this.dbClient = new MongoClient(new MongoClientURI("mongodb://admin:pwd123@ds243963.mlab.com:43963/cmpe207"));
        } catch (Exception e) {
            System.out.println("Failed connect to mongo client.");
        }

        MongoDatabase db = dbClient.getDatabase("cmpe207");
        MongoCollection collection = db.getCollection("user");

        //System.out.println(collection.find().first());
    }


    /**
     * For functional test only
     */
    public static void main(String[] args) {
        MongoDBManager db = new MongoDBManager();
    }
}
