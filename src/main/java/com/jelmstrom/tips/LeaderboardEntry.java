package com.jelmstrom.tips;

public class LeaderboardEntry implements Comparable {

    public final String user;
    public final Integer points;

    public LeaderboardEntry(String user, Integer points) {
        this.user = user;
        this.points = points;
    }


    @Override
    public int compareTo(Object o) {
        LeaderboardEntry that = (LeaderboardEntry) o;
        return that.points.compareTo(this.points);
    }
}
