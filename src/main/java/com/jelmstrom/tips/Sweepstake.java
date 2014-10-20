package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MongoMatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.EmailNotification;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.jelmstrom.tips.match.Match.Stage.GROUP;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("UnusedDeclaration")
@RestController
@ConfigurationProperties("sweepstake")
public class Sweepstake {
    private final Logger logger = LogManager.getLogger(Sweepstake.class);
    private final String context;
    private final MongoMatchRepository matchRepository;
    private final UserRepository userRepository;
    private final TableRepository tableRepository;
    private final GroupRepository groupRepository;

    Sweepstake(){
        //only used for rest controller..
        // if I can get JQuery hooked in...
        this.context = null;
        matchRepository = null;
        userRepository = null;
        tableRepository = null;
        groupRepository = null;
    }

    public Sweepstake(String context) {
        this.context = context;
        matchRepository = new MongoMatchRepository(context);
        userRepository = new UserRepository(context);
        tableRepository = new TableRepository(context);
        groupRepository = new NeoGroupRepository(context);
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
    public List<TablePrediction> getPredictions(@PathVariable String userId) {
        User user = userRepository.read(userId);
        return tableRepository.read().stream().filter(prediction -> prediction.userId.equals(user.id)).collect(toList());
    }

    public List<LeaderboardEntry> fasterLeaderboard(){
        User adminUser = userRepository.findAdminUser();
        List<Match> matches = getMatches();
        Map<String, Integer> matchScores = matches.stream()
                .flatMap(match -> match.results.stream())
                .collect(toMap(result -> result.userId, Result::score, Math::addExact));

        List<TablePrediction> tablePredictions = tableRepository.read();
        List<Result> adminResults = matches.stream().filter(match -> match.stage == GROUP).map(Match::getCorrectResult).filter(Objects::nonNull).collect(toList());

        Map<String, Integer> tables = tablePredictions.stream()
                .collect(toMap(pred -> pred.userId, pred -> pred.score(adminResults), Math::addExact));

        List<User> users = userRepository.read();
        Map<String, Integer> userPoint
                =  users.stream().collect(toMap(user -> user.id, user ->  user.score(adminUser), Math::addExact));


        Map<String, Integer> user =  mergeMaps(tables, userPoint);
        Map<String, Integer> leaderboardmap =  mergeMaps(matchScores, user);

        return leaderboardmap.entrySet().stream()
                .map(entry -> new LeaderboardEntry(userRepository.read(entry.getKey()), entry.getValue()))
                .filter(entry -> !StringUtils.isEmpty(entry.user.id))
                .sorted()
                .collect(toList());

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



    @RequestMapping(value = "/table/{groupName}")
    public List<TableEntry> currentStandingsForGroup(@PathVariable String groupName) {

        List<Result> adminResults = getMatches().stream().filter(match -> match.stage == GROUP).map(Match::getCorrectResult).filter(Objects::nonNull).collect(toList());
        Group group = groupRepository.read(groupName);
        System.out.printf("Group %s :{%s}", groupName, group);
        return group.teams.stream().map(team -> TableEntry.recordForTeam(team, adminResults)).sorted().collect(toList());
    }

    public User getUser(String userId) {
        return userRepository.read(userId);
    }

    public User saveUser(User user) {
        User updated = userRepository.store(user);
        new EmailNotification().sendMail(updated);
        return updated;
    }

    public User findUser(String displayName) {
        return userRepository.findByDisplayName(displayName);
    }

    public User login(String token) {
        return userRepository.findByToken(token);
    }

    public void saveResults(List<Match> resultList, User user) {
        matchRepository.store(resultList);

    }

    private Match findMatch(List<Match> matches, String id) {
        System.out.println(String.format("Finding match for %s", id));
        return matches.stream().filter(match -> match.id.equals(id)).findFirst().get();
    }

    public void saveUserPrediction(TablePrediction tablePrediction) {
        tableRepository.store(tablePrediction);
    }

    public void deleteUser(String userId) {
        userRepository.remove(userId);
    }

    public Match getMatch(String matchId) {
        return matchRepository.read(matchId);
    }

    public List<String> getAllTeams() {
        return groupRepository.allGroups().stream().flatMap(group -> group.teams.stream()).sorted().collect(toList());
    }
}
