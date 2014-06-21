package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TableEntry;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.jelmstrom.tips.match.Match.Stage.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AcceptanceTest {

    public static final String ADMIN_EMAIL = "none@noreply.zzz";
    public static final String USER_EMAIL = "Johan";
    public static final String TEST_REPO = "testRepo";
    private String USER_ID;
    public static final UserRepository USER_REPOSITORY = new UserRepository(TEST_REPO);
    public static final MatchRepository MATCH_REPOSITORY = new MatchRepository(TEST_REPO);
    private static final TableRepository TABLE_REPOSITORY = new TableRepository(TEST_REPO);
    private static final GroupRepository GROUP_REPOSITORY = new GroupRepository(TEST_REPO);
    private final String brazil = "Brazil";
    private final String germany = "Germany";
    private final String australia = "Australia";
    private final String argentina = "Argentina";
    private Group groupA = new Group("GroupA",asList(brazil, germany, argentina, australia));

    private Sweepstake sweepstake = new Sweepstake(TEST_REPO);
    private String ADMIN_ID;
    private Match matchA1;
    private Date matchStart = new Date();
    private User user;
    private User playoffUser;
    private Result resultA1;

    @Before
    public void setup(){
        GROUP_REPOSITORY.store(groupA);
        ADMIN_ID = USER_REPOSITORY.store(new User("adminöö", ADMIN_EMAIL, true, "")).id;
        user = USER_REPOSITORY.store(new User("useråö", USER_EMAIL, false, "3213ou1+297319u"));
        playoffUser = USER_REPOSITORY.store(new User("playoff", "aaaaaaa", false, "1231243179287"));

        USER_ID = user.id;


        List<Match> matches = new ArrayList<>();
        matchA1 = new Match(brazil, germany, matchStart, "A1");
        matches.add(matchA1);
        matches.add(new Match(brazil, argentina, matchStart, "A2"));
        matches.add(new Match(brazil, australia, matchStart, "A3"));
        matches.add(new Match(germany, australia, matchStart, "A4"));
        matches.add(new Match(germany, argentina, matchStart, "A5"));
        matches.add(new Match(australia, argentina, matchStart, "A6"));

        matches.add(new Match(australia, germany, matchStart, "LS1", Match.Stage.LAST_SIXTEEN));
        matches.add(new Match(australia, germany, matchStart, "QF1", Match.Stage.QUARTER_FINAL));
        matches.add(new Match(australia, germany, matchStart, "SF1", Match.Stage.SEMI_FINAL));
        matches.add(new Match(australia, germany, matchStart, "F", Match.Stage.FINAL));

        resultA1 = new Result(matches.get(0), 2, 0, USER_ID);
        new Result(matches.get(1), 2, 0, USER_ID);
        new Result(matches.get(2), 2, 0, USER_ID);

        new Result(matches.get(3), 2, 0, USER_ID);
        new Result(matches.get(4), 0, 0, USER_ID);
        new Result(matches.get(5), 1, 5, USER_ID);

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
       MATCH_REPOSITORY.matchCollection.drop();
       GROUP_REPOSITORY.groupCollection.drop();
       TABLE_REPOSITORY.tablePredictionCollection.drop();
        USER_REPOSITORY.userCollection.drop();
    }


    @Test
    public void currentStandingsForGroupAShouldBeBrazilArgentinaAustraliaGermany(){

        List<TableEntry> table = sweepstake.currentStandingsForGroup(groupA.groupName);

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
    public void pointsForPLayoffUserShouldBe47(){
        int points = pointsForUser(playoffUser);
        assertThat(points, is(47));
    }

    @Test
    public void groupScoreForCompletelyCorrectTableShouldBeSeven(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction(groupA.groupName, USER_ID, userPrediction);
        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(7+9));
    }



    @Test
    public void groupScoreForFirstTwoTeamsIsFourIfCompletelyCorrect(){

        List<String> userPrediction = asList(brazil, argentina, germany, australia);
        TablePrediction prediction = new TablePrediction(groupA.groupName, USER_ID,userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(4+9));

    }

    @Test
    public void groupScoreForFirstTwoTeamsIsTwoIfOrderedIncorrectlyCorrect(){

        List<String> userPrediction = asList( argentina,brazil, germany, australia);
        TablePrediction prediction = new TablePrediction(groupA.groupName, USER_ID,userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(2+9));

    }

    @Test
    public void groupScoreAddsZeroZeroIfCompletelyWrong(){

        List<String> userPrediction = asList( germany, australia, argentina,brazil);
        TablePrediction prediction = new TablePrediction(groupA.groupName, USER_ID, userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(9));

    }


    @Test
    public void totalScoreForGamesAndTableShouldBeSixteen(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction(groupA.groupName, USER_ID, userPrediction);
        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(16));
    }

    @Test
    public void totalScoreCalculationHandlesEmptyActualGroupPositions(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction(groupA.groupName, USER_ID,userPrediction);
        TABLE_REPOSITORY.store(prediction);
        List<String> userPrediction2 = asList("4", "3", "2", "1");

        prediction = new TablePrediction("GroupB", "", userPrediction2);
        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser(user);
        assertThat(points, is(16));
    }

    @Test
    public void updateResultreplacesExistingValue(){
        int originalCount = matchA1.results.size();
        Result result = new Result(matchA1, 2, 2, USER_ID);
        List<Result> results = Arrays.asList(result);

        sweepstake.saveResults(results, user);
        Match fromDb = MATCH_REPOSITORY.read(matchA1.id);
        assertThat(fromDb.results.size(), is(originalCount));
        assertThat(fromDb.resultFor(USER_ID).awayGoals, is(2));
    }

    public int pointsForUser(User user) {
        return sweepstake.fasterLeaderboard().stream().filter(entry -> entry.user.id.equals(user.id)).findFirst().get().points;
    }

}
