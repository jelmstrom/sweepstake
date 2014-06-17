package com.jelmstrom.tips.match;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MatchRepository extends MongoRepository {

    public final DBCollection matchCollection;

    public MatchRepository(String context) {
        super(context);
        matchCollection = getDb().getCollection("matches");
    }

    public void store(Match match) {
        List<DBObject> results =
                match.results.stream()
                        .map(result -> buildDbObject(result))
                        .collect(toList());

        DBObject entry = new BasicDBObject("homeTeam", match.homeTeam)
                .append("awayTeam", match.awayTeam)
                .append("matchStart", match.matchStart.getTime())
                .append("results", results)
                .append("matchId", match.id)
                .append("correctResult", buildDbObject(match.getCorrectResult()));
        Match persisted = read(match.id);
        if (persisted.id.equals(match.id)) {
            matchCollection.update(new BasicDBObject("matchId", match.id), entry);
        } else {
            matchCollection.insert(entry);
        }
    }

    public BasicDBObject buildDbObject(Result result) {
        if(result == null) {
            return null;
        }
        return new BasicDBObject("homeGoals", result.homeGoals)
                .append("awayGoals", result.awayGoals)
                .append("userId", result.userId);
    }

    public Match read(String matchId) {
        DBObject dbMatch = matchCollection.findOne(new BasicDBObject("matchId", matchId));
        if (dbMatch != null && null != dbMatch.get("matchId")) {
            return buildMatch(dbMatch);
        }
        return new Match("", "", new Date(), "");
    }

    public List<Match> read() {
        return matchCollection.find().toArray().parallelStream().map(this::buildMatch).collect(toList());
    }

    private Match buildMatch(DBObject dbMatch) {
        Match match = new Match(dbMatch.get("homeTeam").toString()
                , dbMatch.get("awayTeam").toString()
                , new Date(Long.parseLong(dbMatch.get("matchStart").toString()))
                , dbMatch.get("matchId").toString()
                );
        BasicDBList dbResults = (BasicDBList) dbMatch.get("results");
        BasicDBObject[] dbObjects = new BasicDBObject[dbResults.size()];
        dbResults.toArray(dbObjects);
        for (BasicDBObject dbResult : dbObjects) {
            newResult(match, dbResult);
        }
        match.setCorrectResult(newResult(match, (BasicDBObject) dbMatch.get("correctResult")));
        return match;
    }

    private Result newResult(Match match, BasicDBObject dbResult) {
        if(dbResult == null){
            return null;
        }
        return new Result(match
                , dbResult.getInt("homeGoals")
                , dbResult.getInt("awayGoals")
                , dbResult.getString("userId"));
    }

    public void store(List<Match> matches) {
        matches.stream().forEach(this::store);
    }

    public void remove(List<Match> matches) {
        matches.stream().forEach(this::remove);
    }

    public void remove(Match match) {
        matchCollection.remove(new BasicDBObject("matchId", match.id));

    }


}
