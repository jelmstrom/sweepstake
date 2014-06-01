package com.jelmstrom.tips;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MatchRepository {

    private static final MongoClient mongoClient;

    static {
        try {
            mongoClient = new MongoClient("127.0.0.1", 27017);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Database not initialized properly");
        }
    }

    private static final DB db = mongoClient.getDB("sweepstake");

    private static final DBCollection matchCollection = db.getCollection("matches");

    public static void store(Match match) {
        List <DBObject> results =
                match.results.stream()
                        .map(result -> new BasicDBObject("homeGoals", result.homeGoals)
                                .append("awayGoals", result.awayGoals)
                                .append("user", result.user))
                        .collect(toList());

        DBObject entry = new BasicDBObject("homeTeam", match.homeTeam)
                    .append("awayTeam", match.awayTeam)
                    .append("matchStart", match.matchStart.getTime())
                    .append("results", results)
                    .append("matchId", match.id);
        matchCollection.insert(entry);

    }

    public static Match read(String matchId) {
        DBObject dbMatch = matchCollection.findOne(new BasicDBObject("matchId", matchId));
        Match match = buildMatch(dbMatch);

        return match;
    }

    public static List<Match> getAll(){
        List<Match> matches = new ArrayList();
        DBCursor dbObjects = matchCollection.find();
        dbObjects.forEach(dbMatch -> matches.add(buildMatch(dbMatch)));
        return matches;
    }

    private static Match buildMatch(DBObject dbMatch) {
        Match match = new Match(dbMatch.get("homeTeam").toString()
                , dbMatch.get("awayTeam").toString()
                , new Date(Long.parseLong(dbMatch.get("matchStart").toString()))
                , dbMatch.get("matchId").toString());
        BasicDBList dbResults= (BasicDBList) dbMatch.get("results");
        BasicDBObject[] dbObjects = new  BasicDBObject[dbResults.size()];
        dbResults.toArray(dbObjects);
        for(BasicDBObject dbResult : dbObjects){
            new Result(match
                    , Integer.parseInt(dbResult.get("homeGoals").toString())
                    , Integer.parseInt(dbResult.get("awayGoals").toString())
                    , dbResult.get("user").toString());
        }
        return match;
    }
}
