package com.jelmstrom.tips;


import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Stream;

import static com.jelmstrom.tips.match.Match.Stage.*;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
@Controller
@RequestMapping(value = "/")
public class SweepstakeController {

    public static final String SESSION_USER = "activeUser";
    public static final String USER = "user";
    private final Sweepstake sweepstake;

    public SweepstakeController() {
        sweepstake = new Sweepstake(Config.context);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model uiModel, HttpServletRequest request) {
        uiModel.addAttribute("userList", sweepstake.getUsers());
        uiModel.addAttribute("leaderBoard", sweepstake.fasterLeaderboard());
        setSessionUsers(request, uiModel);
        return "index";
    }


    @RequestMapping(value = "/playoff",  method = RequestMethod.GET)
    public String playoff(Model uiModel, HttpServletRequest request) {
        User sessionUser = setSessionUsers(request, uiModel);
        List<Match> allMatches = sweepstake.getMatches();
        List<Match> last16 = allMatches.stream().filter(match-> match.stage == LAST_SIXTEEN).sorted().collect(toList());
        List<Match> quarterFinal = allMatches.stream().filter(match-> match.stage == QUARTER_FINAL).sorted().collect(toList());
        List<Match> semiFinal = allMatches.stream().filter(match-> match.stage == SEMI_FINAL).sorted().collect(toList());
        List<Match> finals = allMatches.stream().filter(match-> match.stage == FINAL || match.stage == BRONZE).sorted().collect(toList());
        uiModel.addAttribute("last16", last16);
        uiModel.addAttribute("quarterFinal", quarterFinal);
        uiModel.addAttribute("semiFinal", semiFinal);
        uiModel.addAttribute("final", finals);
        List<String> teams = sweepstake.getAllTeams();
        uiModel.addAttribute("teams", teams);
        uiModel.addAttribute("playoffTreeEditable", new Boolean(sessionUser.admin || new Date().before(Config.playoffStart)));
        return "playoff";
    }
    @RequestMapping(value = "/playoff",  method = RequestMethod.POST)
    public String savePlayoff(Model uiModel, HttpServletRequest request) {
        User user = sweepstake.getUser(sessionUserId(request));
        List<Match> resultList = getResults(request, user);
        System.out.println("Saving playoff results " + resultList);
        sweepstake.saveResults(resultList, user);
        return playoff(uiModel, request);
    }

    @RequestMapping(value = "/prediction/{groupLetter}", method = RequestMethod.POST)
    public String storePrediction(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        String pos1 = request.getParameter("prediction1");
        String pos2 = request.getParameter("prediction2");
        String pos3 = request.getParameter("prediction3");
        String pos4 = request.getParameter("prediction4");

        User user = sweepstake.getUser(sessionUserId(request));
        sweepstake.saveUserPrediction(new TablePrediction("Group" + groupLetter, user.id, Arrays.asList(pos1, pos2, pos3, pos4)));

        return showGroup(uiModel, groupLetter, request);
    }


    @RequestMapping(value = "/group/{groupLetter}", method = RequestMethod.POST)
    public String storeGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        User user = sweepstake.getUser(sessionUserId(request));

        List<Match> resultList = getResults(request, user);

        System.out.println("Saving results " + resultList);
        sweepstake.saveResults(resultList, user);
        return showGroup(uiModel, groupLetter, request);

    }

    private List<Match> getResults(HttpServletRequest request, User user) {
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String, Match> matchUpdates = new HashMap<>();

        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String value = request.getParameter(parameterName);
            System.out.println("Parameter : " + parameterName + " = " + value);
            int endIndex = parameterName.indexOf("_");

            if(endIndex == -1){
                continue;
            }

            String matchId = parameterName.substring(0, endIndex);

            if(matchUpdates.containsKey(matchId)){
                continue;
            }

            Match stored = sweepstake.getMatch(matchId);
            Match updatedMatch = updateMatchTeams(request, matchId, stored);
            addResultToMatch(request, user, updatedMatch);
            matchUpdates.put(matchId, updatedMatch);
        }

        return matchUpdates.entrySet().stream().map(entry -> entry.getValue()).collect(toList());
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
            updatedMatch = new Match(homeTeam, awayTeam, stored.matchStart, matchId, stored.stage);
            System.out.println("Updated match details : " + updatedMatch);
            updatedMatch.results.addAll(stored.results);
        }
        return updatedMatch;
    }

    public void addResultToMatch(HttpServletRequest request, User user, Match match) {
        Result previousResult = match.resultFor(user.id);

        Result result = new Result(match,
                getHomeGoals(request, match, previousResult),
                getAwayGoals(request, match, previousResult),
                user.id,
                getWinner(request, match, previousResult));

        System.out.println("Added result : " + result);

        if(user.admin){
            match.setCorrectResult(result);
            System.out.println("Added Correct result : " + match);
        }

    }

    public String getWinner(HttpServletRequest request, Match match, Result previousResult) {
        String winner = request.getParameter(match.id + "_promoted");

        if(winner == null){
            winner = previousResult.promoted;
        }
        return winner;
    }

    public Integer getAwayGoals(HttpServletRequest request, Match match, Result previousResult) {
        String awayGoals = request.getParameter(match.id + "_a");
        Integer away_Goals = StringUtils.isEmptyOrWhitespace(awayGoals) ? null : Integer.parseInt(awayGoals);
        if(away_Goals == null ){
            away_Goals = previousResult.awayGoals;
        }
        return away_Goals;
    }

    public Integer getHomeGoals(HttpServletRequest request, Match match, Result previousResult) {
        String homeGoals = request.getParameter(match.id + "_h");
        Integer home_Goals = StringUtils.isEmptyOrWhitespace(homeGoals) ? null : Integer.parseInt(homeGoals);
        if(home_Goals == null ){
            home_Goals = previousResult.homeGoals;
        }
        return home_Goals;
    }

    public boolean hasResults(String homeGoals, String awayGoals) {
        boolean result = !(StringUtils.isEmptyOrWhitespace(homeGoals)
                && StringUtils.isEmptyOrWhitespace(awayGoals));
        System.out.println(" result : " + homeGoals +":"+ awayGoals + " => " + result);
        return result;
    }

    private Result buildResult(User user, Map<String, int[]> results, String key) {
        int[] resultPair = results.get(key);
        System.out.println(String.format("Creating Result for %s", key));
        return new Result(new Match("", "", null, key)
                , resultPair[0]
                , resultPair[1]
                , user.id);
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Model uiModel, HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_USER, null);
        return index(uiModel, request);
    }

    @RequestMapping(value = "/group/{groupLetter}", method = RequestMethod.GET)
    public String showGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {

        List<TableEntry> tableEntries = sweepstake.currentStandingsForGroup("Group" + groupLetter);
        List<TablePrediction> predictions = sweepstake.getPredictions(sessionUserId(request));
        Optional<TablePrediction> maybe = predictions.stream().filter(entry -> entry.group.equals("Group" + groupLetter)).findFirst();
        List<Match> groupMatches = sweepstake.getMatches().stream().filter(match -> match.id.contains(groupLetter) && match.stage.equals(GROUP)).sorted().collect(toList());
        setSessionUsers(request, uiModel);

        uiModel.addAttribute("matches", groupMatches);
        uiModel.addAttribute("group", groupLetter);
        uiModel.addAttribute("currentStandings", tableEntries);
        uiModel.addAttribute("teams", tableEntries.stream().map(entry -> entry.team).collect(toList()));
        uiModel.addAttribute("maybe", maybe.orElse(new TablePrediction("", "", Collections.emptyList())));
        uiModel.addAttribute("prediction", maybe.isPresent() ? maybe.get() : new TablePrediction("", "", Arrays.asList("", "", "", "")));
        return "group";
    }


    @RequestMapping(value = "/user/{displayName}", method = RequestMethod.GET)
    public String getUser(Model uiModel, @PathVariable String displayName, HttpServletRequest request) {

        setSessionUsers(request, sweepstake.findUser(displayName), uiModel);
        List<String> teams = sweepstake.getAllTeams();
        uiModel.addAttribute("teams", teams);
        return "user";
    }

    private String sessionUserId(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(SESSION_USER);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String getUser(Model uiModel, HttpServletRequest request) {
        setSessionUsers(request, uiModel);
        List<String> teams = sweepstake.getAllTeams();
        uiModel.addAttribute("teams", teams);
        return "user";
    }

    @RequestMapping(value = "/authenticate/{token}", method = RequestMethod.GET)
    public String login(Model uiModel, @PathVariable String token, HttpServletRequest request) {
        System.out.println("login user by token");
        User user = sweepstake.login(token);
        if(user.isValid()){
            request.getSession().setAttribute(SESSION_USER, user.id);
            setSessionUsers(request, user, uiModel);
            List<String> teams = sweepstake.getAllTeams();
            uiModel.addAttribute("teams", teams);
            return "user";
        } else {
            return index(uiModel, request);
        }
    }

    @RequestMapping(value = "/delete/{userId}", method = RequestMethod.POST)
    public String deleteUser(Model uiModel, @PathVariable String userId, HttpServletRequest request) {
        sweepstake.deleteUser(userId);
        return index(uiModel, request);
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String updateUser(Model uiModel, HttpServletRequest request) {

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
            if (StringUtils.isEmptyOrWhitespace(sessionUserId(request))) {
                System.out.println("Logging in new user");
                request.getSession().setAttribute(SESSION_USER, user.id);
            }
        }

        setSessionUsers(request, user, uiModel);
        List<String> teams = sweepstake.getAllTeams();
        uiModel.addAttribute("teams", teams);
        return "user";
    }

    private User saveUserDetails(HttpServletRequest request, Sweepstake sweepstake) {
        System.out.println(String.format("updating user %s, %s %s",  request.getParameter("userId")
                ,request.getParameter("displayName")
                , request.getParameter("email")));
        User user = new User(request.getParameter("userId")
                , request.getParameter("displayName")
                , request.getParameter("email")
                , null != request.getParameter("isAdmin")
                , UUID.randomUUID().toString());
        user.setTopScorer(request.getParameter("topScorer"));
        user.setWinner(request.getParameter("winner"));
        user = sweepstake.saveUser(user);
        System.out.println(String.format("user %s updated %s", user.displayName, (user.admin?" -> admin <-":"")));
        return user;
    }

    private User createUser(HttpServletRequest request, Sweepstake sweepstake) {
        Stream.of(request.getParameterNames()).forEach(System.out::println);
        User user = new User(request.getParameter("displayName")
                , request.getParameter("email")
                , false
                , UUID.randomUUID().toString());
        System.out.println(String.format("create user %s ", user.email));
        return sweepstake.saveUser(user);
    }

    private boolean isNewUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("new"));
    }

    private boolean isUpdateUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("action"));
    }


    private User setSessionUsers(HttpServletRequest request, Model uiModel) {
        return setSessionUsers(request, User.emptyUser(), uiModel);
    }

    private User setSessionUsers(HttpServletRequest request, User user, Model model) {
        User sessionUser = sessionUser(request);
        if (!user.isValid()) {
            user = sessionUser;
        }

        System.out.println(String.format("User : %s", user.email));
        boolean editable = (user.isValid() && user.equals(sessionUser)) || sessionUser.admin;
        model.addAttribute("canEdit", editable);
        model.addAttribute(USER, user);
        model.addAttribute(SESSION_USER, sessionUser);
        return sessionUser;
    }


    private User sessionUser(HttpServletRequest request) {
        String currentSessionUser = sessionUserId(request);
        System.out.println(String.format("Session user : %s", currentSessionUser));
        return sweepstake.getUser(currentSessionUser);
    }
}