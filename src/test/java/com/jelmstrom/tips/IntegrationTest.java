package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.NeoMatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.notification.EmailNotification;
import com.jelmstrom.tips.table.NeoTablePredictionRepository;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TablePredictionRepository;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class IntegrationTest {


    private MatchRepository matchRepo = new NeoMatchRepository("testRepo");
    private TablePredictionRepository tableRepo = new NeoTablePredictionRepository("testRepo");
    private GroupRepository groupRepo = new NeoGroupRepository("testRepo");
    private UserRepository userRepo = new NeoUserRepository("userRepo");

    @After
    public void tearDown() {
        matchRepo.dropAll();
        tableRepo.dropAll();
        groupRepo.dropAll();
        userRepo.dropAll();
    }

    @Test
    public void matchStoredInDb() {
        User user = new User("test", "test", false, "test");
        User user2 = new User("test", "test", false, "test");
        user = userRepo.store(user);
        user2 = userRepo.store(user2);
        Group group = groupRepo.store(new Group("a", Collections.emptyList()));
        Match match = new Match("TeamA", "TeamB", new Date(), group.getGroupId());
        match = matchRepo.store(match);
        new Result(match, 2, 1, user.id);
        new Result(match, 2, 2, user2.id);
        matchRepo.store(match);
        Match persisted = matchRepo.read(match.getId());
        assertThat(persisted.equals(match), is(true));
    }


    @Test
    public void getAllMatchesShouldReturnListOfMatchesGreaterThanOne() {
        User user = new User("_display_", "_mail_", false, "_token");
        user = userRepo.store(user);
        Group group = groupRepo.store(new Group("a", Collections.emptyList()));
        Match match = new Match("TeamA", "TeamB", new Date(), group.getGroupId());
        match = matchRepo.store(match);
        new Result(match, 2, 2, user.id);
        new Result(match, 2, 2, user.id);
        Match match2 = new Match("TeamA", "TeamB", new Date(), group.getGroupId());
        new Result(match, 2, 1, user.id);
        new Result(match, 2, 2, user.id);
        matchRepo.store(match);
        matchRepo.store(match2);
        List<Match> persisted = matchRepo.read();
        assertThat(persisted.size() > 1, is(true));
    }


    @Test
    public void saveExistingMatchUpdates() {
        User user = new User("test", "test", false, "test");
        User user2 = new User("test", "test", false, "test");
        user = userRepo.store(user);
        user2 = userRepo.store(user2);
        Group group = groupRepo.store(new Group("a", Collections.emptyList()));
        Match match = new Match("TeamA", "TeamB", new Date(), group.getGroupId());
        match = matchRepo.store(match);
        new Result(match, 2, 1, user.id);
        matchRepo.store(match);

        Match versionOne = matchRepo.read(match.getId());
        new Result(match, 2, 2, user2.id);
        matchRepo.store(match);

        Match versionTwo = matchRepo.read(match.getId());

        assertThat(versionTwo.equals(match), is(true));
        assertThat(versionTwo.equals(versionOne), is(false));
    }

    @Test
    public void tablePredictionsAreStoredAndReadInCorrectOrder() {

        User user = new User("test", "test", false, "test");
        user = userRepo.store(user);
        List<String> teams = Arrays.asList("teamB", "teamA", "teamC", "teamD");
        Group group = groupRepo.store(new Group("a", teams));
        TablePrediction prediction = new TablePrediction(group.getGroupId(), user.id, teams);
        tableRepo.store(prediction);
        TablePrediction stored = tableRepo.readPrediction(prediction.userId, prediction.group);
        assertThat(stored, equalTo(prediction));

    }


    @Test
    public void groupRepositoryShouldStoreGroup() {
        Group group = new Group("A", Arrays.asList("a", "b", "c", "d"));
        groupRepo.store(group);
        assertThat(groupRepo.read(group.getGroupId()), is(equalTo(group)));
    }

    @Test
    public void addUser() {
        User newUser = new User("display", "Email", false, "");
        userRepo.store(newUser);
        User readUser = userRepo.findByEmail("Email");
        assertThat(readUser.isValid(), is(true));
        assertThat(readUser.id, is(greaterThan(0L)));
    }


    @Test
    public void updateUser() {
        User newUser = new User("display", "Email", false, "");
        userRepo.store(newUser);
        newUser = userRepo.findByDisplayName("display");
        User updated = new User(newUser.id, "updated", "updated", true, "updated");
        userRepo.store(updated);
        assertThat(userRepo.findByDisplayName("display").isValid(), is(false));
        assertThat(userRepo.findByDisplayName("updated"), is(updated));
    }

    @Test
    public void findUserByToken() {
        String token_uuid__ = "__token_uuid__";
        User newUser = new User("display", "Email", false, token_uuid__);
        userRepo.store(newUser);
        User read = userRepo.findByToken(token_uuid__);
        assertThat(read.email, is(equalTo(newUser.email)));
    }


    @Test
    public void removeUser() {
        User newUser = new User("display", "Email", false, "");
        User storedUser = userRepo.store(newUser);
        assertThat(userRepo.findByEmail("Email").isValid(), is(true));
        userRepo.remove(storedUser.id);
        assertThat(userRepo.findByEmail("Email").isValid(), is(false));
    }

    @Ignore
    @Test
    public void sendMailDoesNotFail() {
        new EmailNotification()
                .sendMail(new User("...userName...", "chrilles.vmtips@gmail.com", false, ""));
    }
}
