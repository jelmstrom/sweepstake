package com.jelmstrom.tips.configuration;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.NeoGroupRepository;

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


    public static void seed(){
        NeoGroupRepository neoGroupRepository = new NeoGroupRepository("Champions League");
        System.out.printf("Groups : %d", neoGroupRepository.allGroups().size());
        if(neoGroupRepository.allGroups().isEmpty()){
            System.out.printf("Adding group");
            neoGroupRepository.store(new Group("A", Arrays.asList("Juventus","Malmö FF","Atletico Madrid", "Olympiakos")));
        }
    }

}
