package com.jelmstrom.tips;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class Sweepstake {


    public List<TableEntry> calculateTableFor(List<String> group, String user) {
        List<Result> results = matchesFor(user);
        return group.stream().map(team -> recordForTeam(team, user, results)).sorted().collect(toList());
    }

    private List<Result> matchesFor(String user) {
        return MatchRepository.getAll().stream().map(match -> match.resultFor(user)).filter(Objects::nonNull).collect(toList());
    }

    private TableEntry recordForTeam(String team, String user, List<Result> results) {
        int points=  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.pointsFor(team)).sum();
        int goalsFor =  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsFor(team)).sum();
        int goalsAgainst =  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsAgainst(team)).sum();
        TableEntry entry = new TableEntry(team, goalsFor, goalsAgainst, points);
        return entry;
    }


    public int calculatePointsFor(String user, List<Match> matches) {
        return matches.stream().map(
                match -> match.resultFor(user))
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

    public int scoreTable(TablePrediction tablePrediction) {

        int score = 0;
        List<String> groupTeams = Config.INSTANCE.getGroupTeams(tablePrediction.group);
        List<String> correctOrder = calculateTableFor(groupTeams, "Admin").stream().map(entry -> entry.team).collect(toList());
        List<String> userPrediction = tablePrediction.tablePrediction;
        if(userPrediction.equals(correctOrder)){
            score = 7;
        } else {
            List<String> topTwo =  correctOrder.subList(0,2);
            if(userPrediction.get(0).equals(correctOrder.get(0))){
                score +=2;
            } else if(topTwo.contains(userPrediction.get(0))){
                score++;
            }
            if(userPrediction.get(1).equals(correctOrder.get(1))){
                score +=2;
            }else if(topTwo.contains(userPrediction.get(0))){
                score++;
            }

            if(userPrediction.get(2).equals(correctOrder.get(2))){
                score +=1;
            }
            if(userPrediction.get(3).equals(correctOrder.get(3))){
                score +=1;
            }

        }
        return score;
    }
}
