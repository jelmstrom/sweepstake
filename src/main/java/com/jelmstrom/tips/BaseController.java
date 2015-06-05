package com.jelmstrom.tips;

import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.NeoMatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.springframework.ui.Model;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class BaseController {

    public static final String SESSION_USER = "activeUser";
    public static final String USER = "user";
    protected final Sweepstake sweepstake;


    protected UserRepository userRepository = new NeoUserRepository(Config.context);
    protected MatchRepository matchRepository = new NeoMatchRepository(Config.context);
    protected GroupRepository groupRepository = new NeoGroupRepository(Config.context);

    public BaseController() {
        sweepstake = new Sweepstake(Config.context);
    }

    protected Long sessionUserId(HttpServletRequest request) {
        return (Long) request.getSession().getAttribute(SESSION_USER);
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

    protected User setSessionUsers(HttpServletRequest request, User user, Model model) {
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

    protected User sessionUser(HttpServletRequest request) {
        Long currentSessionUser = sessionUserId(request);
        System.out.printf("Session user : %s \n", currentSessionUser);
        return userRepository.read(currentSessionUser);
    }

    public String indexModel(Model uiModel, HttpServletRequest request) {
        uiModel.addAttribute("userList", userRepository.read());
        uiModel.addAttribute("leaderBoard", sweepstake.leaderboard());
        uiModel.addAttribute("groups", groupRepository.allGroups());
        setSessionUsers(request, uiModel);
        return "index";
    }

    protected List<Match> getResults(HttpServletRequest request, User user) {
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String, Match> matchUpdates = new HashMap<>();

        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
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
        String dateString = request.getParameter(matchId + "_startTime");
        Date startTime = Config.date(dateString);
        System.out.println(homeTeam + "Start time " + dateString) ;
         if(homeTeam.equals(stored.homeTeam) && awayTeam.equals(stored.awayTeam) && startTime.equals(stored.matchStart)
           ) {
            //no change :  keep stored.
            System.out.println("Not updating") ;
            updatedMatch = stored;
        } else {
            //preserve details that are not possible to change
            System.out.println("Updating") ;
            updatedMatch = new Match(homeTeam, awayTeam, startTime, stored.stage, stored.groupId);
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


    public String populateUserView(Model uiModel) {
        List<String> teams = groupRepository.allGroups().stream().flatMap(group -> group.teams.stream()).sorted().collect(toList());
        uiModel.addAttribute("teams", teams);
        uiModel.addAttribute("groups", groupRepository.allGroups());
        return "user";
    }
}
