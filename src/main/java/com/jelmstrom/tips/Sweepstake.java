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

    private final String context;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final TableRepository tableRepository;

    Sweepstake(){
        //only used for rest controller..
        // if I can get JQuery hooked in...
        this.context = null;
        matchRepository = null;
        userRepository = null;
        tableRepository = null;
    }

    public Sweepstake(String context) {
        this.context = context;
        matchRepository = new MatchRepository(context);
        userRepository = new UserRepository(context);
        tableRepository = new TableRepository(context);
    }

    @RequestMapping("/matches")
    public List<Match> getMatches() {
        return matchRepository.read();
    }

    @RequestMapping("/users")
    public List<User> getUsers() {
        return userRepository.read();
    }

    @RequestMapping(value = "/predictions/{user}")
    public List<TablePrediction> getPredictions(@PathVariable String user) {
        return tableRepository.read().stream().filter(prediction -> prediction.user.equals(user)).collect(toList());
    }

    @RequestMapping(value = "/leaderboard")
    public List<Object[]> getLeaderBoard() {
        return getUsers().stream().map(user -> new Object[]{user, Integer.toString(calculatePointsFor(user.email))}).collect(toList());
    }

    @RequestMapping(value = "/table/{groupName}")
    public List<TableEntry> currentStandingsForGroup(@PathVariable String groupName) {
        User admin = userRepository.findAdminUser();
        List<Result> results = resultsFor(admin.email);
        Group group = new GroupRepository(context).read(groupName);
        return group.teams.stream().map(team -> recordForTeam(team, admin.email, results)).sorted().collect(toList());
    }




    private List<Result> resultsFor(String userEmail) {
        return matchRepository.read().stream().map(match -> match.resultFor(userEmail)).filter(Objects::nonNull).collect(toList());
    }

    private TableEntry recordForTeam(String team, String userEmail, List<Result> results) {
        int points = results.stream().filter(result -> result.userEmail.equals(userEmail))
                .mapToInt(match -> match.pointsFor(team)).sum();
        int goalsFor = results.stream().filter(result -> result.userEmail.equals(userEmail))
                .mapToInt(match -> match.goalsFor(team)).sum();
        int goalsAgainst = results.stream().filter(result -> result.userEmail.equals(userEmail))
                .mapToInt(match -> match.goalsAgainst(team)).sum();
        return new TableEntry(team, goalsFor, goalsAgainst, points);
    }


    public int calculatePointsFor(String user) {
        List<Match> matches = matchRepository.read();
        int matchScore = matches.stream().map(
                match -> match.scoreFor(user))
                .filter(Objects::nonNull)
                .reduce(0, (a, b) -> a + b);

        int groupScore = tableRepository.read().stream().mapToInt(this::scoreTable).sum();

        return matchScore + groupScore;
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
        return userRepository.read(email);
    }

    public void saveUser(User user) {
        userRepository.store(user);
    }

    public User findUser(String displayName) {
        return userRepository.find(displayName);
    }
}
