package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Test;

import java.util.*;

import static com.jelmstrom.tips.match.MatchRepository.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class IntegrationTest {


    @After
    public void tearDown(){
       MatchRepository.matchCollection.drop();
       TableRepository.tablePredictionCollection.drop();
       GroupRepository.groupCollection.drop();
       UserRepository.userCollection.drop();
    }

    @Test
    public void matchStoredInDb(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        new Result(match, 2, 2, "Christian");
        store(match);
        Match persisted = read(match.id);
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
        store(match);
        store(match2);
        List<Match> persisted = read();
        assertThat(persisted.size() > 1, is(true));
    }


    @Test
    public void saveExistingMatchUpdates(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        store(match);

        Match versionOne = read(match.id);
        new Result(match, 2, 2, "Christian");
        store(match);

        Match versionTwo = read(match.id);

        assertThat(versionTwo.equals(match), is(true));
        assertThat(versionTwo.equals(versionOne), is(false));
    }

    @Test
    public void tablePredictionsAreStoredAndReadInCorrectOrder(){
        TablePrediction prediction = new TablePrediction("user", "grp", Arrays.asList("teamB", "teamA", "teamC", "teamD"));
        TableRepository.store(prediction);
        TablePrediction stored = TableRepository.readPrediction(prediction.user, prediction.group);
        assertThat(stored, equalTo(prediction));

    }


    @Test
    public void groupRepositoryShouldStoreGroup(){
        Group group = new Group("A", Arrays.asList("a", "b","c", "d"));
        GroupRepository.store(group);
        assertThat(GroupRepository.read("A"), is(equalTo(group)));
    }

    @Test
    public void addUser(){
        User newUser = new User("display", "Email", "cred");
        UserRepository.store(newUser);
        assertThat(UserRepository.read("Email"), is(equalTo(newUser)));
    }

    @Test
    public void removeUser(){
        User newUser = new User("display", "Email", "cred");
        UserRepository.store(newUser);
        assertThat(UserRepository.read("Email"), is(equalTo(newUser)));
        UserRepository.remove("Email");
        assertThat(UserRepository.read("Email").displayName, is(""));
    }
}
