package com.jelmstrom.tips;


import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.Result;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResultTest {

    private final String brazil = "Brazil";
    private final String germany = "Germany";
    private final String argentina = "Argentina";



    @Test
    public void homeTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 2,1, "Johan");
        assertThat(result.winner(), is(1));

    }

    @Test
    public void awayTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 0,1, "Johan");
        assertThat(result.winner() , is(-1));
    }

    @Test
    public void equalGoalsMeansDraw(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 1,1, "Johan");
        assertThat(result.winner() , is(0));
    }

    @Test
    public void homeTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 2,1, "Johan");
        assertThat(result.pointsFor(brazil), is(3));
        assertThat(result.pointsFor(germany), is(0));

    }

    @Test
    public void awayTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 1,2, "Johan");
        assertThat(result.pointsFor(germany), is(3));
        assertThat(result.pointsFor(brazil), is(0));
    }

    @Test
    public void bothTeamGetsOnePointsForDraw(){
        Match newMatch = new Match(brazil, germany, null, "A1");

        Result result = new Result(newMatch, 1,1, "Johan");
        assertThat(result.pointsFor(germany), is(1));
        assertThat(result.pointsFor(brazil), is(1));
    }

    @Test
    public void nonTeamGetsZeroPointsForDraw(){
        Match newMatch = new Match(brazil, germany, null, "A1");
        Result result = new Result(newMatch, 2,1, "Johan");
        assertThat(result.pointsFor(argentina), is(0));
    }


}
