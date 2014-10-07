package com.jelmstrom.tips.match;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.jelmstrom.tips.match.Match.Stage;
import static java.util.stream.Collectors.toList;

public class MatchRepository extends MongoRepository {

    public static final String PROMOTED = "promoted";
    public static final String USER_ID = "userId";
    public static final String AWAY_GOALS = "awayGoals";
    public static final String HOME_GOALS = "homeGoals";
    public static final String MATCH_ID = "matchId";
    public static final String CORRECT_RESULT = "correctResult";
    public static final String RESULTS = "results";
    public static final String AWAY_TEAM = "awayTeam";
    public static final String HOME_TEAM = "homeTeam";
    public static final String STAGE = "stage";
    public static final String MATCH_START = "matchStart";
    public final DBCollection matchCollection;

    public MatchRepository(String context) {
        super(context);
        matchCollection = getDb().getCollection("matches");
    }

    public void store(Match match) {
        List<DBObject> results =
                match.results.stream()
                        .map(this::buildDbObject)
                        .collect(toList());

        DBObject entry = new BasicDBObject(HOME_TEAM, match.homeTeam)
                .append(AWAY_TEAM, match.awayTeam)
                .append(MATCH_START, match.matchStart.getTime())
                .append(RESULTS, results)
                .append(MATCH_ID, match.id)
                .append(STAGE, match.stage.toString())
                .append(CORRECT_RESULT, buildDbObject(match.getCorrectResult())
                );

        Match persisted = read(match.id);
        if (persisted.id.equals(match.id)) {
            matchCollection.update(new BasicDBObject(MATCH_ID, match.id), entry);
        } else {
            matchCollection.insert(entry);
        }
    }

    private BasicDBObject buildDbObject(Result result) {
        if(result == null) {
            return null;
        }
        return new BasicDBObject(HOME_GOALS, result.homeGoals)
                .append(AWAY_GOALS, result.awayGoals)
                .append(USER_ID, result.userId)
                .append(PROMOTED, result.promoted);
    }

    public Match read(String matchId) {
        DBObject dbMatch = matchCollection.findOne(new BasicDBObject(MATCH_ID, matchId));
        if (dbMatch != null && null != dbMatch.get(MATCH_ID)) {
            return buildMatch(dbMatch);
        }
        return new Match("", "", new Date(), "");
    }

    public List<Match> read() {
        return matchCollection.find().toArray().parallelStream().map(this::buildMatch).collect(toList());
    }

    private Match buildMatch(DBObject dbMatch) {
        Stage stage = Stage.GROUP;
        String dbStage = (String) dbMatch.get(STAGE);
        if(null != dbStage){
            stage = Stage.valueOf(dbStage);
        }
        Match match = new Match((String)dbMatch.get(HOME_TEAM)
                , (String)dbMatch.get(AWAY_TEAM)
                , new Date(Long.parseLong(dbMatch.get(MATCH_START).toString()))
                , dbMatch.get(MATCH_ID).toString()
                , stage
                );
        BasicDBList dbResults = (BasicDBList) dbMatch.get(RESULTS);
        BasicDBObject[] dbObjects = new BasicDBObject[dbResults.size()];
        dbResults.toArray(dbObjects);
        for (BasicDBObject dbResult : dbObjects) {
            newResult(match, dbResult);
        }
        match.setCorrectResult(newResult(match, (BasicDBObject) dbMatch.get(CORRECT_RESULT)));
        return match;
    }

    private Result newResult(Match match, BasicDBObject dbResult) {
        if(dbResult == null){
            return null;
        }

        Integer homeGoals = dbResult.get(HOME_GOALS)==null?
                        null:
                        dbResult.getInt(HOME_GOALS);
        Integer awayGoals = dbResult.get(AWAY_GOALS)==null?
                        null:
                        dbResult.getInt(AWAY_GOALS);

        return new Result(match
                , homeGoals
                , awayGoals
                , dbResult.getString(USER_ID)
                , dbResult.getString(PROMOTED));
    }

    public void store(List<Match> matches) {
        matches.stream().forEach(this::store);
    }
}
