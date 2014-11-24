package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.NeoMatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.*;
import com.jelmstrom.tips.table.NeoTablePredictionRepository;
import com.jelmstrom.tips.table.TablePredictionRepository;
import com.jelmstrom.tips.user.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
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
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final TablePredictionRepository tableRepository;
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
        matchRepository = new NeoMatchRepository(context);
        userRepository = new NeoUserRepository(context);
        tableRepository = new NeoTablePredictionRepository(context);
        groupRepository = new NeoGroupRepository(context);
    }

    public List<Match> getMatches() {
        return matchRepository.read();
    }

    public List<User> getUsers() {
        return userRepository.read();
    }

    public List<TablePrediction> getPredictions(Long userId) {
        User user = userRepository.read(userId);
        return tableRepository.read().stream().filter(prediction -> prediction.userId.equals(user.id)).collect(toList());
    }

    public List<LeaderboardEntry> fasterLeaderboard(){
        User adminUser = userRepository.findAdminUser();
        List<Match> matches = getMatches();
        Map<Long, Integer> matchScores = matches.stream()
                .flatMap(match -> match.results.stream())
                .collect(toMap(result -> result.userId, Result::score, Math::addExact));

        List<TablePrediction> tablePredictions = tableRepository.read();
        List<Result> adminResults = matches.stream().filter(match -> match.stage == GROUP).map(Match::getCorrectResult).filter(Objects::nonNull).collect(toList());

        Map<Long, Integer> tables = tablePredictions.stream()
                .filter(prediction -> !StringUtils.isEmpty(prediction.userId))
                .collect(toMap(pred -> pred.userId, pred -> pred.score(adminResults), Math::addExact));

        List<User> users = userRepository.read();
        Map<Long, Integer> userPoint
                =  users.stream().collect(toMap(user -> user.id, user ->  user.score(adminUser), Math::addExact));


        Map<Long, Integer> user =  mergeMaps(tables, userPoint);
        Map<Long, Integer> leaderboardmap =  mergeMaps(matchScores, user);

        return leaderboardmap.entrySet().stream()
                .map(entry -> new LeaderboardEntry(users.stream().filter(u -> u.id.equals(entry.getKey())).findFirst().orElse(new User("","", false, "")), entry.getValue()))
                .filter(entry -> !StringUtils.isEmpty(entry.user.id))
                .sorted()
                .collect(toList());

    }

    public Map<Long, Integer> mergeMaps(Map<Long, Integer> matchScores, Map<Long, Integer> tables) {
        Collector<Map.Entry, ?, Map<Long, Integer>> mapJoinCollector = getEntryMapCollector();


        return Stream.of(tables, matchScores)
                .map(map -> map.entrySet())
                .flatMap(entrySet -> entrySet.stream())
                .collect(mapJoinCollector);
    }

    public Collector<Map.Entry, ?, Map<Long, Integer>> getEntryMapCollector() {
        return toMap(
            entry -> (Long)entry.getKey(),
            entry -> Integer.parseInt(entry.getValue().toString()),
            Math::addExact
        );
    }


    public List<TableEntry> currentStandingsForGroup(Long groupId) {

        List<Result> adminResults = getMatches().stream().filter(match -> match.stage == GROUP).map(Match::getCorrectResult).filter(Objects::nonNull).collect(toList());
        Group group = groupRepository.read(groupId);
        System.out.printf("Group %s :{%s}", groupId, group);
        return group.teams.stream().map(team -> TableEntry.recordForTeam(team, adminResults)).sorted().collect(toList());
    }

    public User getUser(Long userId) {
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

    public void saveMatches(List<Match> matches, User user) {
        matchRepository.store(matches);

    }

    private Match findMatch(List<Match> matches, Long id) {
        System.out.println(String.format("Finding match for %s", id));
        return matches.stream().filter(match -> match.getMatchId().equals(id)).findFirst().get();
    }

    public void saveUserPrediction(TablePrediction tablePrediction) {
        tableRepository.store(tablePrediction);
    }

    public void deleteUser(Long userId) {
        userRepository.remove(userId);
    }

    public Match getMatch(Long matchId) {
        return matchRepository.read(matchId);
    }

    public List<String> getAllTeams() {
        return groupRepository.allGroups().stream().flatMap(group -> group.teams.stream()).sorted().collect(toList());
    }

    public List<Group> groups() {
        return groupRepository.allGroups();
    }
}
