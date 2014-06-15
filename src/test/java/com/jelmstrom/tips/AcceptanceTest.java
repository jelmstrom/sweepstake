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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AcceptanceTest {

    public static final String ADMIN_EMAIL = "none@noreply.zzz";
    public static final String USER_EMAIL = "Johan";
    public static final String TEST_REPO = "testRepo";
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

    @Before
    public void setup(){
        GROUP_REPOSITORY.store(groupA);
        List<Match> matches = new ArrayList<>();
        Date matchStart = new Date();
        matches.add(new Match(brazil, germany, matchStart, "A1"));
        matches.add(new Match(brazil, argentina, matchStart, "A2"));
        matches.add(new Match(brazil, australia, matchStart, "A3"));
        matches.add(new Match(germany, australia, matchStart, "A4"));
        matches.add(new Match(germany, argentina, matchStart, "A5"));
        matches.add(new Match(australia, argentina, matchStart, "A6"));

        new Result(matches.get(0), 2, 0, USER_EMAIL);
        new Result(matches.get(1), 2, 0, USER_EMAIL);
        new Result(matches.get(2), 2, 0, USER_EMAIL);

        new Result(matches.get(3), 2, 0, USER_EMAIL);
        new Result(matches.get(4), 0, 0, USER_EMAIL);
        new Result(matches.get(5), 1, 5, USER_EMAIL);



        new Result(matches.get(0), 2, 1, ADMIN_EMAIL);
        new Result(matches.get(1), 2, 0, ADMIN_EMAIL);
        new Result(matches.get(2), 0, 0, ADMIN_EMAIL);

        new Result(matches.get(3), 1, 3, ADMIN_EMAIL);
        new Result(matches.get(4), 0, 1, ADMIN_EMAIL);

        new Result(matches.get(5), 1, 2, ADMIN_EMAIL);


        USER_REPOSITORY.store(new User("admin", ADMIN_EMAIL, true, ""));
        USER_REPOSITORY.store(new User("user", USER_EMAIL, false, ""));

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
    public void pointsForUserShouldBeCalculatedBasedOnAdminResults(){
        int points = pointsForUser();
        assertThat(points, is(9));
    }

    @Test
    public void groupScoreForCompletelyCorrectTableShouldBeSeven(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction(USER_EMAIL, groupA.groupName, userPrediction);
        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser();
        assertThat(points, is(7+9));
    }

    @Test
    public void groupScoreForFirstTwoTeamsIsFourIfCompletelyCorrect(){

        List<String> userPrediction = asList(brazil, argentina, germany, australia);
        TablePrediction prediction = new TablePrediction(USER_EMAIL, groupA.groupName, userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser();
        assertThat(points, is(4+9));

    }

    @Test
    public void groupScoreForFirstTwoTeamsIsTwoIfOrderedIncorrectlyCorrect(){

        List<String> userPrediction = asList( argentina,brazil, germany, australia);
        TablePrediction prediction = new TablePrediction(USER_EMAIL, groupA.groupName, userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser();
        assertThat(points, is(2+9));

    }

    @Test
    public void groupScoreAddsZeroZeroIfCompletelyWrong(){

        List<String> userPrediction = asList( germany, australia, argentina,brazil);
        TablePrediction prediction = new TablePrediction(USER_EMAIL, groupA.groupName, userPrediction);

        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser();
        assertThat(points, is(9));

    }


    @Test
    public void totalScoreForGamesAndTableShouldBeSixteen(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction(USER_EMAIL, groupA.groupName, userPrediction);
        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser();
        assertThat(points, is(16));
    }

    @Test
    public void totalScoreCalculationHandlesEmptyActualGroupPositions(){

        List<String> userPrediction = asList(brazil, argentina, australia, germany);
        TablePrediction prediction = new TablePrediction(USER_EMAIL, groupA.groupName, userPrediction);
        TABLE_REPOSITORY.store(prediction);
        List<String> userPrediction2 = asList("4", "3", "2", "1");

        prediction = new TablePrediction(USER_EMAIL, "GroupB", userPrediction2);
        TABLE_REPOSITORY.store(prediction);
        int points = pointsForUser();
        assertThat(points, is(16));
    }

    public int pointsForUser() {
        return sweepstake.fasterLeaderboard().stream().filter(entry -> entry.user.equals(USER_EMAIL)).findFirst().get().points;
    }
}
