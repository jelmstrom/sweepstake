package com.jelmstrom.tips.match;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jelmstrom.tips.user.User;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static com.jelmstrom.tips.configuration.Config.startDate;
import static com.jelmstrom.tips.match.Match.Stage.GROUP;

public class Match implements Comparable<Match>{
    public final String homeTeam;
    public final String awayTeam;
    public final Date matchStart;
    public final Stage stage;
    @JsonManagedReference
    public final HashSet<Result> results;
    public final String id;
    private Result correctResult;



    public enum Stage {
        GROUP(0), LAST_SIXTEEN(2), QUARTER_FINAL(4), SEMI_FINAL(8), FINAL(16), BRONZE(12);
        public final int factor;
        Stage(int factor) {
            this.factor = factor;
        }
    }


    public Match(String homeTeam, String awayTeam, Date matchStart, String id) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchStart = matchStart;
        this.id = id;
        this.stage = GROUP;
        results = new HashSet<>();
    }

    public Match(String homeTeam, String awayTeam, Date matchStart, String id, Stage stage) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchStart = matchStart;
        this.id = id;
        this.stage = stage;
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
            return Result.emptyResult();
        }
    }


    public boolean equals(Object other){
        if(other instanceof Match){
            Match that = (Match) other;
            return this.awayTeam.equals(((Match) other).awayTeam)
                    && this.homeTeam.equals(that.homeTeam)
                    && this.matchStart.equals(that.matchStart)
                    && this.results.equals(that.results)
                    && this.id.equals(that.id)
                    && this.stage.equals(that.stage)
                    && ObjectUtils.nullSafeEquals(this.correctResult, that.correctResult);
        }
        return false;
    }

    public int scoreFor(String user) {
        return userScore(resultFor(user));
    }

    private int userScore(Result userResult) {
        if(null == userResult || !userResult.isValid()
           || correctResult == null || !correctResult.isValid()){
            return 0;
        }

        int points = 0;
        if (userResult.winner() == correctResult.winner()) {
            points++;
        }
        if (userResult.homeGoals.equals(correctResult.homeGoals)) {
            points++;
        }
        if (userResult.awayGoals.equals(correctResult.awayGoals)) {
            points++;
        }

        if(!userResult.promoted.equals("") &&
                userResult.promoted.equals(correctResult.promoted)){
            points = points+ stage.factor;
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

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Match o) {
       return matchStart.compareTo(o.matchStart);
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean editable(User user){
        if(user.admin){
            return true;
        } else if(user.isValid()){
            Date date = new Date();
            return stage==GROUP?date.before(startDate) : date.before(matchStart);
        } else {
            return false;
        }
    }


    @Override
    public String toString() {
        return "Match{" +
                "homeTeam='" + homeTeam + '\'' +
                ", awayTeam='" + awayTeam + '\'' +
                ", matchStart=" + matchStart +
                ", stage=" + stage +
                ", id='" + id + '\'' +
                ", correctResult=" + (correctResult==null?"null":(correctResult.homeGoals +":" +correctResult.awayGoals))+
                '}';
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isValid() {
        return !(homeTeam.equals("") || awayTeam.equals("") || startDate == null || stage == null || id.equals("")) ;
    }

}
