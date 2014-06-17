package com.jelmstrom.tips.match;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class Result {
    @JsonBackReference
    public final Match match;
    public final int homeGoals;
    public final int awayGoals;
    public String userId;

    public Result(Match match, int homeGoals, int awayGoals, String userId) {
        this.match = match;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.userId = userId;
        match.add(this);
    }


    public static Result emptyResult() {
        return new Result(new Match("", "", null, ""), 0, 0, "");
    }

    public int goalsAgainst(String team) {
        if(match.homeTeam.equals(team)){
            return awayGoals;
        } else if(match.awayTeam.equals(team)){
            return homeGoals;
        }
        return 0;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int goalsFor(String team) {
        if(match.homeTeam.equals(team)){
            return homeGoals;
        } else if(match.awayTeam.equals(team)){
            return awayGoals;
        }
        return 0;
    }

    public int pointsFor(String team) {
        switch (winner()){
            case 1: return match.homeTeam.equals(team)?3:0;
            case 0: return match.homeTeam.equals(team) || match.awayTeam.equals(team)?1:0;
            case -1:return team.equals(match.awayTeam)?3:0;
            default : throw new IllegalStateException("winner returned incorrect value");
       }
    }

    public int winner() {
        return Integer.signum(Integer.compare(homeGoals, awayGoals));
    }

    public boolean equals(Object other){
        if(other instanceof Result){
            Result that = (Result) other;
            return this.userId.equals(that.userId)
                    && this.match.id.equals(that.match.id);
        }
        return false;
    }

    public int hashCode(){
        return 31 *  (userId.hashCode() * match.id.hashCode());
    }

    public int score() {
        return this.match.scoreFor(this.userId);
    }
}
