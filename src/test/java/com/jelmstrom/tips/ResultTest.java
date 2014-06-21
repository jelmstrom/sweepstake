package com.jelmstrom.tips;


import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.Result;
import org.junit.Test;

import java.util.Date;

import static com.jelmstrom.tips.match.Match.Stage.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResultTest {

    private final String brazil = "Brazil";
    private final String germany = "Germany";


    @Test
    public void homeTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 2,1, "");
        assertThat(result.winner(), is(1));

    }

    @Test
    public void awayTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 0,1, "");
        assertThat(result.winner() , is(-1));
    }

    @Test
    public void equalGoalsMeansDraw(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 1,1, "");
        assertThat(result.winner() , is(0));
    }

    @Test
    public void homeTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 2,1, "");
        assertThat(result.pointsFor(brazil), is(3));
        assertThat(result.pointsFor(germany), is(0));

    }

    @Test
    public void awayTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 1,2, "");
        assertThat(result.pointsFor(germany), is(3));
        assertThat(result.pointsFor(brazil), is(0));
    }

    @Test
    public void bothTeamGetsOnePointsForDraw(){
        Match newMatch = new Match(brazil, germany, null, "A1");

        Result result = new Result(newMatch, 1,1, "");
        assertThat(result.pointsFor(germany), is(1));
        assertThat(result.pointsFor(brazil), is(1));
    }

    @Test
    public void nonTeamGetsZeroPointsForDraw(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 2,1, "");
        assertThat(result.pointsFor("Argentina"), is(0));
    }




    @Test
    public void pointsForCorrectLast16GameShouldBe7(){
        Match lsMatch = new Match("a", "b", new Date(), "qf1", LAST_SIXTEEN);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 2, "12345", "b"));
        Result userResult = new Result(lsMatch, 1, 2, "54312", "b");
        assertThat(userResult.score(), is(7));
    }

    @Test
    public void pointsForCorrectQuarterFinalGameShouldBe11(){
        Match lsMatch = new Match("a", "b", new Date(), "qf1", QUARTER_FINAL);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 2, "12345","b"));
        Result userResult = new Result(lsMatch, 1, 2, "54312", "b");
        assertThat(userResult.score(), is(11));
    }

    @Test
    public void pointsForCorrectWinnerInQuarterFinalShouldBe8(){
        Match lsMatch = new Match("a", "b", new Date(), "qf1", QUARTER_FINAL);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 1, "12345","a"));
        Result userResult = new Result(lsMatch, 2, 0, "54312", "a");
        assertThat(userResult.score(), is(8));
    }

    @Test
    public void pointsForCorrectWinnerInSemiFinalShouldBe16(){
        Match lsMatch = new Match("a", "b", new Date(), "qf1", SEMI_FINAL);
        lsMatch.setCorrectResult(new Result(lsMatch, 1, 1, "12345","a"));
        Result userResult = new Result(lsMatch, 2, 0, "54312", "a");
        assertThat(userResult.score(), is(16));
    }

    @Test
    public void pointsForCorrectSemiFinalShouldBe19(){
        Match lsMatch = new Match("a", "b", new Date(), "qf1", SEMI_FINAL);
        lsMatch.setCorrectResult(new Result(lsMatch, 2, 0, "12345","a"));
        Result userResult = new Result(lsMatch, 2, 0, "54312", "a");
        assertThat(userResult.score(), is(19));
    }

    @Test
    public void pointsForCorrectFinalShouldBe32(){
        Match lsMatch = new Match("a", "b", new Date(), "qf1", FINAL);
        lsMatch.setCorrectResult(new Result(lsMatch, 0, 0, "12345","a"));
        Result userResult = new Result(lsMatch, 2, 1, "54312", "a");
        assertThat(userResult.score(), is(32));
    }



}
