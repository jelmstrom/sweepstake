package com.jelmstrom.tips;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
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


    @Before
    public void setup(){
        matches = new ArrayList();
        results = new ArrayList();
        matches.add( new Match(brazil, germany, null, "A1"));
        results.add(new Result(matches.get(0), 2, 0, "Johan"));
        matches.add( new Match(brazil, argentina, null, "A1"));
        results.add(new Result(matches.get(1), 2, 0, "Johan"));
        matches.add( new Match(brazil, australia, null, "A1"));
        results.add(new Result(matches.get(2), 2, 0, "Johan"));

        matches.add( new Match(germany, australia, null, "A1"));
        results.add(new Result(matches.get(3), 2, 0, "Johan"));
        matches.add( new Match(germany, argentina, null, "A1"));
        results.add(new Result(matches.get(4), 0, 0, "Johan"));

        matches.add( new Match(australia, argentina, null, "A1"));
        results.add(new Result(matches.get(5), 1, 5, "Johan"));



        results.add(new Result(matches.get(0), 2, 1, "Admin")); //2, 0 (2p)
        results.add(new Result(matches.get(1), 2, 0, "Admin")); // 2, 0 (3p)
        results.add(new Result(matches.get(2), 0, 0, "Admin")); // 2, 0 (1p)

        results.add(new Result(matches.get(3), 1, 3, "Admin")); // 2, 0 (0p)
        results.add(new Result(matches.get(4), 0, 1, "Admin")); // 0,1 (1p)

        results.add(new Result(matches.get(5), 1, 2, "Admin"));  // 1, 5, (2p)
        // 9 p

    }


    @Test
    public void tableForGroupAShouldBeBrazilGermanyArgentinaAustralia(){

        List<TableEntry> table = calculateTableFor(groupA, "Johan");

        assertThat(table.get(0).team, is(brazil));
        assertThat(table.get(1).team, is(argentina));
        assertThat(table.get(2).team, is(germany));
        assertThat(table.get(3).team, is(australia));

    }

    private List<TableEntry> calculateTableFor(List<String> group, String user) {
        return group.stream().map(team -> recordForTeam(team, user)).sorted().collect(toList());
    }


    private TableEntry recordForTeam(String team, String user) {
        int points=  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.pointsFor(team)).sum();
        int goalsFor =  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsFor(team)).sum();
        int goalsAgainst =  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsAgainst(team)).sum();
        TableEntry entry = new TableEntry(team, goalsFor, goalsAgainst, points);
        return entry;
    }

    @Test
    public void tableForUserWithoutResultsShouldBeOrderedAlphabetically(){

        List<TableEntry> table = calculateTableFor(groupA, "Christian");

        assertThat(table.get(0).team, is(argentina));
        assertThat(table.get(1).team, is(australia));
        assertThat(table.get(2).team, is(brazil));
        assertThat(table.get(3).team, is(germany));

    }

    @Test
    public void pointsForUserShouldBeCalculatedBasedOnAdminResults(){


        int points = calculatePointsFor("Johan", matches);

        assertThat(points, is(9));


    }

    private int calculatePointsFor(String user, List<Match> matches) {
        return matches.stream().map(
                match -> ((Match) match).resultFor(user))
                .map(result -> userScore((Result) result))
                .reduce(0, (a, b) -> a + b);
    }

    private int userScore(Result userResult){
        Result adminResult = userResult.match.resultFor("Admin");
        int points = 0;
        if(userResult.winner()==adminResult.winner()){
            points++;
        }
        if(userResult.homeGoals ==adminResult.homeGoals){
            points++;
        }
        if(userResult.awayGoals==adminResult.awayGoals){
            points++;
        }

        return points;
    }
     
    @Test
    public void scoreForCompletelyCorrectTableShouldBeSeven(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);
        int score = scoreTable(prediction);


        assertThat(score, is(7));
    }

    private int scoreTable(TablePrediction tablePrediction) {

        int score = 0;
        List<String> correct = calculateTableFor(groupA, "Admin").stream().map(entry -> entry.team).collect(toList());
        List<String> userPrediction = tablePrediction.tablePrediction;
        if(userPrediction.equals(correct)){
            score = 7;
        } else {
            List<String> topTwo =  correct.subList(0,2);
            if(userPrediction.get(0).equals(correct.get(0))){
                score +=2;
            } else if(topTwo.contains(userPrediction.get(0))){
                score++;
            }
            if(userPrediction.get(1).equals(correct.get(1))){
                score +=2;
            }else if(topTwo.contains(userPrediction.get(0))){
                score++;
            }

            if(userPrediction.get(2).equals(correct.get(2))){
                score +=1;
            }
            if(userPrediction.get(3).equals(correct.get(3))){
                score +=1;
            }

        }
        return score;
    }

    @Test
    public void scoreForFirstTwoTeamsIsFourIfCompletelyCorrect(){

        List<String> userPrediction = asList(brazil, argentina, germany, australia);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);

        int score = scoreTable(prediction);

        assertThat(score, is(4));

    }
    @Test
    public void scoreForFirstTwoTeamsIsTwoIfOrderedIncorrectlyCorrect(){

        List<String> userPrediction = asList( argentina,brazil, germany, australia);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);

        int score = scoreTable(prediction);

        assertThat(score, is(2));

    }


    @Test
    public void scoreIsZeroIfCompletelyWrong(){

        List<String> userPrediction = asList( germany, australia, argentina,brazil);
        TablePrediction prediction = new TablePrediction("Johan", "GroupA", userPrediction);

        int score = scoreTable(prediction);

        assertThat(score, is(0));

    }



}
