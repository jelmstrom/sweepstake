package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.EmailNotification;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class IntegrationTest {


    private MatchRepository matchRepo = new MatchRepository("testRepo");
    private TableRepository tableRepo = new TableRepository("testRepo");
    private GroupRepository groupRepo = new GroupRepository("testRepo");
    private UserRepository userRepo = new UserRepository("userRepo");

    @After
    public void tearDown(){
        matchRepo.matchCollection.drop();
        tableRepo.tablePredictionCollection.drop();
        groupRepo.groupCollection.drop();
        userRepo.userCollection.drop();
    }

    @Test
    public void matchStoredInDb(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        new Result(match, 2, 2, "Christian");
        matchRepo.store(match);
        Match persisted = matchRepo.read(match.id);
        assertThat(persisted.equals(match), is(true));
    }


    @Test
    public void getAllMatchesShouldReturnListOfMatchesGreaterThanOne(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 2, "Johan");
        new Result(match, 2, 2, "Christian");
        Match match2 = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        new Result(match, 2, 2, "Christian");
        matchRepo.store(match);
        matchRepo.store(match2);
        List<Match> persisted = matchRepo.read();
        assertThat(persisted.size() > 1, is(true));
    }


    @Test
    public void saveExistingMatchUpdates(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        matchRepo.store(match);

        Match versionOne = matchRepo.read(match.id);
        new Result(match, 2, 2, "Christian");
        matchRepo.store(match);

        Match versionTwo = matchRepo.read(match.id);

        assertThat(versionTwo.equals(match), is(true));
        assertThat(versionTwo.equals(versionOne), is(false));
    }

    @Test
    public void tablePredictionsAreStoredAndReadInCorrectOrder(){
        TablePrediction prediction = new TablePrediction("user", "grp", Arrays.asList("teamB", "teamA", "teamC", "teamD"));
        tableRepo.store(prediction);
        TablePrediction stored = tableRepo.readPrediction(prediction.user, prediction.group);
        assertThat(stored, equalTo(prediction));

    }


    @Test
    public void groupRepositoryShouldStoreGroup(){
        Group group = new Group("A", Arrays.asList("a", "b","c", "d"));
        groupRepo.store(group);
        assertThat(groupRepo.read("A"), is(equalTo(group)));
    }

    @Test
    public void addUser(){
        User newUser = new User("display", "Email", false, "");
        userRepo.store(newUser);
        User readUser = userRepo.findByEmail("Email");
        assertThat(readUser.isValid(), is(true));
        assertThat(readUser.id.length(), is(greaterThan(0)));
    }


    @Test
    public void updateUser(){
        User newUser = new User("display", "Email", false, "");
        userRepo.store(newUser);
        newUser = userRepo.findByDisplayName("display");
        User updated = new User(newUser.id, "updated", "updated", true, "updated");
        userRepo.store(updated);
        assertThat(userRepo.findByDisplayName("display").isValid(), is(false));
        assertThat(userRepo.findByDisplayName("updated"), is(updated));
    }

    @Test
    public void findUserByToken(){
        String token_uuid__ = "__token_uuid__";
        User newUser = new User("display", "Email", false, token_uuid__);
        userRepo.store(newUser);
        User read = userRepo.findByToken(token_uuid__);
        assertThat(read.email, is(equalTo(newUser.email)));
    }


    @Test
         public void removeUser(){
        User newUser = new User("display", "Email", false, "");
        userRepo.store(newUser);
        assertThat(userRepo.findByEmail("Email").isValid(), is(true));
        assertThat(userRepo.findByEmail("Email").isValid(), is(true));
        userRepo.remove("Email");
        assertThat(userRepo.findByEmail("Email").isValid(), is(false));
    }

    @Ignore
    @Test
    public void sendMailDoesNotFail(){
        new EmailNotification(new User("", "admin.user@gmail.com", false, ""))
                .sendMail(new User("...userName...", "johan.elmstrom@gmail.com", false, ""));
    }
}
