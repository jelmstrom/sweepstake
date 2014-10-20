package com.jelmstrom.tips.match;

import java.util.List;

/**
 * Created by jelmstrom on 20/10/14.
 */
public interface MatchRepository {
    void store(Match match);

    Match read(String matchId);

    List<Match> read();

    void store(List<Match> matches);
}
