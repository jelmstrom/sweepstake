package com.jelmstrom.tips;

import com.jelmstrom.tips.configuration.ConfigurationLoader;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class ConfigurationLoaderTest{

    private MatchRepository matches;
    private TableRepository tables;

    @Before
    public void setUp(){
        matches = new MatchRepository("matches");
        tables = new TableRepository("matches");
    }

    @After
    public void tearDown(){
        matches.matchCollection.drop();
        tables.tablePredictionCollection.drop();
        new GroupRepository("matches").groupCollection.drop();
        new UserRepository("matches").userCollection.drop();
    }

    @Test
    public void dataShouldBeInitialised(){
        ConfigurationLoader.initialiseData("matches");
        assertThat(matches.read("A1"), is(notNullValue()));

    }

    @Test
    public void configurationLoaderShouldPatchUsers(){
        ConfigurationLoader.initialiseData("matches");
        String id = new UserRepository("matches").store(new User("display", "test", false, "")).id;
        String id2 = new UserRepository("matches").store(new User("display2", "test2", false, "")).id;
        Match m = matches.read("A1");
        new Result(m, 1, 1, id);
        new Result(m, 1, 1, id2);
        matches.store(m);
        ConfigurationLoader.initialiseData("matches");
        assertThat(matches.read("A1").resultFor(id).userId, is(notNullValue()));
        assertThat(matches.read("A1").resultFor(id2).userId, is(notNullValue()));

    }


    @Test
    public void configurationLoaderShouldPatchPredictions(){
        ConfigurationLoader.initialiseData("matches");
        tables.store(new TablePrediction("A", "", Arrays.asList("one", "two")));
        tables.store(new TablePrediction("B","", Arrays.asList("one", "two")));
        new UserRepository("matches").store(new User("display2", "test2", false, ""));
        new UserRepository("matches").store(new User("display", "test", false, ""));
        ConfigurationLoader.initialiseData("matches");
        assertThat(tables.readPrediction("test", "A").userId, is(notNullValue()));
        assertThat(tables.readPrediction("test", "A").userId, is(notNullValue()));

    }
}
