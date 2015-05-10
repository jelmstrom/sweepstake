package com.jelmstrom.tips;


import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.NeoMatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.twitter.Tweeter;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static com.jelmstrom.tips.match.Match.Stage.*;
import static com.jelmstrom.tips.match.Match.Stage.FINAL;
import static com.jelmstrom.tips.match.Match.Stage.SEMI_FINAL;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
@Controller
@RequestMapping(value = "/")
@EnableScheduling
public class SweepstakeController {

    public static final String SESSION_USER = "activeUser";
    public static final String USER = "user";
    protected final Sweepstake sweepstake;
    protected UserRepository userRepository = new NeoUserRepository(Config.context);
    protected MatchRepository matchRepository = new NeoMatchRepository(Config.context);
    protected GroupRepository groupRepository = new NeoGroupRepository(Config.context);

    public SweepstakeController() {
        sweepstake = new Sweepstake(Config.context);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model uiModel, HttpServletRequest request) {
        uiModel.addAttribute("userList", userRepository.read());
        uiModel.addAttribute("leaderBoard", sweepstake.leaderboard());
        uiModel.addAttribute("groups", groupRepository.allGroups());
        setSessionUsers(request, uiModel);
        return "index";
    }


    @RequestMapping(value = "/prediction/{groupId}", method = RequestMethod.POST)
    public String storePrediction(Model uiModel, @PathVariable String groupId, HttpServletRequest request) {
        String pos1 = request.getParameter("prediction1");
        String pos2 = request.getParameter("prediction2");
        String pos3 = request.getParameter("prediction3");
        String pos4 = request.getParameter("prediction4");

        User user = userRepository.read(sessionUserId(request));
        sweepstake.saveUserPrediction(new TablePrediction(Long.parseLong(groupId), user.id, Arrays.asList(pos1, pos2, pos3, pos4)));

        return showGroup(uiModel, groupId, request);
    }


    @RequestMapping(value = "/group/{groupId}/add", method = RequestMethod.POST)
    public String addTeamToGroup(Model uiModel, @PathVariable String groupId, HttpServletRequest request){
        Group group = groupRepository.read(Long.parseLong(groupId));
        group.teams.add(request.getParameter("newTeam"));
        groupRepository.store(group);
        return showGroup(uiModel, groupId, request);
    }

    @RequestMapping(value = "/group/new", method = RequestMethod.GET)
    public String createGroup(Model uiModel, HttpServletRequest request){
        Group group = new Group("new", Collections.emptyList());
        group = groupRepository.store(group);
        return getGroupInternal(uiModel, request, group.getGroupId());
    }

    @RequestMapping(value = "/group/{groupId}/match", method = RequestMethod.POST)
    public String addMatchToGroup(Model uiModel, @PathVariable String groupId, HttpServletRequest request){

        long group = Long.parseLong(groupId);
        String dateString = request.getParameter("matchDate");
        try {
            Date startTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(dateString);
            Match newMatch = new Match(request.getParameter("homeTeam")
                    , request.getParameter("awayTeam")
                    , startTime
                    , group);
            matchRepository.store(newMatch);
        } catch (ParseException e) {
            uiModel.addAttribute("dateFormatError", dateString + " invalid. Use yyyy-MM-ddTHH:mm or use Chrome");
        }

        return getGroupInternal(uiModel, request, group);
    }

    // GET because form inside form creates problems
    @RequestMapping(value = "/match/{matchId}/delete", method = RequestMethod.GET)
    public String deleteMatch(Model uiModel,  @PathVariable String matchId , HttpServletRequest request){
        System.out.printf(" Delete match %s \n", matchId );
        Long match = Long.parseLong(matchId);
        Long groupId = matchRepository.read(match).groupId;
        matchRepository.drop(match);
        return showGroup(uiModel, groupId.toString(), request);
    }

    @RequestMapping(value = "/group/{groupId}/drop/{team}", method = RequestMethod.POST)
    public String dropTeamFromGroup(Model uiModel, @PathVariable String groupId, @PathVariable String team , HttpServletRequest request){
        Group group = groupRepository.read(Long.parseLong(groupId));
        group.teams.remove(team);
        groupRepository.store(group);
        return showGroup(uiModel, groupId, request);
    }

    @RequestMapping(value = "/group/{groupId}/name", method = RequestMethod.POST)
    public String updateGroupName(Model uiModel, @PathVariable String groupId, HttpServletRequest request){
        Group group = groupRepository.read(Long.parseLong(groupId));
        Group updated = new Group(request.getParameter("groupName"), group.teams);
        updated.setGroupId(group.getGroupId());
        groupRepository.store(updated);
        return showGroup(uiModel, groupId, request);
    }

    @RequestMapping(value = "/results/{groupLetter}", method = RequestMethod.POST)
    public String storeGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        User user = userRepository.read(sessionUserId(request));

        List<Match> resultList = getResults(request, user);

        matchRepository.store(resultList);

        return showGroup(uiModel, groupLetter, request);

    }

    protected List<Match> getResults(HttpServletRequest request, User user) {
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String, Match> matchUpdates = new HashMap<>();

        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String value = request.getParameter(parameterName);
            int endIndex = parameterName.indexOf("_");

            if(endIndex == -1){
                continue;
            }

            String matchId = parameterName.substring(0, endIndex);

            if(matchUpdates.containsKey(matchId)){
                continue;
            }


            Match stored = matchRepository.read(Long.parseLong(matchId));
            Match updatedMatch = updateMatchTeams(request, matchId, stored);
            addResultToMatch(request, user, updatedMatch);
            matchUpdates.put(matchId, updatedMatch);
        }

        return matchUpdates.entrySet().stream().map(Map.Entry::getValue).collect(toList());
    }

    private Match updateMatchTeams(HttpServletRequest request, String matchId, Match stored) {
        Match updatedMatch;
        String homeTeam = request.getParameter(matchId + "_homeTeam");
        String awayTeam = request.getParameter(matchId + "_awayTeam");
        if((StringUtils.isEmptyOrWhitespace(homeTeam)|| StringUtils.isEmpty(homeTeam))
            || (homeTeam.equals(stored.homeTeam) && awayTeam.equals(stored.awayTeam))
           ) {
            //no change keep stored.
            updatedMatch = stored;

        } else {
            //preserve details that are not possible to change
            updatedMatch = new Match(homeTeam, awayTeam, stored.matchStart, stored.stage, stored.groupId);
            updatedMatch.setId(Long.parseLong(matchId));
            updatedMatch.results.addAll(stored.results);
        }
        return updatedMatch;
    }

    public void addResultToMatch(HttpServletRequest request, User user, Match match) {
        Result existingResult = match.resultFor(user.id);

        Integer homeGoals = getHomeGoals(request, match, existingResult);
        Integer awayGoals = getAwayGoals(request, match, existingResult);

        String winner = getWinner(request, match, existingResult);
        if(     null != homeGoals
                && null != awayGoals
                && Integer.compare(homeGoals, awayGoals) != 0) {
            winner = Integer.compare(homeGoals, awayGoals) > 0?match.homeTeam:match.awayTeam;
        }

        Result result = new Result(match,
                homeGoals,
                awayGoals,
                user.id,
                winner);


        if(user.admin){
            match.setCorrectResult(result);
        }

    }

    public String getWinner(HttpServletRequest request, Match match, Result previousResult) {
        String winner = request.getParameter(match.getId()+ "_promoted");

        if(winner == null){
            winner = previousResult.promoted;
        }
        return winner;
    }

    public Integer getAwayGoals(HttpServletRequest request, Match match, Result previousResult) {
        String awayGoals = request.getParameter(match.getId() + "_a");
        Integer away_Goals = StringUtils.isEmptyOrWhitespace(awayGoals) ? null : Integer.parseInt(awayGoals);
        if(away_Goals == null ){
            away_Goals = previousResult.awayGoals;
        }
        return away_Goals;
    }

    public Integer getHomeGoals(HttpServletRequest request, Match match, Result previousResult) {
        String homeGoals = request.getParameter(match.getId() + "_h");
        Integer home_Goals = StringUtils.isEmptyOrWhitespace(homeGoals) ? null : Integer.parseInt(homeGoals);
        if(home_Goals == null ){
            home_Goals = previousResult.homeGoals;
        }
        return home_Goals;
    }

    public boolean hasResults(String homeGoals, String awayGoals) {
        return !(StringUtils.isEmptyOrWhitespace(homeGoals)
                && StringUtils.isEmptyOrWhitespace(awayGoals));
    }

    private Result buildResult_(User user, Map<String, int[]> results, String key, Long groupId) {
        int[] resultPair = results.get(key);
        return new Result(new Match("", "", null, groupId)
                , resultPair[0]
                , resultPair[1]
                , user.id);
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Model uiModel, HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_USER, null);
        return index(uiModel, request);
    }

    @RequestMapping(value = "/group/{groupId}", method = RequestMethod.GET)
    public String showGroup(Model uiModel, @PathVariable String groupId, HttpServletRequest request) {
        long groupID = Long.parseLong(groupId);
        return getGroupInternal(uiModel, request, groupID);
    }

    public String getGroupInternal(Model uiModel, HttpServletRequest request, long groupID) {
        List<TableEntry> tableEntries = sweepstake.currentStandingsForGroup(groupID);
        List<TablePrediction> predictions = sweepstake.getPredictions(sessionUserId(request));
        Optional<TablePrediction> maybe = predictions.stream().filter(entry -> entry.group.equals(groupID)).findFirst();
        List<Match> groupMatches = matchRepository.groupMatches(groupID);
        Collections.sort(groupMatches);
        setSessionUsers(request, uiModel);
        uiModel.addAttribute("matches", groupMatches);
        uiModel.addAttribute("groups", groupRepository.allGroups());
        uiModel.addAttribute("group",groupRepository.read(groupID));
        uiModel.addAttribute("currentStandings", tableEntries);
        uiModel.addAttribute("teams", tableEntries.stream().map(entry -> entry.team).collect(toList()));
        uiModel.addAttribute("prediction", maybe.orElse(TablePrediction.emptyPrediction()));
        return "group";
    }


    @RequestMapping(value = "/user/{displayName}", method = RequestMethod.GET)
    public String getUser(Model uiModel, @PathVariable String displayName, HttpServletRequest request) {

        setSessionUsers(request, userRepository.findByDisplayName(displayName), uiModel);
        List<String> teams = groupRepository.allGroups().stream().flatMap(group -> group.teams.stream()).sorted().collect(toList());
        uiModel.addAttribute("teams", teams);
        uiModel.addAttribute("groups", groupRepository.allGroups());
        return "user";
    }

    private Long sessionUserId(HttpServletRequest request) {
        return (Long) request.getSession().getAttribute(SESSION_USER);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String getUser(Model uiModel, HttpServletRequest request) {
        setSessionUsers(request, uiModel);
        List<String> teams = groupRepository.allGroups().stream().flatMap(group -> group.teams.stream()).sorted().collect(toList());
        uiModel.addAttribute("teams", teams);
        uiModel.addAttribute("groups", groupRepository.allGroups());
        return "user";
    }

    @RequestMapping(value = "/authenticate/{token}", method = RequestMethod.GET)
    public String login(Model uiModel, @PathVariable String token, HttpServletRequest request) {
        System.out.println("login user by token");
        User user = userRepository.findByToken(token);
        if(user.isValid()){
            request.getSession().setAttribute(SESSION_USER, user.id);
            return getUser(uiModel, request);
        } else {
            return index(uiModel, request);
        }
    }

    @RequestMapping(value = "/drop/{userId}", method = RequestMethod.POST)
    public String deleteUser(Model uiModel, @PathVariable String userId, HttpServletRequest request) {
        sweepstake.deleteUser(Long.parseLong(userId));
        return index(uiModel, request);
    }


    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String storeUser(Model uiModel, HttpServletRequest request) {

        System.out.println(String.format("%s user", request.getParameter("action")));

        User user;
        if ("update".equals(request.getParameter("action"))) {
            User sessionUser = sessionUser(request);
            user = saveUserDetails(request, sweepstake);
            if(user.id.equals(sessionUser.id)){
                request.getSession().setAttribute(SESSION_USER, user.id);
            }
        } else {
            System.out.println("new");
            user = createUser(request, sweepstake);
            if (null == sessionUserId(request)) {
                System.out.println("Logging in new user");
                request.getSession().setAttribute(SESSION_USER, user.id);
            }
        }
        return getUser(uiModel, request);
    }

    private User saveUserDetails(HttpServletRequest request, Sweepstake sweepstake) {
        System.out.println(String.format("updating user %s, DisplayName: %s email : %s "
                , request.getParameter("userId")
                , request.getParameter("displayName")
                , request.getParameter("email")));

        User user = new User(Long.parseLong(request.getParameter("userId"))
                , request.getParameter("displayName")
                , request.getParameter("email")
                , null!=request.getParameter("admin")
                , UUID.randomUUID().toString());
        user.setTopScorer(request.getParameter("topScorer"));
        user.setWinner(request.getParameter("winner"));
        
        user = sweepstake.saveUser(user);
        System.out.println(String.format("user %s updated %s", user.displayName, (user.admin ? " (admin)" : "")));
        return user;
    }

    private User createUser(HttpServletRequest request, Sweepstake sweepstake) {
        Stream.of(request.getParameterNames()).forEach(System.out::println);
        User user = new User(request.getParameter("displayName")
                , request.getParameter("email")
                , false
                , UUID.randomUUID().toString());

        System.out.println(String.format("creating user %s ", user.email));
        return sweepstake.saveUser(user);
    }

    private boolean isNewUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("new"));
    }

    protected boolean isUpdateUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("action"));
    }


    protected User setSessionUsers(HttpServletRequest request, Model uiModel) {
        User user;
        String userId = request.getParameter("userId");
        if(StringUtils.isEmpty(userId)){
            user = User.emptyUser();
        } else {
            user = userRepository.read(Long.parseLong(userId));
        }
        return setSessionUsers(request, user , uiModel);
    }

    private User setSessionUsers(HttpServletRequest request, User user, Model model) {
        User sessionUser = sessionUser(request);
        if (!user.isValid()) {
            user = sessionUser;
        }
        boolean editable = (user.isValid() && user.equals(sessionUser)) || sessionUser.admin;
        model.addAttribute("canEdit", editable);
        model.addAttribute(USER, user);
        model.addAttribute(SESSION_USER, sessionUser);
        return sessionUser;
    }


    private User sessionUser(HttpServletRequest request) {
        Long currentSessionUser = sessionUserId(request);
        System.out.printf("Session user : %s \n", currentSessionUser);
        return userRepository.read(currentSessionUser);
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public String showConfig(Model uiModel, HttpServletRequest request) {
        setSessionUsers(request, uiModel);
        ArrayList <Match.Stage>stages = new ArrayList<>();
        for (Match.Stage stage : Match.Stage.values()) {
            if(matchRepository.stageMatches(stage).isEmpty()){
                stages.add(stage);
            }
        }
        uiModel.addAttribute("stages", stages);
        return "config";
    }

    @RequestMapping(value = "/config/twitter", method = RequestMethod.POST)
    public String configureTwitter(Model uiModel, HttpServletRequest request) {
        System.out.println(request.getParameter("enabled"));
        Tweeter.configure(request.getParameter("tokenSecret")
                , request.getParameter("token")
                , request.getParameter("secret")
                , request.getParameter("key")
                , Boolean.valueOf(request.getParameter("enabled")));
        return showConfig(uiModel, request);
    }

    @RequestMapping(value = "/playoff",  method = RequestMethod.GET)
    public String playoff(Model uiModel, HttpServletRequest request) {
        User sessionUser = setSessionUsers(request, uiModel);

        SortedMap<Match.Stage, List<Match>> playoffMap = matchRepository.getPlayoffMatches();

        uiModel.addAttribute("stages",playoffMap);
        uiModel.addAttribute("users", userRepository.read());
        uiModel.addAttribute("groups", groupRepository.allGroups());
        List<String> teams = groupRepository.allGroups().stream().flatMap(group -> group.teams.stream()).sorted().collect(toList());
        uiModel.addAttribute("teams", teams);
        uiModel.addAttribute("playoffTreeEditable", (sessionUser.admin || new Date().before(Config.playoffStart)));

        return "playoff";
    }

    @RequestMapping(value = "/playoff",  method = RequestMethod.POST)
    public String savePlayoff(Model uiModel, HttpServletRequest request) {
        User user = userRepository.read(Long.parseLong(request.getParameter("userId")));
        List<Match> resultList = getResults(request, user);
        matchRepository.store(resultList);
        return playoff(uiModel, request);
    }

    @RequestMapping(value = "/playoff/stage",  method = RequestMethod.POST)
    public String createPlayoffStage(Model uiModel,  HttpServletRequest request) {
        String stage = request.getParameter("stage");
        Match.Stage newStage = valueOf(stage);
        switch (newStage){
            case  FINAL : {
                createFinalStage();
                break;
            }
            case  SEMI_FINAL: {
                createSemiFinalStage();
                break;
            }
            case  QUARTER_FINAL: {
                createQuarterFinalStage();
                break;
            }

            case  LAST_SIXTEEN: {
                createLastSixteenStage();
                break;
            }
            default :{

            }
        }

        return playoff(uiModel, request);

    }

    public List<Match> createLastSixteenStage() {
        Group lastSixteen = groupRepository.store(new Group("16", Collections.<String>emptyList(), Match.Stage.LAST_SIXTEEN));
        Match match1 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));
        Match match2 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));

        Match match3 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));
        Match match4 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));

        Match match5 = matchRepository.store(new Match("", "", new Date() , Match.Stage.LAST_SIXTEEN, lastSixteen.getGroupId()));
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

        Match match3 = matchRepository.store(new Match("", "", new Date() , Match.Stage.QUARTER_FINAL, quarterFinals.getGroupId()));
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
        Group finals = groupRepository.store(new Group("Final", Collections.<String>emptyList(), Match.Stage.FINAL));
        return Collections.singletonList(matchRepository.store(new Match("", "", new Date(), Match.Stage.FINAL, finals.getGroupId())));
    }

}