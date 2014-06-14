package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ConfigurationLoader {

    public static final String BRAZIL = "Brazil";
    public static final String CROATIA = "Croatia";
    public static final String MEXICO = "Mexico";
    public static final String CAMEROON = "Cameroon";
    public static final String SPAIN = "Spain";
    public static final String HOLLAND = "Holland";
    public static final String CHILE = "Chile";
    public static final String AUSTRALIA = "Australia";
    public static final String IVORY_COAST = "Ivory Coast";
    public static final String JAPAN = "Japan";
    public static final String GREECE = "Greece";
    public static final String COLOMBIA = "Colombia";
    public static final String URUGUAY = "Uruguay";
    public static final String COSTA_RICA = "Costa Rica";
    public static final String ENGLAND = "England";
    public static final String ITALY = "Italy";
    public static final String SWITZERLAND = "Switzerland";
    public static final String ARGENTINA = "Argentina";
    public static final String GERMANY = "Germany";
    public static final String BELGIUM = "Belgium";
    public static final String ALGERIA = "Algeria";
    public static final String PORTUGAL = "Portugal";
    public static final String BOSNIA_HERCEGOVINA = "Bosnia & Hercegovina";
    public static final String ECUADOR = "Ecuador";
    public static final String FRANCE = "France";
    public static final String GHANA = "Ghana";
    public static final String RUSSIA = "Russia";
    public static final String SOUTH_KOREA = "South Korea";
    public static final String U_S_A = "U.S.A";
    public static final String IRAN = "Iran";
    public static final String HONDURAS = "Honduras";
    public static final String NIGERIA = "Nigeria";

    public static final Group GROUP_A = new Group("GroupA", Arrays.asList(BRAZIL, CROATIA, MEXICO, CAMEROON));
    public static final Group GROUP_B = new Group("GroupB", Arrays.asList(SPAIN, HOLLAND, CHILE, AUSTRALIA));
    public static final Group GROUP_C = new Group("GroupC", Arrays.asList(COLOMBIA, GREECE, IVORY_COAST, JAPAN));
    public static final Group GROUP_D = new Group("GroupD", Arrays.asList(URUGUAY, COSTA_RICA, ENGLAND, ITALY));
    public static final Group GROUP_E = new Group("GroupE", Arrays.asList(SWITZERLAND, ECUADOR, FRANCE, HONDURAS));
    public static final Group GROUP_F = new Group("GroupF", Arrays.asList(ARGENTINA, BOSNIA_HERCEGOVINA, IRAN, NIGERIA));
    public static final Group GROUP_G = new Group("GroupG", Arrays.asList(GERMANY, PORTUGAL, GHANA, U_S_A));
    public static final Group GROUP_H = new Group("GroupH", Arrays.asList(BELGIUM, ALGERIA, RUSSIA, SOUTH_KOREA));

    private static final DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy hh:mm");


    private static void createGroups(GroupRepository groupRepository) {
        if(groupRepository.read("GroupA").teams.isEmpty()){
            groupRepository.store(GROUP_A);
            groupRepository.store(GROUP_B);
            groupRepository.store(GROUP_C);
            groupRepository.store(GROUP_D);
            groupRepository.store(GROUP_E);
            groupRepository.store(GROUP_F);
            groupRepository.store(GROUP_G);
            groupRepository.store(GROUP_H);
        }
    }

    public static void initialiseData(String context) {
        GroupRepository groupRepository = new GroupRepository(context);
        if(groupRepository.groupCollection.find().count() > 0){
            //data already configured for context
            return;
        }
        System.out.println("Create groups");
        createGroups(groupRepository);
        System.out.println("Create matches");
        createMatches(new MatchRepository(context));
        System.out.println("Create Admin user");
        createAdminUser(new UserRepository(context));
        System.out.println("Data configured");
    }

    private static void createAdminUser(UserRepository userRepo) {
        userRepo.store(new User("Admin", "none@noreply.zzz", "admin", true, ""));
    }

    private static void createMatches(MatchRepository matchRepository){
        if(matchRepository.read().isEmpty()){
            List<Match> matches = new ArrayList<>();
            try {
                matches.add(new Match(BRAZIL, CROATIA, dateFormat.parse("12 06 2014 17:00"),"A1"));
                matches.add(new Match(MEXICO, CAMEROON, dateFormat.parse("13 06 2014 13:00"),"A2"));
                matches.add(new Match(BRAZIL, MEXICO, dateFormat.parse("17 06 2014 16:00"),"A3"));
                matches.add(new Match(CAMEROON, CROATIA, dateFormat.parse("18 06 2014 19:00"),"A4"));
                matches.add(new Match(CAMEROON, BRAZIL, dateFormat.parse("23 06 2014 17:00"),"A5"));
                matches.add(new Match(CROATIA, MEXICO, dateFormat.parse("23 06 2014 17:00"),"A6"));

                matches.add(new Match(SPAIN, HOLLAND, dateFormat.parse("13 06 2014 16:00"),"B1"));
                matches.add(new Match(CHILE, AUSTRALIA, dateFormat.parse("13 06 2014 19:00"),"B2"));
                matches.add(new Match(AUSTRALIA, HOLLAND, dateFormat.parse("18 06 2014 13:00"),"B3"));
                matches.add(new Match(SPAIN, CHILE, dateFormat.parse("18 06 2014 16:00"),"B4"));
                matches.add(new Match(AUSTRALIA, SPAIN, dateFormat.parse("23 06 2014 13:00 "),"B5"));
                matches.add(new Match(HOLLAND, CHILE, dateFormat.parse("23 06 2014 13:00"),"B6"));

                matches.add(new Match(GROUP_C.teams.get(0), GROUP_C.teams.get(1), dateFormat.parse("14 06 2014 13:00"),"C1"));
                matches.add(new Match(GROUP_C.teams.get(2), GROUP_C.teams.get(3), dateFormat.parse("14 06 2014 22:00"),"C2"));
                matches.add(new Match(GROUP_C.teams.get(0), GROUP_C.teams.get(2), dateFormat.parse("19 06 2014 13:00"),"C3"));
                matches.add(new Match(GROUP_C.teams.get(3), GROUP_C.teams.get(1), dateFormat.parse("19 06 2014 19:00"),"C4"));
                matches.add(new Match(GROUP_C.teams.get(3), GROUP_C.teams.get(0), dateFormat.parse("24 06 2014 17:00"),"C5"));
                matches.add(new Match(GROUP_C.teams.get(1), GROUP_C.teams.get(2), dateFormat.parse("24 06 2014 17:00"),"C6"));

                matches.add(new Match(GROUP_D.teams.get(0), GROUP_D.teams.get(1), dateFormat.parse("14 06 2014 16:00"),"D1"));
                matches.add(new Match(GROUP_D.teams.get(2), GROUP_D.teams.get(3), dateFormat.parse("14 06 2014 19:00"),"D2"));
                matches.add(new Match(GROUP_D.teams.get(0), GROUP_D.teams.get(2), dateFormat.parse("19 06 2014 16:00"),"D3"));
                matches.add(new Match(GROUP_D.teams.get(3), GROUP_D.teams.get(1), dateFormat.parse("20 06 2014 13:00"),"D4"));
                matches.add(new Match(GROUP_D.teams.get(3), GROUP_D.teams.get(0), dateFormat.parse("24 06 2014 13:00"),"D5"));
                matches.add(new Match(GROUP_D.teams.get(1), GROUP_D.teams.get(2), dateFormat.parse("24 06 2014 13:00"),"D6"));


                matches.add(new Match(GROUP_E.teams.get(0), GROUP_E.teams.get(1), dateFormat.parse("15 06 2014 13:00"),"E1"));
                matches.add(new Match(GROUP_E.teams.get(2), GROUP_E.teams.get(3), dateFormat.parse("15 06 2014 16:00"),"E2"));
                matches.add(new Match(GROUP_E.teams.get(0), GROUP_E.teams.get(2), dateFormat.parse("20 06 2014 16:00"),"E3"));
                matches.add(new Match(GROUP_E.teams.get(3), GROUP_E.teams.get(1), dateFormat.parse("20 06 2014 19:00"),"E4"));
                matches.add(new Match(GROUP_E.teams.get(3), GROUP_E.teams.get(0), dateFormat.parse("25 06 2014 17:00"),"E5"));
                matches.add(new Match(GROUP_E.teams.get(1), GROUP_E.teams.get(2), dateFormat.parse("25 06 2014 17:00"),"E6"));


                matches.add(new Match(GROUP_F.teams.get(0), GROUP_F.teams.get(1), dateFormat.parse("15 06 2014 19:00"),"F1"));
                matches.add(new Match(GROUP_F.teams.get(2), GROUP_F.teams.get(3), dateFormat.parse("16 06 2014 16:00"),"F2"));
                matches.add(new Match(GROUP_F.teams.get(0), GROUP_F.teams.get(2), dateFormat.parse("21 06 2014 13:00"),"F3"));
                matches.add(new Match(GROUP_F.teams.get(3), GROUP_F.teams.get(1), dateFormat.parse("21 06 2014 19:00"),"F4"));
                matches.add(new Match(GROUP_F.teams.get(3), GROUP_F.teams.get(0), dateFormat.parse("25 06 2014 13:00"),"F5"));
                matches.add(new Match(GROUP_F.teams.get(1), GROUP_F.teams.get(2), dateFormat.parse("25 06 2014 13:00"),"F6"));


                matches.add(new Match(GROUP_G.teams.get(0), GROUP_G.teams.get(1), dateFormat.parse("16 06 2014 13:00"),"G1"));
                matches.add(new Match(GROUP_G.teams.get(2), GROUP_G.teams.get(3), dateFormat.parse("16 06 2014 19:00"),"G2"));
                matches.add(new Match(GROUP_G.teams.get(0), GROUP_G.teams.get(2), dateFormat.parse("21 06 2014 16:00"),"G3"));
                matches.add(new Match(GROUP_G.teams.get(3), GROUP_G.teams.get(1), dateFormat.parse("21 06 2014 19:00"),"G4"));
                matches.add(new Match(GROUP_G.teams.get(3), GROUP_G.teams.get(0), dateFormat.parse("26 06 2014 13:00"),"G5"));
                matches.add(new Match(GROUP_G.teams.get(1), GROUP_G.teams.get(2), dateFormat.parse("26 06 2014 13:00"),"G6"));

                matches.add(new Match(GROUP_H.teams.get(0), GROUP_H.teams.get(1), dateFormat.parse("17 06 2014 13:00"),"H1"));
                matches.add(new Match(GROUP_H.teams.get(2), GROUP_H.teams.get(3), dateFormat.parse("17 06 2014 19:00"),"H2"));
                matches.add(new Match(GROUP_H.teams.get(0), GROUP_H.teams.get(2), dateFormat.parse("22 06 2014 13:00"),"H3"));
                matches.add(new Match(GROUP_H.teams.get(3), GROUP_H.teams.get(1), dateFormat.parse("23 06 2014 16:00"),"H4"));
                matches.add(new Match(GROUP_H.teams.get(3), GROUP_H.teams.get(0), dateFormat.parse("26 06 2014 17:00"),"H5"));
                matches.add(new Match(GROUP_H.teams.get(1), GROUP_H.teams.get(2), dateFormat.parse("26 06 2014 17:00"),"H6"));

                matchRepository.store(matches);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalStateException("Incorrect configuration, failed to parse date");
            }
       }
    }
}
