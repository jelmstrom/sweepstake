package com.jelmstrom.tips.match;

import java.util.List;

public interface MatchRepository {
    void store(Match match);
    Match read(String matchId);
    List<Match> read();
    void store(List<Match> matches);
    void dropAll();

}
