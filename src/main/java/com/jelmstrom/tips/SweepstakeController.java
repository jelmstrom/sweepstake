package com.jelmstrom.tips;


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

import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
@Controller
@RequestMapping(value = "/")
public class SweepstakeController {

    public static final String context = "vmtips";
    public static final String SESSION_USER = "activeUser";
    public static final String USER = "user";

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model uiModel, HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        uiModel.addAttribute("userList", sweepstake.getUsers());
        uiModel.addAttribute("leaderBoard", sweepstake.getLeaderBoard());
        setSessionUsers(request, uiModel);
        return "index";
    }

    @RequestMapping(value = "/prediction/{groupLetter}", method = RequestMethod.POST)
    public String storePrediction(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        String pos1 = request.getParameter("prediction1");
        String pos2 = request.getParameter("prediction2");
        String pos3 = request.getParameter("prediction3");
        String pos4 = request.getParameter("prediction4");
        new Sweepstake(context).saveUserPrediction(new TablePrediction(sessionUser(request), "Group" + groupLetter, Arrays.asList(pos1, pos2, pos3, pos4)));

        return showGroup(uiModel, groupLetter, request);
    }


    @RequestMapping(value = "/group/{groupLetter}", method = RequestMethod.POST)
    public String storeGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        Sweepstake sweepstake = new Sweepstake(context);
        User user = sweepstake.getUser(sessionUser(request));
        Map<String, int[]> results = new HashMap<>();

        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String parameter = request.getParameter(parameterName);
            if (StringUtils.isEmpty(parameter)) {
                continue;
            }
            String matchId = parameterName.substring(0, 2);
            int[] resultPair = results.get(matchId);
            if (null == resultPair) {
                resultPair = new int[2];
            }
            int position = parameterName.endsWith("h") ? 0 : 1;
            resultPair[position] = Integer.parseInt(parameter);
            results.put(matchId, resultPair);
        }

        List<Result> resultList = results.keySet().stream()
                .map(key -> buildResult(user, results, key))
                .collect(toList());

        System.out.println("Saving results " + resultList);
        sweepstake.saveResults(resultList, user);
        return showGroup(uiModel, groupLetter, request);

    }

    private Result buildResult(User user, Map<String, int[]> results, String key) {
        int[] resultPair = results.get(key);
        System.out.println(String.format("Creating Result for %s", key));
        return new Result(new Match("", "", null, key)
                , resultPair[0]
                , resultPair[1]
                , user.email);
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Model uiModel, HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_USER, null);
        return index(uiModel, request);
    }

    @RequestMapping(value = "/group/{groupLetter}", method = RequestMethod.GET)
    public String showGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        List<TableEntry> tableEntries = sweepstake.currentStandingsForGroup("Group" + groupLetter);
        List<TablePrediction> predictions = sweepstake.getPredictions(sessionUser(request));
        Optional<TablePrediction> maybe = predictions.stream().filter(entry -> entry.group.equals("Group" + groupLetter)).findFirst();
        List<Match> groupMatches = sweepstake.getMatches().stream().filter(match -> match.id.contains(groupLetter)).sorted().collect(toList());

        uiModel.addAttribute("matches", groupMatches);
        uiModel.addAttribute("group", groupLetter);
        uiModel.addAttribute("currentStandings", tableEntries);
        uiModel.addAttribute("teams", tableEntries.stream().map(entry -> entry.team).collect(toList()));
        uiModel.addAttribute("maybe", maybe.orElse(new TablePrediction("", "", Collections.emptyList())));
        uiModel.addAttribute("prediction", maybe.isPresent() ? maybe.get().tablePrediction : Arrays.asList("", "", "", ""));
        setSessionUsers(request, uiModel);
        return "group";
    }


    @RequestMapping(value = "/user/{displayName}", method = RequestMethod.GET)
    public String getUser(Model uiModel, @PathVariable String displayName, HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        setSessionUsers(request, sweepstake.findUser(displayName), uiModel);
        return "user";
    }

    private String sessionUser(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(SESSION_USER);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String getUser(Model uiModel, HttpServletRequest request) {
        setSessionUsers(request, uiModel);
        return "user";
    }

    @RequestMapping(value = "/authenticate/{token}", method = RequestMethod.GET)
    public String login(Model uiModel, @PathVariable String token, HttpServletRequest request) {
        System.out.println("login user by token");
        User user = new Sweepstake(context).login(token);
        if(user.isValid()){
            request.getSession().setAttribute(SESSION_USER, user.email);
            setSessionUsers(request, user, uiModel);
            return "user";
        } else {
            return index(uiModel, request);
        }
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String updateUser(Model uiModel, HttpServletRequest request) {

        System.out.println("Update user");
        Sweepstake sweepstake = new Sweepstake(context);
        User user;
        if ("update".equals(request.getParameter("action"))) {
            System.out.println("Update");
            user = updateUser(request, sweepstake);
        } else {
            System.out.println("new");
            user = createUser(request, sweepstake);
            if (null == sessionUser(request)) {
                request.getSession().setAttribute(SESSION_USER, user.email);
            }
        }

        setSessionUsers(request, user, uiModel);
        return "user";
    }

    private User updateUser(HttpServletRequest request, Sweepstake sweepstake) {
        User sessionUser = sweepstake.getUser(sessionUser(request));
        User user = new User(request.getParameter("displayName")
                , sessionUser.email
                , null != request.getParameter("isAdmin")
                , UUID.randomUUID().toString());
        sweepstake.saveUser(user);
        System.out.println(String.format("user %s updated %s", user.email, (user.admin?" -> admin <-":"")));
        return user;
    }

    private User createUser(HttpServletRequest request, Sweepstake sweepstake) {
        Stream.of(request.getParameterNames()).forEach(System.out::println);
        User user = new User(request.getParameter("displayName")
                , request.getParameter("email")
                , false
                , UUID.randomUUID().toString());
        System.out.println(String.format("create user %s ", user.email));
        sweepstake.saveUser(user);
        return user;
    }

    private boolean isNewUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("new"));
    }

    private boolean isUpdateUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("action"));
    }


    private void setSessionUsers(HttpServletRequest request, Model uiModel) {
        setSessionUsers(request, User.emptyUser(), uiModel);
    }

    private void setSessionUsers(HttpServletRequest request, User user, Model model) {
        String currentSessionUser = sessionUser(request);
        System.out.println(String.format("Session user : %s", currentSessionUser));
        User sessionUser = new Sweepstake(context).getUser(currentSessionUser);
        if (!user.isValid()) {
            user = sessionUser;
        }

        System.out.println(String.format("User : %s", user.email));
        boolean editable = (user.isValid() && user.equals(sessionUser)) || sessionUser.admin;
        model.addAttribute("canEdit", editable);
        model.addAttribute(USER, user);
        model.addAttribute(SESSION_USER, sessionUser);
    }
}