package com.jelmstrom.tips.match;


import com.jelmstrom.tips.configuration.Config;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Date;

import static com.jelmstrom.tips.match.Match.Stage.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResultTest {

    private final String brazil = "Brazil";
    private final String germany = "Germany";


    @Test
    public void homeTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, null, -1L);
        newMatch.setId(1L);
        Result result = new Result(newMatch, 2,1, 111L);
        assertThat(result.winner(), is(1));

    }

    @Test
    public void awayTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, null, -1L);
        newMatch.setId(1L);
        Result result = new Result(newMatch, 0,1, 111L);
        assertThat(result.winner() , is(-1));
    }

    @Test
    public void equalGoalsMeansDraw(){
        Match newMatch = new Match(brazil, germany, null, -1L);
        newMatch.setId(1L);
        Result result = new Result(newMatch, 1,1, 111L);
        assertThat(result.winner() , is(0));
    }

    @Test
    public void homeTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, null, -1L);
        newMatch.setId(1L);
        Result result = new Result(newMatch, 2,1, 111L);
        assertThat(result.pointsFor(brazil), is(3));
        assertThat(result.pointsFor(germany), is(0));

    }

    @Test
    public void awayTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, null, -1L);
        newMatch.setId(10L);
        Result result = new Result(newMatch, 1,2, 111L);
        assertThat(result.pointsFor(germany), is(3));
        assertThat(result.pointsFor(brazil), is(0));
    }

    @Test
    public void bothTeamGetsOnePointsForDraw(){
        Match newMatch = new Match(brazil, germany, null, -1L);
        newMatch.setId(1L);
        Result result = new Result(newMatch, 1,1, 111L);
        assertThat(result.pointsFor(germany), is(1));
        assertThat(result.pointsFor(brazil), is(1));
    }

    @Test
    public void nonTeamGetsZeroPointsForDraw(){
        Match newMatch = new Match(brazil, germany, null, -1L);
        newMatch.setId(1L);
        Result result = new Result(newMatch, 2,1, 111L);
        assertThat(result.pointsFor("Argentina"), is(0));
    }


    @Test
    public void pointsForCorrectLast16GameShouldBe7(){
        Match lsMatch = new Match("a", "b", ZonedDateTime.now(Config.STOCKHOLM), LAST_SIXTEEN, -1L);
        lsMatch.setId(1L);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 2, 12345L, "b"));
        Result userResult = new Result(lsMatch, 1, 2, 54312L, "b");
        assertThat(userResult.score(), is(5));
    }

    @Test
    public void pointsForCorrectQuarterFinalGameShouldBe11(){
        Match lsMatch = new Match("a", "b", ZonedDateTime.now(Config.STOCKHOLM), QUARTER_FINAL, -1L);
        lsMatch.setId(1L);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 2, 12345L, "b"));
        Result userResult = new Result(lsMatch, 1, 2, 54312L, "b");
        assertThat(userResult.score(), is(7));
    }

    @Test
    public void pointsForCorrectWinnerInQuarterFinalShouldBe4(){
        Match lsMatch = new Match("a", "b", ZonedDateTime.now(Config.STOCKHOLM), QUARTER_FINAL, -1L);
        lsMatch.setId(1L);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 1, 12345L, "a"));
        Result userResult = new Result(lsMatch, 2, 0, 54312L, "a");
        assertThat(userResult.score(), is(4));
    }

    @Test
    public void pointsForCorrectWinnerInSemiFinalShouldBe8(){
        Match lsMatch = new Match("a", "b", ZonedDateTime.now(Config.STOCKHOLM), SEMI_FINAL, -1L);
        lsMatch.setId(1L);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 1, 12345L, "a"));
        Result userResult = new Result(lsMatch, 2, 0, 54321L, "a");
        assertThat(userResult.score(), is(8));
    }

    @Test
    public void pointsForCorrectSemiFinalShouldBe11(){
        Match lsMatch = new Match("a", "b", ZonedDateTime.now(Config.STOCKHOLM), SEMI_FINAL, -1L);
        lsMatch.setId(1L);
        lsMatch.setCorrectResult(new Result(lsMatch, 2, 0, 12345L, "a"));
        Result userResult = new Result(lsMatch, 2, 0, 54321L, "a");
        assertThat(userResult.score(), is(11));
    }

    @Test
    public void pointsForCorrectFinalShouldBe16(){
        Match lsMatch = new Match("a", "b", ZonedDateTime.now(Config.STOCKHOLM), FINAL, -1L);
        lsMatch.setId(1L);
        lsMatch.setCorrectResult(new Result(lsMatch, 0, 0, 12345L, "a"));
        Result userResult = new Result(lsMatch, 2, 1, 54321L, "a");
        assertThat(userResult.score(), is(16));
    }

    @Test
    public void pointsForCorrectFinalShouldBe12(){
        Match lsMatch = new Match("a", "b", ZonedDateTime.now(Config.STOCKHOLM), BRONZE, -1L);
        lsMatch.setId(1L);
        lsMatch.setCorrectResult(new Result(lsMatch, 0, 0, 12345L, "a"));
        Result userResult = new Result(lsMatch, 2, 1, 54321L, "a");
        assertThat(userResult.score(), is(12));
    }



}
