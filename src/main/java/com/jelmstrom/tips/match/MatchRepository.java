package com.jelmstrom.tips.match;

import java.util.List;
import java.util.SortedMap;

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
    void addRelation(Match match, String label, Match nextStage);


    SortedMap<Match.Stage, List<Match>> getPlayoffMatches();
}
