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
            neoUserRepository.store(new User("aptitud", "aptitud", false, "aptitud"));
        }


        if(neoGroupRepository.allGroups().isEmpty()){
            System.out.println("Adding group");


            neoGroupRepository.store(new Group("A",
                    Arrays.asList("@ahlinder", "@anderslowenborg", "@j_elmstrom", "@jessicaWikander"
                    , "@magnuh", "@per_jansson", "@peterqwarnstrom","@tgranbacka", "ahnlund" , "david"
                    , "Hampus", "Johannes", "Nilsson", "Rååger")));

            Group ls = neoGroupRepository.store(new Group("QF", Collections.<String>emptyList(), Match.Stage.LAST_SIXTEEN));
            Group qf = neoGroupRepository.store(new Group("QF", Collections.<String>emptyList(), Match.Stage.QUARTER_FINAL));
            Group sf = neoGroupRepository.store(new Group("QF", Collections.<String>emptyList(), Match.Stage.SEMI_FINAL));
            Group fin = neoGroupRepository.store(new Group("QF", Collections.<String>emptyList(), Match.Stage.FINAL));
            Group br = neoGroupRepository.store(new Group("QF", Collections.<String>emptyList(), Match.Stage.BRONZE));

            Match finalMatch = matches.store(new Match("", "", Config.date("2014-12-05 16:25"), Match.Stage.FINAL, fin.getGroupId()));

            Match bronzeMatch = matches.store(new Match("", "", Config.date("2014-12-05 16:20"), Match.Stage.BRONZE, br.getGroupId()));

            Match sfMatch = matches.store(new Match("", "", Config.date("2014-12-05 16:15"), Match.Stage.SEMI_FINAL, sf.getGroupId()));
            Match sfMatch2 = matches.store(new Match("", "", Config.date("2014-12-05 16:15"), Match.Stage.SEMI_FINAL, sf.getGroupId()));

            matches.addRelation(sfMatch, "homeTeam", finalMatch);
            matches.addRelation(sfMatch2, "awayTeam", finalMatch);

            Match qf1 = matches.store(new Match("", "", Config.date("2014-12-05 16:10"), Match.Stage.QUARTER_FINAL, qf.getGroupId()));
            Match qf2 = matches.store(new Match("", "", Config.date("2014-12-05 16:10"), Match.Stage.QUARTER_FINAL, qf.getGroupId()));
            Match qf3 = matches.store(new Match("", "", Config.date("2014-12-05 16:05"), Match.Stage.QUARTER_FINAL, qf.getGroupId()));
            Match qf4 = matches.store(new Match("", "", Config.date("2014-12-05 16:05"), Match.Stage.QUARTER_FINAL, qf.getGroupId()));


            matches.addRelation(qf1, "homeTeam", sfMatch);
            matches.addRelation(qf2, "awayTeam", sfMatch);

            matches.addRelation(qf3, "homeTeam", sfMatch2);
            matches.addRelation(qf4, "awayTeam", sfMatch2);

            Match ls1 = matches.store(new Match("", "", Config.date("2014-12-05 15:30"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));
            Match ls2 = matches.store(new Match("", "", Config.date("2014-12-05 15:30"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));
            Match ls3 = matches.store(new Match("", "", Config.date("2014-12-05 15:40"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));
            Match ls4 = matches.store(new Match("", "", Config.date("2014-12-05 15:40"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));
            Match ls5 = matches.store(new Match("", "", Config.date("2014-12-05 15:50"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));
            Match ls6 = matches.store(new Match("", "", Config.date("2014-12-05 15:50"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));
            Match ls7 = matches.store(new Match("", "", Config.date("2014-12-05 15:55"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));
            Match ls8 = matches.store(new Match("", "", Config.date("2014-12-05 15:55"), Match.Stage.LAST_SIXTEEN, ls.getGroupId()));



            matches.addRelation(ls1, "homeTeam", qf1);
            matches.addRelation(ls2, "awayTeam", qf1);


            matches.addRelation(ls3, "homeTeam", qf2);
            matches.addRelation(ls4, "awayTeam", qf2);

            matches.addRelation(ls5, "homeTeam", qf3);
            matches.addRelation(ls6, "awayTeam", qf3);

            matches.addRelation(ls7, "homeTeam", qf4);
            matches.addRelation(ls8, "awayTeam", qf4);

        }
    }

    public static Date date(String dateString) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateString);
    }

}
