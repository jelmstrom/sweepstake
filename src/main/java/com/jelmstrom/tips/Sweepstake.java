package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class Sweepstake {

    public List<TableEntry> calculateTableFor(Group group, String user) {
        List<Result> results = resultsFor(user);
        return group.teams.stream().map(team -> recordForTeam(team, user, results)).sorted().collect(toList());
    }

    private List<Result> resultsFor(String user) {
        return com.jelmstrom.tips.match.MatchRepository.getAll().stream().map(match -> match.resultFor(user)).filter(Objects::nonNull).collect(toList());
    }

    private TableEntry recordForTeam(String team, String user, List<Result> results) {
        int points=  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.pointsFor(team)).sum();
        int goalsFor =  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsFor(team)).sum();
        int goalsAgainst =  results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsAgainst(team)).sum();
        return new TableEntry(team, goalsFor, goalsAgainst, points);
    }


    public int calculatePointsFor(String user) {
        List<Match> matches = MatchRepository.getAll();
        int matchScore =  matches.stream().map(
                match -> match.resultFor(user))
                .map(result -> userScore((Result) result))
                .reduce(0, (a, b) -> a + b);

        int groupScore = TableRepository.read().stream().mapToInt(this::scoreTable).sum();

        return matchScore+groupScore;
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
        Group group = GroupRepository.read(tablePrediction.group);
        List<String> correctOrder = calculateTableFor(group, "Admin").stream().map(entry -> entry.team).collect(toList());
        List<String> userPrediction = tablePrediction.tablePrediction;
        if(userPrediction.equals(correctOrder)){
            score = 7;
        } else if(correctOrder.isEmpty() || userPrediction.isEmpty()){
            return 0;
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
