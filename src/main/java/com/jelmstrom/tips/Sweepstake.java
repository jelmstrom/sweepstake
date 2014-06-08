package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
@RestController
public class Sweepstake {

    @RequestMapping("/matches")
    public List<Match> getMatches() {
        return MatchRepository.read();
    }

    @RequestMapping("/users")
    public List<User> getUsers() {
        return UserRepository.read();
    }

    @RequestMapping(value = "/predictions/{user}")
    public List<TablePrediction> getPredictions(@PathVariable String user) {
        return TableRepository.read().stream().filter(prediction -> prediction.user.equals(user)).collect(toList());
    }

    @RequestMapping(value = "/leaderboard")
    public List<Object[]> getLeaderBoard() {
        return getUsers().stream().map(user -> new Object[]{user, Integer.toString(calculatePointsFor(user.displayName))}).collect(toList());
    }

    @RequestMapping(value = "/table/{user}/{groupName}")
    public List<TableEntry> currentStandingsForGroup(@PathVariable String groupName) {
        List<Result> results = resultsFor("Admin");
        Group group = GroupRepository.read(groupName);
        return group.teams.stream().map(team -> recordForTeam(team, "Admin", results)).sorted().collect(toList());
    }




    private List<Result> resultsFor(String user) {
        return com.jelmstrom.tips.match.MatchRepository.read().stream().map(match -> match.resultFor(user)).filter(Objects::nonNull).collect(toList());
    }

    private TableEntry recordForTeam(String team, String user, List<Result> results) {
        int points = results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.pointsFor(team)).sum();
        int goalsFor = results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsFor(team)).sum();
        int goalsAgainst = results.stream().filter(result -> result.user.equals(user))
                .mapToInt(match -> match.goalsAgainst(team)).sum();
        return new TableEntry(team, goalsFor, goalsAgainst, points);
    }


    public int calculatePointsFor(String user) {
        List<Match> matches = MatchRepository.read();
        int matchScore = matches.stream().map(
                match -> match.resultFor(user))
                .filter(Objects::nonNull)
                .map(result -> userScore((Result) result))
                .reduce(0, (a, b) -> a + b);

        int groupScore = TableRepository.read().stream().mapToInt(this::scoreTable).sum();

        return matchScore + groupScore;
    }


    private int userScore(Result userResult) {
        Result adminResult = userResult.match.resultFor("Admin");
        int points = 0;
        if (userResult.winner() == adminResult.winner()) {
            points++;
        }
        if (userResult.homeGoals == adminResult.homeGoals) {
            points++;
        }
        if (userResult.awayGoals == adminResult.awayGoals) {
            points++;
        }

        return points;
    }

    public int scoreTable(TablePrediction tablePrediction) {

        int score = 0;

        List<String> correctOrder = currentStandingsForGroup(tablePrediction.group).stream()
                .map(entry -> entry.team)
                .filter(Objects::nonNull)
                .collect(toList());
        List<String> userPrediction = tablePrediction.tablePrediction;
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
                score += 1;
            }
            if (userPrediction.get(3).equals(correctOrder.get(3))) {
                score += 1;
            }

        }
        return score;
    }

    public User getUser(String email) {
        return UserRepository.read(email);
    }

    public void saveUser(User user) {
        UserRepository.store(user);
    }

    public User findUser(String displayName) {
        return UserRepository.find(displayName);
    }
}
