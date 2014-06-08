package com.jelmstrom.tips;


import com.jelmstrom.tips.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
@Controller
public class SweepstakeController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model uiModel,HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake();

        uiModel.addAttribute("userList", sweepstake.getUsers());
        uiModel.addAttribute("leaderBoard", sweepstake.getLeaderBoard());
        setActiveUserModel(uiModel, request, sweepstake);
        return "helloWorld";
    }


    @RequestMapping(value = "/group/{groupName}", method = RequestMethod.GET)
    public String showGroup(Model uiModel, @PathVariable String groupName,HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake();
        System.out.println("getting group  " + groupName);
        uiModel.addAttribute("matches", sweepstake.getMatches().stream().filter(match -> match.id.contains(groupName)).collect(toList()));
        setActiveUserModel(uiModel, request, sweepstake);
        return "group";
    }


    @RequestMapping(value = "/user/{displayName}", method = RequestMethod.GET)
    public String getUser(Model uiModel, @PathVariable String displayName, HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake();
        System.out.println("getting User  " + displayName);
        uiModel.addAttribute("user", sweepstake.findUser(displayName));
        setActiveUserModel(uiModel, request, sweepstake);
        return "user";
    }

    private void setActiveUserModel(Model uiModel, HttpServletRequest request, Sweepstake sweepstake) {
        uiModel.addAttribute("activeUser", sessionUser(request, sweepstake));
    }

    private User sessionUser(HttpServletRequest request, Sweepstake sweepstake) {
        return sweepstake.getUser((String)request.getSession().getAttribute("activeUser"));
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String getUser(Model uiModel, HttpServletRequest request) {
        Sweepstake sweepstake = new Sweepstake();
        boolean newUser = request.getParameter("new") != null;
        User user;
        if(newUser) {
            user = new User(request.getParameter("email"), request.getParameter("email"),request.getParameter("credentials"));
            sweepstake.saveUser(user);
            setSessionUser(request, user);
        } else if(request.getParameter("update") != null) {

            User sessionUser = sessionUser(request, sweepstake);
            String newCredentials = validateCredentialsOnUpdate(request, sessionUser);

            System.out.println("updating user ");
            user = new User(request.getParameter("displayName"), sessionUser.email, newCredentials);

            sweepstake.saveUser(user);
            setSessionUser(request, user);
        }else {
            user = sweepstake.getUser(request.getParameter("email"));
            if(user.credentials.equals(request.getParameter("credentials"))){
                setSessionUser(request, user);
            } else {
                throw new IllegalAccessError("User Credentials does not match");
            }
        }
        uiModel.addAttribute("activeUser", user);
        uiModel.addAttribute("user", user);
        return "user";
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
        request.getSession().setAttribute("activeUser", user.email);
    }
}