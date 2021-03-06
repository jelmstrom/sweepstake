package com.jelmstrom.tips;


import com.jelmstrom.tips.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping(value = "/authenticate")
public class AuthenticationController extends BaseController {

    public AuthenticationController() {
        super();
    }


    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public String login(Model uiModel, @PathVariable String token, HttpServletRequest request) {
        System.out.println("login user by token");
        User user = userRepository.findByToken(token);
        if(user.isValid()){
            System.out.println("Valid token");
            request.getSession().setAttribute(SESSION_USER, user.id);
            setSessionUsers(request, uiModel);
            return populateUserView(uiModel);
        } else {
            System.out.println("Unknown token");
            request.getSession().setAttribute(SESSION_USER, null);
            return indexModel(uiModel, request);
        }
    }

}