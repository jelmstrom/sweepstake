package com.jelmstrom.tips.persistence;

import com.mongodb.*;

import java.net.UnknownHostException;

public class MongoRepository {

    private static final MongoClient mongoClient;

    static {
        try {
            mongoClient = new MongoClient("127.0.0.1", 27017);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Database not initialized properly");
        }
    }

    private final DB db;

    protected MongoRepository(String context){
        db = getMongoClient().getDB(context);
    }


    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public DB getDb() {
        return db;
    }


}
