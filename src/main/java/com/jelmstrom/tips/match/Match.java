package com.jelmstrom.tips.match;


import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;


public class Match implements Comparable<Match>{
    public final String homeTeam;
    public final String awayTeam;
    public final Date matchStart;
    @JsonManagedReference
    public final HashSet<Result> results;
    public final String id;

    public Match(String homeTeam, String awayTeam, Date matchStart, String id) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchStart = matchStart;
        this.id = id;
        results = new HashSet<>();
    }

    public void add(Result result){
        results.add(result);
    }

    public Result resultFor(String userEmail) {
        Optional<Result> option = results.stream().filter(result -> result.userEmail.equals(userEmail)).findFirst();
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
        Result adminResult;
        if(null == userResult
                || (adminResult =  resultFor("none@noreply.zzz")) == null){
            return 0;
        }

        int points = 0;
        if (userResult.winner() == adminResult.winner()) {
            points++;
        }
        if (userResult.homeGoals == adminResult.homeGoals) {
            points++;
        }
        if (userResult.awayGoals == adminResult.awayGoals) {
            points++;
        }

        return points;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean hasResult(){
        return resultFor("none@noreply.zzz") != null;
    }

    public String homeGoalsAsStringFor(String email){
        Result result =  resultFor(email);
        return result==null?"":Integer.toString(result.homeGoals);
    }
    public String awayGoalsAsStringFor(String email){
        Result result =  resultFor(email);
        return result==null?"":Integer.toString(result.awayGoals);
    }

    @Override
    public int compareTo(Match o) {
        return matchStart.compareTo(o.matchStart);
    }

}
