package com.jelmstrom.tips;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.MatchRepository;
import com.jelmstrom.tips.match.Result;
import com.jelmstrom.tips.table.TablePrediction;
import com.jelmstrom.tips.table.TableRepository;
import org.junit.After;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class IntegrationTest {


    @After
    public void tearDown(){
        MatchRepository.matchCollection.drop();
        TableRepository.tablePredictionCollection.drop();
        GroupRepository.groupCollection.drop();
    }

    @Test
    public void matchStoredInDb(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        new Result(match, 2, 2, "Christian");
        com.jelmstrom.tips.match.MatchRepository.store(match);
        Match persisted = com.jelmstrom.tips.match.MatchRepository.read(match.id);
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
        com.jelmstrom.tips.match.MatchRepository.store(match);
        com.jelmstrom.tips.match.MatchRepository.store(match2);
        List<Match> persisted = com.jelmstrom.tips.match.MatchRepository.getAll();
        assertThat(persisted.size() > 1, is(true));
    }


    @Test
    public void saveExistingMatchUpdates(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        com.jelmstrom.tips.match.MatchRepository.store(match);

        Match versionOne = com.jelmstrom.tips.match.MatchRepository.read(match.id);
        new Result(match, 2, 2, "Christian");
        com.jelmstrom.tips.match.MatchRepository.store(match);

        Match versionTwo = com.jelmstrom.tips.match.MatchRepository.read(match.id);

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
}
