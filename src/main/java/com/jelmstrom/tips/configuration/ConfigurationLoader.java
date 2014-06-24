package com.jelmstrom.tips.configuration;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;

import java.text.ParseException;
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


    public static void initialiseData(String context) {
        GroupRepository groupRepository = new GroupRepository(context);
        UserRepository userRepo = new UserRepository(context);
        MatchRepository matchRepository = new MatchRepository(context);

        if (groupRepository.groupCollection.find().count() == 0) {
            System.out.println("Create groups");
            createGroups(groupRepository);
            System.out.println("Create matches");
            createMatches(matchRepository);
            System.out.println("Create Admin user");
            createAdminUser(userRepo);
            System.out.println("Data configured");
        }
        User admin = userRepo.findByEmail("none@noreply.zzz");
        userRepo.store(new User(admin.id, "Admin", "none@noreply.zzz", true, "__admin__"));

        createPlayoffMatches(matchRepository);
    }

    private static void createGroups(GroupRepository groupRepository) {
        if (groupRepository.read("GroupA").teams.isEmpty()) {
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

    private static void createAdminUser(UserRepository userRepo) {
        userRepo.store(new User("Admin", "none@noreply.zzz", true, ""));
    }

    private static void createMatches(MatchRepository matchRepository) {
        if (matchRepository.read().isEmpty()) {
            List<Match> matches = new ArrayList<>();
            try {
                matches.add(new Match(BRAZIL, CROATIA, Config.dateFormat.parse("12 06 2014 17:00"), "A1"));
                matches.add(new Match(MEXICO, CAMEROON, Config.dateFormat.parse("13 06 2014 13:00"), "A2"));
                matches.add(new Match(BRAZIL, MEXICO, Config.dateFormat.parse("17 06 2014 16:00"), "A3"));
                matches.add(new Match(CAMEROON, CROATIA, Config.dateFormat.parse("18 06 2014 19:00"), "A4"));
                matches.add(new Match(CAMEROON, BRAZIL, Config.dateFormat.parse("23 06 2014 17:00"), "A5"));
                matches.add(new Match(CROATIA, MEXICO, Config.dateFormat.parse("23 06 2014 17:00"), "A6"));

                matches.add(new Match(SPAIN, HOLLAND, Config.dateFormat.parse("13 06 2014 16:00"), "B1"));
                matches.add(new Match(CHILE, AUSTRALIA, Config.dateFormat.parse("13 06 2014 19:00"), "B2"));
                matches.add(new Match(AUSTRALIA, HOLLAND, Config.dateFormat.parse("18 06 2014 13:00"), "B3"));
                matches.add(new Match(SPAIN, CHILE, Config.dateFormat.parse("18 06 2014 16:00"), "B4"));
                matches.add(new Match(AUSTRALIA, SPAIN, Config.dateFormat.parse("23 06 2014 13:00 "), "B5"));
                matches.add(new Match(HOLLAND, CHILE, Config.dateFormat.parse("23 06 2014 13:00"), "B6"));

                matches.add(new Match(GROUP_C.teams.get(0), GROUP_C.teams.get(1), Config.dateFormat.parse("14 06 2014 13:00"), "C1"));
                matches.add(new Match(GROUP_C.teams.get(2), GROUP_C.teams.get(3), Config.dateFormat.parse("14 06 2014 22:00"), "C2"));
                matches.add(new Match(GROUP_C.teams.get(0), GROUP_C.teams.get(2), Config.dateFormat.parse("19 06 2014 13:00"), "C3"));
                matches.add(new Match(GROUP_C.teams.get(3), GROUP_C.teams.get(1), Config.dateFormat.parse("19 06 2014 19:00"), "C4"));
                matches.add(new Match(GROUP_C.teams.get(3), GROUP_C.teams.get(0), Config.dateFormat.parse("24 06 2014 17:00"), "C5"));
                matches.add(new Match(GROUP_C.teams.get(1), GROUP_C.teams.get(2), Config.dateFormat.parse("24 06 2014 17:00"), "C6"));

                matches.add(new Match(GROUP_D.teams.get(0), GROUP_D.teams.get(1), Config.dateFormat.parse("14 06 2014 16:00"), "D1"));
                matches.add(new Match(GROUP_D.teams.get(2), GROUP_D.teams.get(3), Config.dateFormat.parse("14 06 2014 19:00"), "D2"));
                matches.add(new Match(GROUP_D.teams.get(0), GROUP_D.teams.get(2), Config.dateFormat.parse("19 06 2014 16:00"), "D3"));
                matches.add(new Match(GROUP_D.teams.get(3), GROUP_D.teams.get(1), Config.dateFormat.parse("20 06 2014 13:00"), "D4"));
                matches.add(new Match(GROUP_D.teams.get(3), GROUP_D.teams.get(0), Config.dateFormat.parse("24 06 2014 13:00"), "D5"));
                matches.add(new Match(GROUP_D.teams.get(1), GROUP_D.teams.get(2), Config.dateFormat.parse("24 06 2014 13:00"), "D6"));


                matches.add(new Match(GROUP_E.teams.get(0), GROUP_E.teams.get(1), Config.dateFormat.parse("15 06 2014 13:00"), "E1"));
                matches.add(new Match(GROUP_E.teams.get(2), GROUP_E.teams.get(3), Config.dateFormat.parse("15 06 2014 16:00"), "E2"));
                matches.add(new Match(GROUP_E.teams.get(0), GROUP_E.teams.get(2), Config.dateFormat.parse("20 06 2014 16:00"), "E3"));
                matches.add(new Match(GROUP_E.teams.get(3), GROUP_E.teams.get(1), Config.dateFormat.parse("20 06 2014 19:00"), "E4"));
                matches.add(new Match(GROUP_E.teams.get(3), GROUP_E.teams.get(0), Config.dateFormat.parse("25 06 2014 17:00"), "E5"));
                matches.add(new Match(GROUP_E.teams.get(1), GROUP_E.teams.get(2), Config.dateFormat.parse("25 06 2014 17:00"), "E6"));


                matches.add(new Match(GROUP_F.teams.get(0), GROUP_F.teams.get(1), Config.dateFormat.parse("15 06 2014 19:00"), "F1"));
                matches.add(new Match(GROUP_F.teams.get(2), GROUP_F.teams.get(3), Config.dateFormat.parse("16 06 2014 16:00"), "F2"));
                matches.add(new Match(GROUP_F.teams.get(0), GROUP_F.teams.get(2), Config.dateFormat.parse("21 06 2014 13:00"), "F3"));
                matches.add(new Match(GROUP_F.teams.get(3), GROUP_F.teams.get(1), Config.dateFormat.parse("21 06 2014 19:00"), "F4"));
                matches.add(new Match(GROUP_F.teams.get(3), GROUP_F.teams.get(0), Config.dateFormat.parse("25 06 2014 13:00"), "F5"));
                matches.add(new Match(GROUP_F.teams.get(1), GROUP_F.teams.get(2), Config.dateFormat.parse("25 06 2014 13:00"), "F6"));


                matches.add(new Match(GROUP_G.teams.get(0), GROUP_G.teams.get(1), Config.dateFormat.parse("16 06 2014 13:00"), "G1"));
                matches.add(new Match(GROUP_G.teams.get(2), GROUP_G.teams.get(3), Config.dateFormat.parse("16 06 2014 19:00"), "G2"));
                matches.add(new Match(GROUP_G.teams.get(0), GROUP_G.teams.get(2), Config.dateFormat.parse("21 06 2014 16:00"), "G3"));
                matches.add(new Match(GROUP_G.teams.get(3), GROUP_G.teams.get(1), Config.dateFormat.parse("21 06 2014 19:00"), "G4"));
                matches.add(new Match(GROUP_G.teams.get(3), GROUP_G.teams.get(0), Config.dateFormat.parse("26 06 2014 13:00"), "G5"));
                matches.add(new Match(GROUP_G.teams.get(1), GROUP_G.teams.get(2), Config.dateFormat.parse("26 06 2014 13:00"), "G6"));

                matches.add(new Match(GROUP_H.teams.get(0), GROUP_H.teams.get(1), Config.dateFormat.parse("17 06 2014 13:00"), "H1"));
                matches.add(new Match(GROUP_H.teams.get(2), GROUP_H.teams.get(3), Config.dateFormat.parse("17 06 2014 19:00"), "H2"));
                matches.add(new Match(GROUP_H.teams.get(0), GROUP_H.teams.get(2), Config.dateFormat.parse("22 06 2014 13:00"), "H3"));
                matches.add(new Match(GROUP_H.teams.get(3), GROUP_H.teams.get(1), Config.dateFormat.parse("23 06 2014 16:00"), "H4"));
                matches.add(new Match(GROUP_H.teams.get(3), GROUP_H.teams.get(0), Config.dateFormat.parse("26 06 2014 17:00"), "H5"));
                matches.add(new Match(GROUP_H.teams.get(1), GROUP_H.teams.get(2), Config.dateFormat.parse("26 06 2014 17:00"), "H6"));

                matchRepository.store(matches);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalStateException("Incorrect configuration, failed to parse date");
            }
        }
    }

    private static void createPlayoffMatches(MatchRepository matchRepository) {
        if (matchRepository.read("LS0").id.isEmpty()) {
            List<Match> matches = new ArrayList<>();
            try {
                int index = 0;
                matches.add(new Match("{A1}", "{B2}", Config.dateFormat.parse("28 06 2014 18:00"), "LS" + (index++), Match.Stage.LAST_SIXTEEN));
                matches.add(new Match("{C1}", "{D2}", Config.dateFormat.parse("28 06 2014 22:00"), "LS" + (index++), Match.Stage.LAST_SIXTEEN));

                matches.add(new Match("{B1}", "{A2}", Config.dateFormat.parse("29 06 2014 18:00"), "LS" + (index++), Match.Stage.LAST_SIXTEEN));
                matches.add(new Match("{D1}", "{C2}", Config.dateFormat.parse("29 06 2014 22:00"), "LS" + (index++), Match.Stage.LAST_SIXTEEN));

                matches.add(new Match("{E1}", "{F2}", Config.dateFormat.parse("30 06 2014 18:00"), "LS" + (index++), Match.Stage.LAST_SIXTEEN));
                matches.add(new Match("{G1}", "{H2}", Config.dateFormat.parse("30 06 2014 22:00"), "LS" + (index++), Match.Stage.LAST_SIXTEEN));

                matches.add(new Match("{F1}", "{E2}", Config.dateFormat.parse("01 07 2014 18:00"), "LS" + (index++), Match.Stage.LAST_SIXTEEN));
                matches.add(new Match("{H1}", "{G2}", Config.dateFormat.parse("01 07 2014 22:00"), "LS" + (index), Match.Stage.LAST_SIXTEEN));

                matchRepository.store(matches);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalStateException("Incorrect configuration, failed to parse date");
            }
        }

        if (matchRepository.read("QF1").id.isEmpty()) {
            List<Match> matches = new ArrayList<>();
            try {
                int index = 0;
                matches.add(new Match("{LS1}", "{LS2}", Config.dateFormat.parse("04 07 2014 22:00"), "QF" + (index++), Match.Stage.QUARTER_FINAL));
                matches.add(new Match("{LS5}", "{LS6}", Config.dateFormat.parse("04 07 2014 18:00"), "QF" + (index++), Match.Stage.QUARTER_FINAL));

                matches.add(new Match("{LS3}", "{LS4}", Config.dateFormat.parse("05 07 2014 22:00"), "QF" + (index++), Match.Stage.QUARTER_FINAL));
                matches.add(new Match("{LS7}", "{LS8}", Config.dateFormat.parse("05 07 2014 18:00"), "QF" + (index), Match.Stage.QUARTER_FINAL));

                matchRepository.store(matches);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalStateException("Incorrect configuration, failed to parse date");
            }
        }

        if (matchRepository.read("SF1").id.isEmpty()) {
            List<Match> matches = new ArrayList<>();
            try {
                int index = 0;
                matches.add(new Match("{QF1}", "{QF2}", Config.dateFormat.parse("08 07 2014 22:00"), "SF" + (index++), Match.Stage.SEMI_FINAL));
                matches.add(new Match("{QF3}", "{QF4}", Config.dateFormat.parse("09 07 2014 22:00"), "SF" + (index), Match.Stage.SEMI_FINAL));

                matches.add(new Match("{SF1}", "{SF2}", Config.dateFormat.parse("13 07 2014 21:00"), "FINAL", Match.Stage.FINAL));

                matchRepository.store(matches);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalStateException("Incorrect configuration, failed to parse date");
            }
        }
    }
}
