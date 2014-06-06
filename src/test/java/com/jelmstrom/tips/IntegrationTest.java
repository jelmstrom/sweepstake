package com.jelmstrom.tips;

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
        MatchRepository.tablePredictionCollection.drop();
    }

    @Test
    public void matchStoredInDb(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        new Result(match, 2, 2, "Christian");
        MatchRepository.store(match);
        Match persisted = MatchRepository.read(match.id);
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
        MatchRepository.store(match);
        MatchRepository.store(match2);
        List<Match> persisted = MatchRepository.getAll();
        assertThat(persisted.size() > 1, is(true));
    }


    @Test
    public void saveExistingMatchUpdates(){
        Match match = new Match("TeamA", "TeamB", new Date(), UUID.randomUUID().toString());
        new Result(match, 2, 1, "Johan");
        MatchRepository.store(match);

        Match versionOne = MatchRepository.read(match.id);
        new Result(match, 2, 2, "Christian");
        MatchRepository.store(match);

        Match versionTwo = MatchRepository.read(match.id);

        assertThat(versionTwo.equals(match), is(true));
        assertThat(versionTwo.equals(versionOne), is(false));
    }

    @Test
    public void tablePredictionsAreStoredAndReadInCorrectOrder(){
        TablePrediction prediction = new TablePrediction("user", "grp", Arrays.asList("teamB", "teamA", "teamC", "teamD"));
        MatchRepository.store(prediction);
        TablePrediction stored = MatchRepository.readPrediction(prediction.user, prediction.group);
        assertThat(stored, equalTo(prediction));

    }
}
