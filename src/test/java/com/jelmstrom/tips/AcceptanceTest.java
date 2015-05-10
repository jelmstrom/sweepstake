package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.*;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.*;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AcceptanceTest {

    public static final String ADMIN_EMAIL = "none@noreply.zzz";
    public static final String USER_EMAIL = "Johan";
    public static final String TEST_REPO = "testRepo";
    public static final UserRepository USER_REPOSITORY = new NeoUserRepository(TEST_REPO);
    public static final MatchRepository MATCH_REPOSITORY = new NeoMatchRepository(TEST_REPO);
    private static final TablePredictionRepository TABLE_REPOSITORY = new NeoTablePredictionRepository(TEST_REPO);
    private static final GroupRepository GROUP_REPOSITORY = new NeoGroupRepository(TEST_REPO);
    private final String brazil = "Brazil";
    private final String germany = "Germany";
    private final String australia = "Australia";
    private final String argentina = "Argentina";
    private Group groupA = new Group("GroupA",asList(brazil, germany, argentina, australia));

    private Sweepstake sweepstake = new Sweepstake(TEST_REPO);
    private Match matchA1;
    private Date matchStart = new Date();
    private User user;
    private User playoffUser;
    private User adminUser;

    @Before
    public void setup(){

        MATCH_REPOSITORY.dropAll();
        GROUP_REPOSITORY.dropAll();
        TABLE_REPOSITORY.dropAll();
        USER_REPOSITORY.dropAll();

        groupA = GROUP_REPOSITORY.store(groupA);
        adminUser = USER_REPOSITORY.store(new User("admin", ADMIN_EMAIL, true, ""));
        user = USER_REPOSITORY.store(new User("user", USER_EMAIL, false, "3213ou1+297319u"));
        playoffUser = USER_REPOSITORY.store(new User("playoff", "mail@null", false, "1231243179287"));
        Long ADMIN_ID = adminUser.id;


        List<Match> matches = new ArrayList<>();
        matchA1 = new Match(brazil, germany, matchStart, groupA.getGroupId());
        matchA1 = MATCH_REPOSITORY.store(matchA1);
        matches.add(matchA1);
        matches.add(MATCH_REPOSITORY.store(new Match(brazil, argentina, matchStart, groupA.getGroupId())));
        matches.add(MATCH_REPOSITORY.store(new Match(brazil, australia, matchStart, groupA.getGroupId())));
        matches.add(MATCH_REPOSITORY.store(new Match(germany, australia, matchStart, groupA.getGroupId())));
        matches.add(MATCH_REPOSITORY.store(new Match(germany, argentina, matchStart, groupA.getGroupId())));
        matches.add(MATCH_REPOSITORY.store(new Match(australia, argentina, matchStart, groupA.getGroupId())));

        matches.add(MATCH_REPOSITORY.store(new Match(australia, germany, matchStart, Match.Stage.LAST_SIXTEEN, groupA.getGroupId())));
        matches.add(MATCH_REPOSITORY.store(new Match(australia, germany, matchStart, Match.Stage.QUARTER_FINAL, groupA.getGroupId())));
        matches.add(MATCH_REPOSITORY.store(new Match(australia, germany, matchStart, Match.Stage.SEMI_FINAL, groupA.getGroupId())));
        matches.add(MATCH_REPOSITORY.store(new Match(australia, germany, matchStart, Match.Stage.FINAL, groupA.getGroupId())));

        new Result(matches.get(0), 2, 0, user.id);
        new Result(matches.get(1), 2, 0, user.id);
        new Result(matches.get(2), 2, 0, user.id);

        new Result(matches.get(3), 2, 0, user.id);
        new Result(matches.get(4), 0, 0, user.id);
        new Result(matches.get(5), 1, 5, user.id);

        new Result(matches.get(6), 1, 1, playoffUser.id, argentina); // 1 + 4 =  5
        new Result(matches.get(7), 1, 2, playoffUser.id, germany); // 1+1+8 = 10
        new Result(matches.get(8), 2, 0, playoffUser.id, argentina); //  = 0
        new Result(matches.get(9), 1, 5, playoffUser.id, argentina); // 0+0+32 = 32
                                                                       // => 47



        matches.get(0).setCorrectResult(new Result(matches.get(0), 2, 1, ADMIN_ID));
        matches.get(1).setCorrectResult(new Result(matches.get(1), 2, 0, ADMIN_ID));
        matches.get(2).setCorrectResult(new Result(matches.get(2), 0, 0, ADMIN_ID));
        matches.get(3).setCorrectResult(new Result(matches.get(3), 1, 3, ADMIN_ID));
        matches.get(4).setCorrectResult(new Result(matches.get(4), 0, 1, ADMIN_ID));
        matches.get(5).setCorrectResult(new Result(matches.get(5), 1, 2, ADMIN_ID));

        matches.get(6).setCorrectResult(new Result(matches.get(6), 2, 2, ADMIN_ID, argentina));
        matches.get(7).setCorrectResult(new Result(matches.get(7), 0, 2, ADMIN_ID, germany));
        matches.get(8).setCorrectResult(new Result(matches.get(8), 1, 2, ADMIN_ID, germany));
        matches.get(9).setCorrectResult(new Result(matches.get(9), 3, 1, ADMIN_ID, argentina));



        MATCH_REPOSITORY.store(matches);

    }

    @After
    public void tearDown(){
        MATCH_REPOSITORY.dropAll();
        GROUP_REPOSITORY.dropAll();
        TABLE_REPOSITORY.dropAll();
        USER_REPOSITORY.dropAll();
    }


    @Test
    public void currentStandingsForGroupAShouldBeBrazilArgentinaAustraliaGermany(){

        List<TableEntry> table = sweepstake.currentStandingsForGroup(groupA.getGroupId());
        assertThat(table.get(0).team, is(brazil));
        assertThat(table.get(1).team, is(argentina));
        assertThat(table.get(2).team, is(australia));
        assertThat(table.get(3).team, is(germany));

    }

    @Test
    public void pointsForUserShouldBeNine(){
        int points = pointsForUser(user);
        assertThat(points, is(9));
    }

    @Test
    public void pointsForPLayoffUserShouldBe25(){
        int points = pointsForUser(playoffUser);
        assertThat(points, is(25));
    }

    @Test
    public void pointsForPlayoffUserShouldBe35WithCorrectTopScorer(){

        adminUser.setTopScorer("someone");
        playoffUser.setTopScorer("someone");
        USER_REPOSITORY.store(adminUser);
        USER_REPOSITORY.store(playoffUser);

        int points = pointsForUser(playoffUser);
        assertThat(points, is(35));
    }

    @Test
    public void pointsForPlayoffUserShouldBe45WithCorrectTopScorerAndWinner(){

        adminUser.setTopScorer("scorer");
        adminUser.setWinner("winner");
        playoffUser.setTopScorer("scorer");
        playoffUser.setWinner("winner");

        USER_REPOSITORY.store(adminUser);
        USER_REPOSITORY.store(playoffUser);

        int points = pointsForUser(playoffUser);
        assertThat(points, is(45));
    }


    @Test
    public void groupScoreForCompletelyCorrectTableShouldBeSeven(){
        storePrediction(user, groupA);
        int points = pointsForUser(user);
        assertThat(points, is(7 + 9));
    }



    @Test
    public void groupScoreForFirstTwoTeamsIsFourIfCompletelyCorrect(){

        List<String> userPrediction = asList(brazil, argentina, germany, australia);
        TablePrediction prediction = new TablePrediction(groupA.getGroupId(), user.id,userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(4+9));

    }

    @Test
    public void groupScoreForFirstTwoTeamsIsTwoIfOrderedIncorrectlyCorrect(){

        List<String> userPrediction = asList( argentina,brazil, germany, australia);
        TablePrediction prediction = new TablePrediction(groupA.getGroupId(), user.id,userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(2+9));

    }

    @Test
    public void groupScoreAddsZeroZeroIfCompletelyWrong(){

        List<String> userPrediction = asList( germany, australia, argentina,brazil);
        TablePrediction prediction = new TablePrediction(groupA.getGroupId(), user.id, userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(9));

    }


    @Test
    public void totalScoreForGamesAndTableShouldBeSixteen(){

        storePrediction(user, groupA);
        int points = pointsForUser(user);
        assertThat(points, is(16));
    }



    @Test
    public void totalScoreCalculationHandlesEmptyActualGroupPositions(){

        storePrediction(user, groupA);
        TablePrediction prediction;
        List<String> userPrediction2 = asList("4", "3", "2", "1");
        Group groupB  = new Group("B", userPrediction2);
        GROUP_REPOSITORY.store(groupB);
        prediction = new TablePrediction(groupB.getGroupId(), user.id, userPrediction2);
        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(16));
    }

    public void storePrediction(User user, Group groupA) {
        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction(groupA.getGroupId(), user.id,userPrediction);
        TABLE_REPOSITORY.store(prediction);
    }

    @Test
    public void updateResultReplacesExistingValue(){
        int originalCount = matchA1.results.size();
        new Result(matchA1, 2, 2, user.id);
        List<Match> results = Arrays.asList(matchA1);

        MATCH_REPOSITORY.store(results);

        Match fromDb = MATCH_REPOSITORY.read(matchA1.getId());
        assertThat(fromDb.results.size(), is(originalCount));
        assertThat(fromDb.resultFor(user.id).awayGoals, is(2));
    }

    public int pointsForUser(User user) {
        return sweepstake.leaderboard().stream().filter(entry -> entry.user.id.equals(user.id)).findFirst().get().points;
    }

    @Test
    public void groupWithPredictionAndMatchesLoadsCorrectly(){
        storePrediction(user, groupA);
        List<Match> matches = MATCH_REPOSITORY.groupMatches(groupA.getGroupId());
        assertThat(matches.size(), is(10));


    }

}
