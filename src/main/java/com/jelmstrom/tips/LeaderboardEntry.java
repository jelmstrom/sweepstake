package com.jelmstrom.tips;

import com.jelmstrom.tips.user.User;

public class LeaderboardEntry implements Comparable {

    public final User user;
    public final Integer points;

    public LeaderboardEntry(User user, Integer points) {
        this.user = user;
        this.points = points;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Object o) {
        LeaderboardEntry that = (LeaderboardEntry) o;
        return that.points.compareTo(this.points);
    }
}
