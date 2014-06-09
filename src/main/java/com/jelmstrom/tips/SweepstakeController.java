package com.jelmstrom.tips;


import com.jelmstrom.tips.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
@Controller
@RequestMapping(value = "/")
public class SweepstakeController {

    public static final String context = "sweepstake";
    public static final String ACTIVE_USER = "activeUser";

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model uiModel,HttpServletRequest request) {
        ConfigurationLoader.initialiseData(context);
        Sweepstake sweepstake = new Sweepstake(context);
        uiModel.addAttribute("userList", sweepstake.getUsers());
        uiModel.addAttribute("leaderBoard", sweepstake.getLeaderBoard());
        setActiveUserModel(uiModel, request, sweepstake);
        return "helloWorld";
    }


    @RequestMapping(value = "/group/{groupName}", method = RequestMethod.GET)
    public String showGroup(Model uiModel
            , @PathVariable String groupName
            ,HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        if(StringUtils.isEmpty(sessionUser(request, sweepstake).email)){
            return index(uiModel, request);
        }
        System.out.println("getting group  " + groupName);
        uiModel.addAttribute("matches", sweepstake.getMatches().stream().filter(match -> match.id.contains(groupName)).collect(toList()));
        setActiveUserModel(uiModel, request, sweepstake);
        return "group";
    }


    @RequestMapping(value = "/user/{displayName}", method = RequestMethod.GET)
    public String getUser(Model uiModel
            , @PathVariable String displayName
            , HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake(context);
        if(StringUtils.isEmpty(sessionUser(request, sweepstake).email)){
            return index(uiModel, request);
        }
        System.out.println("getting User  " + displayName);
        uiModel.addAttribute("user", sweepstake.findUser(displayName));
        setActiveUserModel(uiModel, request, sweepstake);
        return "user";
    }

    private void setActiveUserModel(Model uiModel, HttpServletRequest request, Sweepstake sweepstake) {
        uiModel.addAttribute(ACTIVE_USER, sessionUser(request, sweepstake));
    }

    private User sessionUser(HttpServletRequest request, Sweepstake sweepstake) {
        String sessionUser = (String)request.getSession().getAttribute(ACTIVE_USER);
        System.out.println("Current session user " + sessionUser);
        return sweepstake.getUser(sessionUser);
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String getUser(Model uiModel
            , HttpServletRequest request) {

        Sweepstake sweepstake = new Sweepstake(context);

        User user;
        if(isNewUser(request)) {
            user = createUser(request, sweepstake);
        } else if(isUpdateUser(request)) {
            user = updateUser(request, sweepstake);
        } else {
            user = authenticateUser(request, sweepstake);
        }
        uiModel.addAttribute(ACTIVE_USER, user);
        uiModel.addAttribute("user", user);
        setSessionUser(request, user);
        return "user";
    }

    private User authenticateUser(HttpServletRequest request, Sweepstake sweepstake) {
        User user;
        user = sweepstake.getUser(request.getParameter("email"));
        if(!user.credentials.equals(request.getParameter("credentials"))){
            throw new IllegalAccessError("User Credentials does not match");
        }
        return user;
    }

    private User updateUser(HttpServletRequest request, Sweepstake sweepstake) {
        User user;
        User sessionUser = sessionUser(request, sweepstake);
        String newCredentials = validateCredentialsOnUpdate(request, sessionUser);

        System.out.println("updating user ");
        user = new User(request.getParameter("displayName"), sessionUser.email, newCredentials, false);

        sweepstake.saveUser(user);
        return user;
    }

    private User createUser(HttpServletRequest request, Sweepstake sweepstake) {
        User user;
        user = new User(request.getParameter("email"), request.getParameter("email"),request.getParameter("credentials"), false);
        sweepstake.saveUser(user);
        return user;
    }

    private boolean isNewUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("new"));
    }

    private boolean isUpdateUser(HttpServletRequest request) {
        return !StringUtils.isEmpty(request.getParameter("update"));
    }

    private String validateCredentialsOnUpdate(HttpServletRequest request, User sessionUser) {
        String newCredentials = request.getParameter("newCredentials");
        String oldCredentials = request.getParameter("oldCredentials");
        System.out.println(newCredentials + "" + oldCredentials);

        if(newCredentials !=  null && !sessionUser.credentials.equals(oldCredentials)){
          throw new IllegalAccessError("Incorrect credentials for update");
        }
        return newCredentials;
    }

    private void setSessionUser(HttpServletRequest request, User user) {
        System.out.println("Session user" + user.email);
        request.getSession().setAttribute(ACTIVE_USER, user.email);
    }
}