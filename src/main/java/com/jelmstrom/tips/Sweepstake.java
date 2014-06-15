package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.EmailNotification;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("UnusedDeclaration")
@RestController
public class Sweepstake {
    private final Logger logger = LogManager.getLogger(Sweepstake.class);
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
        ConfigurationLoader.initialiseData(context);
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

    public Map<String, Integer> fasterLeaderboard(){
        List<Match> matches = getMatches();
        Map<String, Integer> matchScores = matches.stream()
                .flatMap(match -> match.results.stream())
                .collect(toMap(p -> p.userEmail, Result::score, Math::addExact));

        List<TablePrediction> tablePredictions = tableRepository.read();

        Map<String, Integer> tables = tablePredictions
                    .stream()
                .collect(toMap(p -> p.user, p2 -> p2.score(matches), Math::addExact));

        return mergeMaps(matchScores, tables);

    }

    public Map<String, Integer> mergeMaps(Map<String, Integer> matchScores, Map<String, Integer> tables) {
        Collector<Map.Entry, ?, Map<String, Integer>> mapJoinCollector = getEntryMapCollector();


        return Stream.of(tables, matchScores)
                .map(map -> map.entrySet())
                .flatMap(entrySet -> entrySet.stream())
                .collect(mapJoinCollector);
    }

    public Collector<Map.Entry, ?, Map<String, Integer>> getEntryMapCollector() {
        return toMap(
            entry -> entry.getKey().toString(),
            entry -> Integer.parseInt(entry.getValue().toString()),
            Math::addExact
);
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
        List<TableEntry> tableEntries =  group.teams.stream().map(team -> recordForTeam(team, admin.email, results)).sorted().collect(toList());
        return tableEntries;
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
        System.out.println(String.format("Table Entry %s", Arrays.asList(team, points, goalsFor, goalsAgainst)));
        return new TableEntry(team, goalsFor, goalsAgainst, points);
    }


    private int calculatePointsFor(String user) {
        List<Match> matches = matchRepository.read();
        int matchScore = matches.stream().map(
                match -> match.scoreFor(user))
                .filter(Objects::nonNull)
                .reduce(0, (a, b) -> a + b);

        int groupScore = tableRepository.read().stream().filter(prediction -> prediction.user.equals(user)).mapToInt(this::scoreTable).sum();

        return matchScore + groupScore;
    }

    private int scoreTable(TablePrediction tablePrediction) {

        int score = 0;

        List<String> correctOrder = currentStandingsForGroup(tablePrediction.group).stream()
                .map(entry -> entry.team)
                .filter(Objects::nonNull)
                .collect(toList());
        List<String> userPrediction = tablePrediction.tablePrediction;
        System.out.println("User table " + tablePrediction.user  + " " + tablePrediction.group + "  " + userPrediction);
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

    public User getUser(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        User updated = userRepository.store(user);
        new EmailNotification(userRepository.findAdminUser()).sendMail(updated);
        return updated;
    }

    public User findUser(String displayName) {
        return userRepository.findByDisplayName(displayName);
    }

    public User login(String token) {
        return userRepository.findByToken(token);
    }

    public void saveResults(List<Result> resultList, User user) {
        List<Match> matches = getMatches();
        System.out.println(String.format("Storing results %s", resultList));
        resultList.stream().forEach(result -> new Result(
                            findMatch(matches, result.match.id)
                            , result.homeGoals
                            , result.awayGoals
                            , result.userEmail));
        matchRepository.store(matches);

    }

    private Match findMatch(List<Match> matches, String id) {
        System.out.println(String.format("Finding match for %s", id));
        return matches.stream().filter(match -> match.id.equals(id)).findFirst().get();
    }

    public void saveUserPrediction(TablePrediction tablePrediction) {
        tableRepository.store(tablePrediction);
    }
}
