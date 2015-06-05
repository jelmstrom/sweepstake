package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.NeoMatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.notification.EmailNotification;
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
import java.util.stream.Stream;

import static com.jelmstrom.tips.match.Match.Stage.*;
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

    public List<TablePrediction> getPredictions(Long userId) {
        User user = userRepository.read(userId);
        return tableRepository.read().stream().filter(prediction -> prediction.userId.equals(user.id)).collect(toList());
    }

    public List<LeaderboardEntry> leaderboard(){
        User adminUser = userRepository.findAdminUser();
        List<Match> matches = matchRepository.read();
        Map<Long, Integer> matchScores = matches.stream()
                .flatMap(match -> match.results.stream())
                .collect(toMap(result -> result.userId, Result::score, Math::addExact));

        List<TablePrediction> tablePredictions = tableRepository.read();
        
        Map<Long, Integer> tables = scoreTablePredictions(matches, tablePredictions);

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

    public Map<Long, Integer> scoreTablePredictions(List<Match> matches, List<TablePrediction> tablePredictions) {
        List<Result> adminResults = matches.stream().filter(match -> match.stage == GROUP).map(Match::getCorrectResult).filter(Objects::nonNull).collect(toList());
        return tablePredictions.stream()
                .filter(prediction -> !StringUtils.isEmpty(prediction.userId))
                .collect(toMap(prediction -> prediction.userId, prediction -> prediction.score(adminResults), Math::addExact));
    }

    public Map<Long, Integer> mergeMaps(Map<Long, Integer> map1, Map<Long, Integer> map2) {
        return Stream.of(map2, map1)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(toMap(
                        entry -> (Long) entry.getKey(),
                        entry -> Integer.parseInt(entry.getValue().toString()),
                        Math::addExact
                ));
    }


    public List<TableEntry> currentStandingsForGroup(Long groupId) {
       List<Result> adminResults = matchRepository.read().stream().filter(match -> match.stage == GROUP).map(Match::getCorrectResult).filter(Objects::nonNull).collect(toList());
        Group group = groupRepository.read(groupId);
        System.out.printf("Group %s :{%s}", groupId, group);
        return group.teams.stream().map(team -> TableEntry.recordForTeam(team, adminResults)).sorted().collect(toList());
    }

    private Match findMatch(List<Match> matches, Long id) {
        System.out.println(String.format("Finding match for %s", id));
        return matches.stream().filter(match -> match.getId().equals(id)).findFirst().get();
    }



    public User saveUser(User user) {
        User updated = userRepository.store(user);
        new EmailNotification().sendMail(updated);
        return updated;
    }


    public void saveUserPrediction(TablePrediction tablePrediction) {
        tableRepository.store(tablePrediction);
    }

    public void deleteUser(Long userId) {
        userRepository.remove(userId);
    }

    public List<Match> createLastSixteenStage() {
        Group lastSixteen = groupRepository.store(new Group("16", Collections.<String>emptyList(), Match.Stage.LAST_SIXTEEN));
        Match match1 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));
        Match match2 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));

        Match match3 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));
        Match match4 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));

        Match match5 = matchRepository.store(new Match("", "", new Date(), Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));
        Match match6 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));

        Match match7 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));
        Match match8 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));

        List<Match> quarterFinals=  matchRepository.stageMatches(QUARTER_FINAL);
        if(quarterFinals.isEmpty()){
            quarterFinals = createQuarterFinalStage();
        }
        matchRepository.addRelation(match1, "homeTeam", quarterFinals.get(0));
        matchRepository.addRelation(match2, "awayTeam", quarterFinals.get(0));

        matchRepository.addRelation(match3, "homeTeam", quarterFinals.get(1));
        matchRepository.addRelation(match4, "awayTeam", quarterFinals.get(1));

        matchRepository.addRelation(match5, "homeTeam", quarterFinals.get(2));
        matchRepository.addRelation(match6, "awayTeam", quarterFinals.get(2));

        matchRepository.addRelation(match7, "homeTeam", quarterFinals.get(3));
        matchRepository.addRelation(match8, "awayTeam", quarterFinals.get(3));
        return Arrays.asList(match1, match2, match3, match4, match5, match6, match7, match8);
    }

    public List<Match> createQuarterFinalStage() {
        Group quarterFinals = groupRepository.store(new Group("QF", Collections.<String>emptyList(), Match.Stage.QUARTER_FINAL));
        Match match1 = matchRepository.store(new Match("", "", new Date() , Match.Stage.QUARTER_FINAL, quarterFinals.getGroupId()));
        Match match2 = matchRepository.store(new Match("", "", new Date() , Match.Stage.QUARTER_FINAL, quarterFinals.getGroupId()));

        Match match3 = matchRepository.store(new Match("", "", new Date(), Match.Stage.QUARTER_FINAL, quarterFinals.getGroupId()));
        Match match4 = matchRepository.store(new Match("", "", new Date() , Match.Stage.QUARTER_FINAL, quarterFinals.getGroupId()));

        List<Match> semis =  matchRepository.stageMatches(SEMI_FINAL);
        if(semis.isEmpty()){
            semis = createSemiFinalStage();
        }
        matchRepository.addRelation(match1, "homeTeam", semis.get(0));
        matchRepository.addRelation(match2, "awayTeam", semis.get(0));

        matchRepository.addRelation(match3, "homeTeam", semis.get(1));
        matchRepository.addRelation(match4, "awayTeam", semis.get(1));
        return Arrays.asList(match1, match2, match3, match4);
    }

    public List<Match> createSemiFinalStage() {
        Group semis = groupRepository.store(new Group("Semi", Collections.<String>emptyList(), Match.Stage.SEMI_FINAL));
        Match semi1 = matchRepository.store(new Match("", "", new Date() , Match.Stage.SEMI_FINAL, semis.getGroupId()));
        Match semi2 = matchRepository.store(new Match("", "", new Date() , Match.Stage.SEMI_FINAL, semis.getGroupId()));

        List<Match> finalStage=  matchRepository.stageMatches(FINAL);
        if(finalStage.isEmpty()){
            finalStage =  createFinalStage();
        }
        matchRepository.addRelation(semi1, "homeTeam", finalStage.get(0));
        matchRepository.addRelation(semi2, "awayTeam", finalStage.get(0));
        return Arrays.asList(semi1, semi1);
    }

    public List<Match> createFinalStage() {
        Group finals = groupRepository.store(new Group("Final", Collections.<String>emptyList(), FINAL));
        return Collections.singletonList(matchRepository.store(new Match("", "", new Date(), FINAL, finals.getGroupId())));
    }
}
