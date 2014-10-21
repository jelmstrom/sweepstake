package com.jelmstrom.tips.match;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.util.StringUtils;

public class Result {
    @JsonBackReference
    public final Match match;
    public final Integer homeGoals;
    public final Integer awayGoals;
    public final String userId;
    public final String promoted;
    private Long id;

    public Result(Match match, Integer homeGoals, Integer awayGoals, String userId) {
        this.match = match;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.userId = userId;
        promoted = "";
        match.add(this);
    }

    public Result(Match match, Integer homeGoals, Integer awayGoals, String userId, String promoted) {
        this.match = match;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.userId = userId;
        this.promoted = null == promoted ? "" : promoted;
        match.add(this);
    }


    public static Result emptyResult() {
        return new Result(new Match("", "", null, ""), null, null, "");
    }

    public int goalsAgainst(String team) {
        if (match.homeTeam.equals(team)) {
            return awayGoals;
        } else if (match.awayTeam.equals(team)) {
            return homeGoals;
        }
        return 0;
    }

    public int goalsFor(String team) {
        if (match.homeTeam.equals(team)) {
            return homeGoals;
        } else if (match.awayTeam.equals(team)) {
            return awayGoals;
        }
        return 0;
    }

    public int pointsFor(String team) {
        switch (winner()) {
            case 1:
                return match.homeTeam.equals(team) ? 3 : 0;
            case 0:
                return match.homeTeam.equals(team) || match.awayTeam.equals(team) ? 1 : 0;
            case -1:
                return team.equals(match.awayTeam) ? 3 : 0;
            default:
                throw new IllegalStateException("winner returned incorrect value");
        }
    }

    public int winner() {
        if(isValid()){
            return Integer.signum(Integer.compare(homeGoals, awayGoals));
        } else {
            return 0;
        }
    }

    public boolean equals(Object other) {
        if (other instanceof Result) {
            Result that = (Result) other;
            return this.userId.equals(that.userId)
                    && this.match.id.equals(that.match.id);
        }
        return false;
    }

    public int hashCode() {
        return 31 * (userId.hashCode() * match.id.hashCode());
    }

    public int score() {
        return this.match.scoreFor(this.userId);
    }

    @Override
    public String toString() {
        return "Result{" +
                "match=" + match.id +
                ", homeGoals=" + homeGoals +
                ", awayGoals=" + awayGoals +
                ", userId='" + userId + '\'' +
                ", promoted='" + promoted + '\'' +
                '}';
    }

    public boolean isValid() {
        return this.homeGoals != null
                && this.awayGoals != null
                && !StringUtils.isEmpty(this.userId)
                && !StringUtils.isEmpty(this.match.id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
