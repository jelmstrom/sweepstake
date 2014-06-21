package com.jelmstrom.tips.match;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.user.User;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

public class Match implements Comparable<Match>{
    public final String homeTeam;
    public final String awayTeam;
    public final Date matchStart;
    @JsonManagedReference
    public final HashSet<Result> results;
    public final String id;
    private Result correctResult;



    public Match(String homeTeam, String awayTeam, Date matchStart, String id) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchStart = matchStart;
        this.id = id;
        results = new HashSet<>();
    }

    public void add(Result result){
        results.remove(result);
        results.add(result);
    }

    public Result resultFor(String userId) {
        Optional<Result> option = results.stream().filter(result -> result.userId.equals(userId)).findFirst();
        if(option.isPresent()){
            return option.get();
        } else {
            return null;
        }
    }


    public boolean equals(Object other){
        if(other instanceof Match){
            Match that = (Match) other;
            return this.awayTeam.equals(((Match) other).awayTeam)
                    && this.homeTeam.equals(that.homeTeam)
                    && this.matchStart.equals(that.matchStart)
                    && this.results.hashCode() == that.results.hashCode()
                    && this.id.equals(that.id);

        }
        return false;
    }

    public int scoreFor(String user) {
        return userScore(resultFor(user));
    }

    private int userScore(Result userResult) {
        if(null == userResult || correctResult == null){
            return 0;
        }

        int points = 0;
        if (userResult.winner() == correctResult.winner()) {
            points++;
        }
        if (userResult.homeGoals == correctResult.homeGoals) {
            points++;
        }
        if (userResult.awayGoals == correctResult.awayGoals) {
            points++;
        }

        return points;
    }

    public void setCorrectResult(Result correctResult) {
        this.correctResult = correctResult;
    }

    public Result getCorrectResult() {
        return correctResult;
    }


    @SuppressWarnings("UnusedDeclaration")
    public boolean hasResult(){
        return correctResult != null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String homeGoalsAsStringFor(String email){
        Result result =  resultFor(email);
        return result==null?"":Integer.toString(result.homeGoals);
    }
    @SuppressWarnings("UnusedDeclaration")
    public String awayGoalsAsStringFor(String email){
        Result result =  resultFor(email);
        return result==null?"":Integer.toString(result.awayGoals);
    }

    @Override
    public int compareTo(Match o) {
        return matchStart.compareTo(o.matchStart);
    }

    public boolean editable(User user){
        if(user.admin){
            return true;
        } else {
            return new Date().before(Config.startDate);
        }
    }

}
