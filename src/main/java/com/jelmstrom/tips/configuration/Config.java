package com.jelmstrom.tips.configuration;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.NeoMatchRepository;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class Config {
    public static final String context = "vmtips";
    public static final DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy hh:mm");
    public static final Date playoffStart;
    public static final Date startDate;

    static {
        Date tempDate  = null;
        Date tempPlayoffStart = null;
        try {
            tempDate = dateFormat.parse("12 06 2015 17:00");
            tempPlayoffStart = dateFormat.parse("28 06 2015 18:00");
        } catch (ParseException pex){
            System.out.println("failed to parse start date");
        }
        startDate = tempDate;
        playoffStart = tempPlayoffStart;
    }

    private static NeoUserRepository neoUserRepository;


    public static void seed() throws ParseException {
        NeoGroupRepository neoGroupRepository = new NeoGroupRepository("Champions League");
        NeoMatchRepository matches = new NeoMatchRepository("Champions League");
        System.out.printf("Groups : %d\n", neoGroupRepository.allGroups().size());
        neoUserRepository = new NeoUserRepository("");
        if(!neoUserRepository.findAdminUser().isValid()){
            neoUserRepository.store(new User("admin", "admin", true, "admin"));
        }
    }

    public static Date date(String dateString) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateString);
    }

}
