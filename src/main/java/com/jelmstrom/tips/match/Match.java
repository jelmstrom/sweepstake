package com.jelmstrom.tips.match;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

public class Match {
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

    public Result resultFor(String user) {
        Optional<Result> maybe = results.stream().filter(result -> result.user.equals(user)).findFirst();
        if(maybe.isPresent()){
            return maybe.get();
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
}
