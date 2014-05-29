package com.jelmstrom.tips;


import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.stream.Collectors.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MatchTest {

    private final String brazil = "Brazil";
    private final String germany = "Germany";
    private final String australia = "Australia";
    private final String argentina = "Argentina";
    private final String spain = "Spain";
    private final String england = "England";
    private final String uruguay = "Uruguay";
    private List matches;
    private Match match1;
    private Match match2;
    private Match match3;
    private Match match4;
    private List<String> groupA = Arrays.asList(brazil, germany, argentina, australia);
    private List <String> groupB = Arrays.asList( spain, england, uruguay);

    @Before
    public void setup(){
        matches = new ArrayList();
        matches.add( new Match(brazil, germany, 1,0, null));
        matches.add( new Match(brazil, argentina, 2,0, null));
        matches.add( new Match(brazil, australia, 5,0, null));

        matches.add( new Match(germany, australia, 2,0, null));
        matches.add( new Match(germany, argentina, 1,0, null));

        matches.add( new Match(australia, argentina, 0,3, null));



        matches.add( new Match(spain, england, 0,0, null));
        matches.add( new Match(england, uruguay, 2,0, null));
    }

    @Test
    public void homeTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, 10,0, null);
        assertThat(newMatch.winner(), is(1));

    }

    @Test
    public void awayTeamWinsIfScoringMoreGoals(){
        Match newMatch = new Match(brazil, germany, 0,5, null);
        assertThat(newMatch.winner() , is(-1));
    }

    @Test
    public void equalGoalsMeansDraw(){
        Match newMatch = new Match(brazil, germany, 0,0, null);
        assertThat(newMatch.winner() , is(0));
    }

    @Test
    public void matchHasHomeAndAwayTeam(){
        Match newMatch = new Match(brazil, germany, 0,0, null);
        assertThat(newMatch.getHomeTeam(), is(brazil));
        assertThat(newMatch.getAwayTeam(), is(germany));
    }

    @Test
    public void homeTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, 1,0, null);
        assertThat(newMatch.pointsFor(brazil), is(3));
        assertThat(newMatch.pointsFor(germany), is(0));

    }

    @Test
    public void awayTeamGetsAllThreePointsForWin(){
        Match newMatch = new Match(brazil, germany, 0,3, null);
        assertThat(newMatch.pointsFor(germany), is(3));
        assertThat(newMatch.pointsFor(brazil), is(0));
    }

    @Test
    public void bothTeamGetsOnePointsForDraw(){
        Match newMatch = new Match(brazil, germany, 0,0, null);
        assertThat(newMatch.pointsFor(germany), is(1));
        assertThat(newMatch.pointsFor(brazil), is(1));
    }

    @Test
    public void nonTeamGetsZeroPointsForDraw(){
        Match newMatch = new Match(brazil, germany, 0,0, null);
        assertThat(newMatch.pointsFor("TeamC"), is(0));
    }

    @Test
    public void aggregatingResultsForTeamShouldReturnSixPointsForTwoWins(){

        TableEntry entry = recordForTeam(brazil);
        assertThat(entry.points, is(9));
    }

    @Test
    public void tableForGroupAShouldBeBrazilGermanyArgentinaAustralia(){

        List<TableEntry> table = calculateTableFor(groupA);

        table.stream().forEach(entry -> System.out.println(entry));

        assertThat(table.get(0).team, is(brazil));
        assertThat(table.get(1).team, is(germany));
        assertThat(table.get(2).team, is(argentina));
        assertThat(table.get(3).team, is(australia));

    }

    private List<TableEntry> calculateTableFor(List<String> group) {
        return group.stream().map(team -> recordForTeam(team)).collect(toList());
    }


    private TableEntry recordForTeam(String team) {
        int points=  matches.stream().mapToInt(match -> ((Match) match).pointsFor(team)).sum();
        int goalsFor =  matches.stream().mapToInt(match -> ((Match) match).goalsFor(team)).sum();
        int goalsAgainst =  matches.stream().mapToInt(match -> ((Match) match).goalsAgainst(team)).sum();
        TableEntry entry = new TableEntry(team, goalsFor, goalsAgainst, points);
        return entry;
    }



}
