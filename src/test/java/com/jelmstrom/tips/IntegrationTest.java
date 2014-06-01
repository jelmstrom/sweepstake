package com.jelmstrom.tips;

import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IntegrationTest {


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
}
