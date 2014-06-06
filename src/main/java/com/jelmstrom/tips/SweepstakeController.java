package com.jelmstrom.tips;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings("UnusedDeclaration")
@Controller
public class SweepstakeController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "helloWorld";
    }
}