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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static java.time.ZonedDateTime.of;
import static java.time.format.DateTimeFormatter.ofPattern;

public class Config {
    public static final String context = "WorldCup";
    public static final ZoneId STOCKHOLM = ZoneId.of("Europe/Stockholm");
    public static String dateFormat = "yyyy-MM-dd HH:mm";
    private static ZonedDateTime playoffStart;
    private static ZonedDateTime startDate;

    static {
        startDate = date("2015-09-08 12:00");
        playoffStart = date("2015-09-22 20:00");
    }

    private static NeoUserRepository neoUserRepository;


    public static void seed() throws ParseException {
        NeoGroupRepository neoGroupRepository = new NeoGroupRepository(context);
        NeoMatchRepository matches = new NeoMatchRepository(context);
        System.out.printf("Groups : %d\n", neoGroupRepository.allGroups().size());
        neoUserRepository = new NeoUserRepository("");
        if(!neoUserRepository.findAdminUser().isValid()){
            neoUserRepository.store(new User("admin", "admin", true, "admin"));
        }
    }

    public static ZonedDateTime date(String dateString)  {
        LocalDateTime parse = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(dateFormat));
        return parse.atZone(STOCKHOLM);
    }

    public static ZonedDateTime getZonedDateTime(String dateString) {
        return of(LocalDateTime.parse(dateString, ofPattern("yyyy-MM-dd'T'HH:mm")), STOCKHOLM);
    }

    public static ZonedDateTime getPlayoffStart() {
        return playoffStart;
    }

    public static ZonedDateTime getStartDate() {
        return startDate;
    }
}
