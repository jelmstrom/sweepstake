package com.jelmstrom.tips.match;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.jelmstrom.tips.match.Match.Stage;
import static java.util.stream.Collectors.toList;

public class MongoMatchRepository extends MongoRepository implements MatchRepository {

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
    private static final String GROUP_ID = "groupId";
    public final DBCollection matchCollection;

    public MongoMatchRepository(String context) {
        super(context);
        matchCollection = getDb().getCollection("matches");
    }

    @Override
    public Match store(Match match) {
        List<DBObject> results =
                match.results.stream()
                        .map(this::buildDbObject)
                        .collect(toList());


        Match persisted = read(match.getMatchId());
        if (persisted.getMatchId().equals(match.getMatchId())) {
            DBObject entry = new BasicDBObject(HOME_TEAM, match.homeTeam)
                    .append(AWAY_TEAM, match.awayTeam)
                    .append(MATCH_START, match.matchStart.getTime())
                    .append(RESULTS, results)
                    .append(STAGE, match.stage.toString())
                    .append(GROUP_ID, match.groupId)
                    .append(MATCH_ID, match.getMatchId())
                    .append(CORRECT_RESULT, buildDbObject(match.getCorrectResult()));
            matchCollection.update(new BasicDBObject(MATCH_ID, match.getMatchId()), entry);
        } else {
            long nodeId = UUID.randomUUID().hashCode();
            DBObject entry = new BasicDBObject(HOME_TEAM, match.homeTeam)
                    .append(AWAY_TEAM, match.awayTeam)
                    .append(MATCH_START, match.matchStart.getTime())
                    .append(RESULTS, results)
                    .append(STAGE, match.stage.toString())
                    .append(GROUP_ID, match.groupId)
                    .append(MATCH_ID, nodeId)
                    .append(CORRECT_RESULT, buildDbObject(match.getCorrectResult()));
            matchCollection.insert(entry);
            match.setMatchId(nodeId);
        }
        return match;
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

    @Override
    public Match read(Long matchId) {
        DBObject dbMatch = matchCollection.findOne(new BasicDBObject(MATCH_ID, matchId));
        if (dbMatch != null && null != dbMatch.get(MATCH_ID)) {
            return buildMatch(dbMatch);
        }
        Match m =  new Match("", "", new Date(), -1L);
        m.setMatchId(-1L);
        return m;
    }

    @Override
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
                ,
                stage,
                Long.parseLong(dbMatch.get(GROUP_ID).toString()));
        match.setMatchId((long) dbMatch.get(MATCH_ID));
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
                , Long.parseLong(dbResult.getString(USER_ID))
                , dbResult.getString(PROMOTED));
    }

    @Override
    public void store(List<Match> matches) {
        matches.stream().forEach(this::store);
    }

    @Override
    public void dropAll() {
        matchCollection.drop();
    }

    @Override
    public List<Result> userPredictions(long userId) {
        return null;
    }
}
