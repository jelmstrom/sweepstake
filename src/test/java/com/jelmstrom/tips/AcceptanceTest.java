package com.jelmstrom.tips;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AcceptanceTest {

    private final String brazil = "Brazil";
    private final String germany = "Germany";
    private final String australia = "Australia";
    private final String argentina = "Argentina";
    private List<Match> matches;
    private List<String> groupA = asList(brazil, germany, argentina, australia);
    private List<Result> results;

    private Sweepstake sweepstake = new Sweepstake();

    @Before
    public void setup(){
        matches = new ArrayList<>();
        results = new ArrayList<>();
        Date matchStart = new Date();
        matches.add( new Match(brazil, germany, matchStart, "A1"));
        matches.add( new Match(brazil, argentina, matchStart, "A2"));
        matches.add( new Match(brazil, australia, matchStart, "A3"));
        matches.add( new Match(germany, australia, matchStart, "A4"));
        matches.add( new Match(germany, argentina, matchStart, "A5"));
        matches.add( new Match(australia, argentina, matchStart, "A6"));

        results.add(new Result(matches.get(0), 2, 0, "Johan"));
        results.add(new Result(matches.get(1), 2, 0, "Johan"));
        results.add(new Result(matches.get(2), 2, 0, "Johan"));

        results.add(new Result(matches.get(3), 2, 0, "Johan"));
        results.add(new Result(matches.get(4), 0, 0, "Johan"));

        results.add(new Result(matches.get(5), 1, 5, "Johan"));



        results.add(new Result(matches.get(0), 2, 1, "Admin"));
        results.add(new Result(matches.get(1), 2, 0, "Admin"));
        results.add(new Result(matches.get(2), 0, 0, "Admin"));

        results.add(new Result(matches.get(3), 1, 3, "Admin"));
        results.add(new Result(matches.get(4), 0, 1, "Admin"));

        results.add(new Result(matches.get(5), 1, 2, "Admin"));

        MatchRepository.store(matches);
    }

    @After
    public void tearDown(){
        MatchRepository.remove(matches);
        MatchRepository.tablePredictionCollection.drop();
    }


    @Test
    public void tableForGroupAShouldBeBrazilGermanyArgentinaAustralia(){

        List<TableEntry> table = sweepstake.calculateTableFor(groupA, "Johan");

        assertThat(table.get(0).team, is(brazil));
        assertThat(table.get(1).team, is(argentina));
        assertThat(table.get(2).team, is(germany));
        assertThat(table.get(3).team, is(australia));

    }


    @Test
    public void tableForUserWithoutResultsShouldBeOrderedAlphabetically(){

        List<TableEntry> table = sweepstake.calculateTableFor(groupA, "Christian");

        assertThat(table.get(0).team, is(argentina));
        assertThat(table.get(1).team, is(australia));
        assertThat(table.get(2).team, is(brazil));
        assertThat(table.get(3).team, is(germany));

    }

    @Test
    public void pointsForUserShouldBeCalculatedBasedOnAdminResults(){
        int points = sweepstake.calculatePointsFor("Johan", matches);
        assertThat(points, is(9));
    }

    @Test
    public void groupScoreForCompletelyCorrectTableShouldBeSeven(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);
        int score = sweepstake.scoreTable(prediction);
        assertThat(score, is(7));
    }

    @Test
    public void groupScoreForFirstTwoTeamsIsFourIfCompletelyCorrect(){

        List<String> userPrediction = asList(brazil, argentina, germany, australia);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);

        int score = sweepstake.scoreTable(prediction);

        assertThat(score, is(4));

    }

    @Test
    public void groupScoreForFirstTwoTeamsIsTwoIfOrderedIncorrectlyCorrect(){

        List<String> userPrediction = asList( argentina,brazil, germany, australia);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);

        int score = sweepstake.scoreTable(prediction);

        assertThat(score, is(2));

    }

    @Test
    public void groupScoreIsZeroIfCompletelyWrong(){

        List<String> userPrediction = asList( germany, australia, argentina,brazil);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);

        int score = sweepstake.scoreTable(prediction);

        assertThat(score, is(0));

    }
}
