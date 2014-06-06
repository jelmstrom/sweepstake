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

    private static final DB db = getMongoClient().getDB("sweepstake");



    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static DB getDb() {
        return db;
    }
}
