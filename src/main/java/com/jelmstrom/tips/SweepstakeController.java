package com.jelmstrom.tips;


import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.twitter.Tweeter;
import com.jelmstrom.tips.user.User;
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
import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
@Controller
@RequestMapping(value = "/")
@EnableScheduling
public class SweepstakeController extends BaseController {

    public SweepstakeController() {
        super();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model uiModel, HttpServletRequest request) {
        return indexModel(uiModel, request);
    }


    @RequestMapping(value = "/user/prediction/{groupId}", method = RequestMethod.POST)
    public String storePrediction(Model uiModel, @PathVariable String groupId, HttpServletRequest request) {
        String pos1 = request.getParameter("prediction1");
        String pos2 = request.getParameter("prediction2");
        String pos3 = request.getParameter("prediction3");
        String pos4 = request.getParameter("prediction4");
        System.out.println("store prediction");
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
        return groupViewModel(uiModel, request, group.getGroupId());
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

        return groupViewModel(uiModel, request, group);
    }

    // GET because form inside form creates problems
    @RequestMapping(value = "/group/match/{matchId}/delete", method = RequestMethod.GET)
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

    @RequestMapping(value = "/user/results/{groupLetter}", method = RequestMethod.POST)
    public String storeGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        User user = userRepository.read(sessionUserId(request));

        List<Match> resultList = getResults(request, user);

        matchRepository.store(resultList);

        return showGroup(uiModel, groupLetter, request);

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
        return groupViewModel(uiModel, request, groupID);
    }

    public String groupViewModel(Model uiModel, HttpServletRequest request, long groupID) {
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
}