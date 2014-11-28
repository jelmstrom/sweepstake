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
    private Long id;

    public final String homeTeam;
    public final String awayTeam;
    public final Date matchStart;
    public final Stage stage;
    @JsonManagedReference
    public final HashSet<Result> results;
    private Result correctResult;
    public final Long groupId;


    public enum Stage {
        GROUP(0), LAST_SIXTEEN(2), QUARTER_FINAL(4), SEMI_FINAL(8), FINAL(16), BRONZE(12);
        public final int factor;
        Stage(int factor) {
            this.factor = factor;
        }
    }


    public Match(String homeTeam, String awayTeam, Date matchStart, Long groupId) {
        this(homeTeam, awayTeam, matchStart, Stage.GROUP, groupId);
    }

    public Match(String homeTeam, String awayTeam, Date matchStart, Stage stage, Long groupId) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchStart = matchStart;
        this.stage = stage;
        results = new HashSet<>();
        this.groupId = groupId;
        this.correctResult = Result.emptyResult(this);

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void add(Result result){
        if(result.isValid()){
            results.remove(result);
            results.add(result);
        }
    }

    public Result resultFor(Long userId) {
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
                    && this.stage.equals(that.stage)
                    && ObjectUtils.nullSafeEquals(this.correctResult, that.correctResult)
                    && ObjectUtils.nullSafeEquals(this.id, that.id);
        }
        return false;
    }

    public int scoreFor(Long user) {
        return userScore(resultFor(user));
    }

    private int userScore(Result userResult) {
        if(null == userResult || correctResult == null){
            return 0;
        }

        int points = 0;
        if(userResult.isValid() && correctResult.isValid()){
            if (userResult.winner() == correctResult.winner()) {
                points++;
            }
            if (userResult.homeGoals.equals(correctResult.homeGoals)) {
                points++;
            }
            if (userResult.awayGoals.equals(correctResult.awayGoals)) {
                points++;
            }
        }

        if(!userResult.promoted.equals("") &&
                userResult.promoted.equals(correctResult.promoted)){
            points = points+ stage.factor;
        }

        return points;
    }

    public void setCorrectResult(Result correctResult) {
        if(correctResult.isValid()){
            this.correctResult = correctResult;
        }
    }

    public Result getCorrectResult() {
        return correctResult;
    }


    @SuppressWarnings("UnusedDeclaration")
    public boolean hasResult(){
        return correctResult != null && correctResult.isValid();
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
                ", correctResult=" + (correctResult==null?"null":(correctResult.homeGoals +":" +correctResult.awayGoals))+
                '}';
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isValid() {
        return !(homeTeam.equals("") || awayTeam.equals("") || startDate == null || stage == null || groupId==null || groupId.equals(-1L) ) ;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
