package com.jelmstrom.tips.configuration;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.NeoMatchRepository;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
            tempDate = dateFormat.parse("12 06 2014 17:00");
            tempPlayoffStart = dateFormat.parse("28 06 2014 18:00");
        } catch (ParseException pex){
            System.out.println("failed to parse start date");
        }
        startDate = tempDate;
        playoffStart = tempPlayoffStart;
    }


    public static void seed() throws ParseException {
        NeoGroupRepository neoGroupRepository = new NeoGroupRepository("Champions League");
        NeoMatchRepository matches = new NeoMatchRepository("Champions League");
        System.out.printf("Groups : %d\n", neoGroupRepository.allGroups().size());
        if(neoGroupRepository.allGroups().isEmpty()){
            System.out.println("Adding group");
            Group group = new Group("A", Arrays.asList("Juventus", "Malmö FF", "Atletico Madrid", "Olympiakos"));
            neoGroupRepository.store(group);

            matches.store(new Match("Juventus", "Malmö FF", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Juventus", "Atletico Madrid", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Juventus", "Olympiakos", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Malmö FF", "Juventus", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Atletico Madrid","Juventus", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Olympiakos", "Juventus",new Date(), Match.Stage.GROUP, group.getGroupId()));

            matches.store(new Match("Atletico Madrid", "Malmö FF", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Olympiakos", "Malmö FF", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Malmö FF", "Atletico Madrid", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Malmö FF", "Olympiakos", new Date(), Match.Stage.GROUP, group.getGroupId()));

            matches.store(new Match("Atletico Madrid", "Olympiakos", new Date(), Match.Stage.GROUP, group.getGroupId()));
            matches.store(new Match("Olympiakos", "Atletico Madrid",  new Date(), Match.Stage.GROUP, group.getGroupId()));
        }
    }

}
