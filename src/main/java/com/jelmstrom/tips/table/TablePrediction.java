package com.jelmstrom.tips.table;

import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.Result;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class TablePrediction {

    public final String user;
    public final String group;

    public final List<String> tablePrediction;

    public TablePrediction(String user, String group, List<String> tablePrediction) {
        this.user = user;
        this.group = group;
        this.tablePrediction = tablePrediction;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TablePrediction)) return false;

        TablePrediction that = (TablePrediction) o;

        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        if (tablePrediction != null ? !tablePrediction.equals(that.tablePrediction) : that.tablePrediction != null)
            return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    public int score(List<Match> matches){
        List <Result> adminResult = matches.stream().map(match -> match.resultFor("none@noreply.zzz")).filter(Objects::nonNull).collect(toList());
        List<TableEntry> tableEntries =  this.tablePrediction.stream().map(team -> recordForTeam(team, adminResult)).sorted().collect(toList());
        return scoreTable(tableEntries);
    }


    private int scoreTable(List<TableEntry> table) {

        int score = 0;

        List<String> correctOrder = table.stream().map(entry -> entry.team).collect(toList());
        List<String> userPrediction = this.tablePrediction;
        System.out.println("User table " + this.user  + " " + this.group + "  " + userPrediction);
        System.out.println("correct table " + correctOrder);
        if (userPrediction.equals(correctOrder)) {
            score = 7;
        } else if (correctOrder.isEmpty() || userPrediction.isEmpty()) {
            return 0;
        } else {
            List<String> topTwo = correctOrder.subList(0, 2);
            if (userPrediction.get(0).equals(correctOrder.get(0))) {
                score += 2;
            } else if (topTwo.contains(userPrediction.get(0))) {
                score++;
            }
            if (userPrediction.get(1).equals(correctOrder.get(1))) {
                score += 2;
            } else if (topTwo.contains(userPrediction.get(0))) {
                score++;
            }

            if (userPrediction.get(2).equals(correctOrder.get(2))) {
                score ++;
            }
            if (userPrediction.get(3).equals(correctOrder.get(3))) {
                score ++;
            }

        }
        return score;
    }


    private TableEntry recordForTeam(String team, List<Result> results) {
        int points = results.stream().mapToInt(match -> match.pointsFor(team)).sum();
        int goalsFor = results.stream().mapToInt(match -> match.goalsFor(team)).sum();
        int goalsAgainst = results.stream().mapToInt(match -> match.goalsAgainst(team)).sum();

        return new TableEntry(team, goalsFor, goalsAgainst, points);
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (tablePrediction != null ? tablePrediction.hashCode() : 0);
        return result;
    }
}
