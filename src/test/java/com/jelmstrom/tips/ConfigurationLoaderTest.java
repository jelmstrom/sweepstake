package com.jelmstrom.tips;

import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.configuration.ConfigurationLoader;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.table.TableRepository;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;

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
    public void dataShouldBeInitialised() throws ParseException {
        ConfigurationLoader.initialiseData("matches");
        assertThat(matches.read("A1"), is(notNullValue()));

    }

    @Test
    public void dateShouldBeMovedBack() throws ParseException {
        ConfigurationLoader.initialiseData("matches");
        assertThat(matches.read("BRONZE").matchStart, is(Config.dateFormat.parse("12 07 2014 20:00")));
    }
}
