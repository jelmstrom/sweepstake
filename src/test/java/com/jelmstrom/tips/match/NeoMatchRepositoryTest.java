package com.jelmstrom.tips.match;

import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.jelmstrom.tips.match.Match.Stage.GROUP;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NeoMatchRepositoryTest {

    private NeoMatchRepository neoMatchRepository = new NeoMatchRepository("test");
    private Match match;

    @Before
    public void before(){
        match = new Match("home", "away", new Date(), "a1", GROUP);
        neoMatchRepository.store(match);
    }

    @After
    public void after(){
        neoMatchRepository.dropAll();
    }

    @Test
    public void createGroupShouldStoreGroup(){
        Match persisted = neoMatchRepository.read(match.id);
        assertThat(match.getNodeId(), is(equalTo(persisted.getNodeId())));
        assertThat(match, is(equalTo(persisted)));
    }

    @Test
    public void getGroupsShouldReturnListOfOne(){
        List<Match> matches = neoMatchRepository.read();
        assertThat(matches.size(), is(1));
    }

    @Test
    public void updateNodeShouldUpdateExistingNode(){
        Match m2 = new Match("home2", "away2", new Date(), "a1", GROUP);
        m2.setNodeId(match.getNodeId());
        neoMatchRepository.store(m2);
        List<Match> matches = neoMatchRepository.read();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0).getNodeId(), is(equalTo(match.getNodeId())));
    }

    @Test
    public void matchPredictionsShouldFindTwoResultsForUser(){
        Match m2 = new Match("home2", "away2", new Date(), "a1", GROUP);
        User user = new NeoUserRepository("").store(new User("111", "111", false, "111"));
        Result result = new Result(m2, 2, 2, user.id);
        m2.add(result);
        Result result2 = new Result(match, 1,1, user.id);
        neoMatchRepository.store(m2);
        neoMatchRepository.store(match);
        List<Result> results = neoMatchRepository.userPredictions(user.id);
        assertThat(results.size(), is(2));

    }

}
