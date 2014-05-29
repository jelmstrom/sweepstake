package com.jelmstrom.tips;


import java.sql.Timestamp;
import java.util.List;

public class Match {
    private final String homeTeam;
    private final String awayTeam;


    private final String matchStart;
    private final int homeGoals;
    private final int awayGoals;

    public Match(String homeTeam, String awayTeam, int homeGoals, int awayGoals, String matchStart) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.matchStart = matchStart;
    }


    public int winner() {
        return Integer.signum(Integer.compare(homeGoals, awayGoals));
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public int pointsFor(String team) {
        switch (winner()){
            case 1: return homeTeam.equals(team)?3:0;
            case 0: return homeTeam.equals(team) || awayTeam.equals(team)?1:0;
            case -1:return team.equals(awayTeam)?3:0;
            default : throw new IllegalStateException("winner returned incorrect value");

        }
    }

    public String getMatchStart() {
        return matchStart;
    }

    public int goalsFor(String team) {
        if(homeTeam.equals(team)){
            return homeGoals;
        } else if(awayTeam.equals(team)){
            return awayGoals;
        }
        return 0;
    }

    public int goalsAgainst(String team) {
        if(homeTeam.equals(team)){
            return awayGoals;
        } else if(awayTeam.equals(team)){
            return homeGoals;
        }
        return 0;
    }
}
