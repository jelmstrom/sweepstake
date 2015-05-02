package com.jelmstrom.tips.match;

import org.neo4j.graphdb.Node;

import java.util.List;

public interface MatchRepository {
    Match store(Match match);
    Match read(Long matchId);
    List<Match> read();
    void store(List<Match> matches);
    void dropAll();
    List<Result> userPredictions(long userId);
    void drop(Long id);

    List<Match> groupMatches(Long groupId);
    List<Match> stageMatches(Match.Stage stage);


}
