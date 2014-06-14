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
    public String index(Model uiModel,HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        uiModel.addAttribute("userList", sweepstake.getUsers());
        uiModel.addAttribute("leaderBoard", sweepstake.getLeaderBoard());
        setActiveUserModel(uiModel, request);
        return "index";
    }

    @RequestMapping(value = "/prediction/{groupLetter}", method = RequestMethod.POST)
    public String storePrediction(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request){
        String pos1 = request.getParameter("prediction1");
        String pos2 = request.getParameter("prediction2");
        String pos3 = request.getParameter("prediction3");
        String pos4 = request.getParameter("prediction4");
        new Sweepstake(context).saveUserPrediction(new TablePrediction(sessionUser(request), "Group" + groupLetter, Arrays.asList(pos1, pos2, pos3, pos4)));

        return showGroup(uiModel, groupLetter, request);
    }


    @RequestMapping(value = "/group/{groupLetter}", method = RequestMethod.POST)
   public String storeGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request){
        Enumeration<String> parameterNames = request.getParameterNames();
        Sweepstake sweepstake = new Sweepstake(context);
        User user = sweepstake.getUser(sessionUser(request));
        Map<String, int[]> results = new HashMap<>();

        while(parameterNames.hasMoreElements()){
            String parameterName = parameterNames.nextElement();
            String parameter = request.getParameter(parameterName);
            if(StringUtils.isEmpty(parameter)){
                continue;
            }
            String matchId = parameterName.substring(0, 2);
            int[] resultPair = results.get(matchId);
            if(null == resultPair){
                resultPair = new int[2];
            }
            int position = parameterName.endsWith("h")?0:1;
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
    public String logout(Model uiModel ,HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_USER, null);
        return index(uiModel, request);
    }

    @RequestMapping(value = "/group/{groupLetter}", method = RequestMethod.GET)
    public String showGroup(Model uiModel, @PathVariable String groupLetter, HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        List<TableEntry> tableEntries = sweepstake.currentStandingsForGroup("Group"+groupLetter);
        List<TablePrediction> predictions = sweepstake.getPredictions(sessionUser(request));
        Optional<TablePrediction> maybe = predictions.stream().filter(entry -> entry.group.equals("Group"+groupLetter)).findFirst();
        List<Match> groupMatches = sweepstake.getMatches().stream().filter(match -> match.id.contains(groupLetter)).sorted().collect(toList());

        uiModel.addAttribute("matches", groupMatches);
        uiModel.addAttribute("group", groupLetter);
        uiModel.addAttribute("currentStandings", tableEntries);
        uiModel.addAttribute("teams", tableEntries.stream().map(entry -> entry.team).collect(toList()));
        uiModel.addAttribute("maybe", maybe.orElse(new TablePrediction("", "", Collections.emptyList())));
        uiModel.addAttribute("prediction", maybe.isPresent() ? maybe.get().tablePrediction : Arrays.asList("", "", "", ""));
        setActiveUserModel(uiModel, request);
        return "group";
    }


    @RequestMapping(value = "/user/{displayName}", method = RequestMethod.GET)
    public String getUser(Model uiModel, @PathVariable String displayName, HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        setActiveUserModel(uiModel, request);
        uiModel.addAttribute("user", sweepstake.findUser(displayName));
        return "user";
    }

    private void setActiveUserModel(Model uiModel, HttpServletRequest request) {
        User user = new Sweepstake(context).getUser(sessionUser(request));
        uiModel.addAttribute(SESSION_USER, user);
        boolean editable = !StringUtils.isEmpty(user.displayName);
        uiModel.addAttribute("canEdit", editable);
    }

    private String sessionUser(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(SESSION_USER);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String getUser(Model uiModel, HttpServletRequest request) {
        setActiveUserModel(uiModel, request);
        uiModel.addAttribute("user", new User("","", "", false, ""));
        return "user";
    }

    @RequestMapping(value = "/authenticate/{token}", method = RequestMethod.GET)
    public String login(Model uiModel, @PathVariable String token, HttpServletRequest request) {

        System.out.println("login user by token");
        User user = new Sweepstake(context).login(token);
        System.out.println(user);
        setSessionUsers(request, user, uiModel);
        return "user";
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String updateUser(Model uiModel, HttpServletRequest request) {

        System.out.println("Update user");
        Sweepstake sweepstake = new Sweepstake(context);
        User user;
        if("update".equals(request.getParameter("action"))){
            System.out.println("Update");
            if(isUpdateUser(request)) {
                user = updateUser(request, sweepstake);
            } else {
                user = authenticateUser(request, sweepstake);
            }
        } else {
            System.out.println("new");
            user = createUser(request, sweepstake);
        }

        setSessionUsers(request, user, uiModel);
        return "user";
    }

    private User authenticateUser(HttpServletRequest request, Sweepstake sweepstake) {
        User user = sweepstake.getUser(request.getParameter("email"));
        if(!user.credentials.equals(request.getParameter("credentials"))){
            throw new IllegalAccessError("User Credentials does not match");
        }
        System.out.println(String.format("user %s created", user.email));
        return user;
    }

    private User updateUser(HttpServletRequest request, Sweepstake sweepstake) {
        User sessionUser = sweepstake.getUser(sessionUser(request));
        String newCredentials = validateCredentialsOnUpdate(request, sessionUser);
        User user = new User(request.getParameter("displayName"), sessionUser.email, newCredentials, false, UUID.randomUUID().toString());
        System.out.println(String.format("user %s update ", user.email));
        sweepstake.saveUser(user);
        return user;
    }

    private User createUser(HttpServletRequest request, Sweepstake sweepstake) {
        Stream.of(request.getParameterNames()).forEach(System.out::println);
        User user = new User(
                        request.getParameter("displayName").toString()
                    ,   request.getParameter("email").toString()
                    , "pwd"
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

    private String validateCredentialsOnUpdate(HttpServletRequest request, User sessionUser) {
        String newCredentials = request.getParameter("newCredentials");
        String oldCredentials = request.getParameter("oldCredentials");

        if(newCredentials !=  null && !sessionUser.credentials.equals(oldCredentials)){
          throw new IllegalAccessError("Incorrect credentials for update");
        }
        return newCredentials;
    }

    private void setSessionUsers(HttpServletRequest request, User user, Model model) {
        String currentSessionUser = sessionUser(request);
        User sessionUser = new Sweepstake(context).getUser(currentSessionUser);
        if(!sessionUser.isValid()){
            sessionUser = user;
        }
        System.out.println(String.format("Session user : %s", user.email));
        request.getSession().setAttribute(SESSION_USER, user.email);
        model.addAttribute(USER, user);
        model.addAttribute(SESSION_USER, sessionUser);

    }
}