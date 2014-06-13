package com.jelmstrom.tips;

import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class ConfigurationLoaderTest{

    private MatchRepository matches;
    private TableRepository tables;

    @Before
    public void setUp(){
        matches = new MatchRepository("matches");
    }
    @After
    public void tearDown(){
        matches.matchCollection.drop();
        tables = new TableRepository("matches");
        tables.tablePredictionCollection.drop();
        new GroupRepository("matches").groupCollection.drop();
        new UserRepository("matches").userCollection.drop();
    }

    @Test
    public void dataShouldBeInitialised(){
        new ConfigurationLoader().initialiseData("matches");
        assertThat(matches.read("A1"), is(notNullValue()));

    }
}
