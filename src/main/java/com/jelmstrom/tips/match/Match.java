package com.jelmstrom.tips.match;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jelmstrom.tips.user.UserRepository;

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

    public Result resultFor(String userEmail) {
        Optional<Result> option = results.stream().filter(result -> result.userEmail.equals(userEmail)).findFirst();
        if(option.isPresent()){
            return option.get();
        } else {
            return null;
            //until any results have been added, this result will be
            // returned for all users (including admin). Setting a default will give all users
            // maxpoints until results are entered :)
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
        if(null == userResult){
            return 0;
        }

        Result adminResult = resultFor(UserRepository.findAdminUser().email);
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

    public boolean hasResult(){
        return null != resultFor(UserRepository.findAdminUser().email);
    }
}
