package com.jelmstrom.tips.match;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.jelmstrom.tips.match.Match.Stage.GROUP;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MongoMatchRepositoryTest {

    private MatchRepository matchRepository = new MongoMatchRepository("test");
    private Match match;

    @Before
    public void before(){
        match = new Match("home", "away", new Date(), "a1", GROUP);
        matchRepository.store(match);
    }

    @After
    public void after(){
        matchRepository.dropAll();
    }

    @Test
    public void createGroupShouldStoreGroup(){
        Match persisted = matchRepository.read(match.id);
        assertThat(match.getNodeId(), is(equalTo(persisted.getNodeId())));
        assertThat(match, is(equalTo(persisted)));
    }

    @Test
    public void getGroupsShouldReturnListOfOne(){
        List<Match> matches = matchRepository.read();
        assertThat(matches.size(), is(1));
    }

    @Test
    public void updateNodeShouldUpdateExistingNode(){
        Match m2 = new Match("home2", "away2", new Date(), "a1", GROUP);
        m2.setNodeId(match.getNodeId());
        matchRepository.store(m2);
        List<Match> matches = matchRepository.read();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0).getNodeId(), is(equalTo(match.getNodeId())));
    }

}
