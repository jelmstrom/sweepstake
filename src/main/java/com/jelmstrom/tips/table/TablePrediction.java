package com.jelmstrom.tips.table;

import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.user.User;

import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TablePrediction {

    public final String group;
    public final List<String> tablePrediction;
    public String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public TablePrediction(String group, String userId, List<String> tablePrediction) {
        this.group = group;
        this.tablePrediction = tablePrediction;
        this.userId = userId;
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
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;

        return true;
    }

    public int score(List<Result> adminResult){
        List<TableEntry> tableEntries =  this.tablePrediction.stream().map(team -> TableEntry.recordForTeam(team, adminResult)).sorted().collect(toList());
        return scoreTable(tableEntries);
    }


    private int scoreTable(List<TableEntry> correctTable) {

        int score = 0;

        List<String> correctOrder = correctTable.stream().map(entry -> entry.team).collect(toList());
        List<String> userPrediction = this.tablePrediction;
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


    @SuppressWarnings("UnusedDeclaration")
    public boolean editable(User user){
        return user.admin || new Date().before(Config.startDate);
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (tablePrediction != null ? tablePrediction.hashCode() : 0);
        return result;
    }
}
