package com.jelmstrom.tips;


import com.jelmstrom.tips.match.Match;
import com.jelmstrom.tips.match.NeoMatchRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.jelmstrom.tips.match.Match.Stage.GROUP;
import static com.jelmstrom.tips.persistence.NeoRepository.MATCH_LABEL;
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
        neoMatchRepository.dropAll(MATCH_LABEL);
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

}
