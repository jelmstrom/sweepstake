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
public class UserController extends BaseController {

    public UserController() {
        super();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model uiModel, HttpServletRequest request) {
        return indexModel(uiModel, request);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Model uiModel, HttpServletRequest request) {
        request.getSession().setAttribute(SESSION_USER, null);
        return index(uiModel, request);
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