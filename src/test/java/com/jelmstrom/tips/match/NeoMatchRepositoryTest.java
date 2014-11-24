package com.jelmstrom.tips.match;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.jelmstrom.tips.match.Match.Stage.GROUP;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NeoMatchRepositoryTest {

    private NeoMatchRepository neoMatchRepository = new NeoMatchRepository("test");
    private GroupRepository groupRepository = new NeoGroupRepository("");
    private Match match;

    @Before
    public void before(){
        Group group = groupRepository.store(new Group("A", Collections.emptyList()));
        match = new Match("home", "away", new Date(), GROUP, group.getGroupId());
        neoMatchRepository.store(match);
    }

    @After
    public void after(){
        neoMatchRepository.dropAll();
        groupRepository.dropAll();
    }

    @Test
    public void createGroupShouldStoreGroup(){
        Match persisted = neoMatchRepository.read(match.getMatchId());
        assertThat(match.getMatchId(), is(equalTo(persisted.getMatchId())));
        assertThat(match, is(equalTo(persisted)));
    }

    @Test
    public void getGroupsShouldReturnListOfOne(){
        List<Match> matches = neoMatchRepository.read();
        assertThat(matches.size(), is(1));
    }

    @Test
    public void updateNodeShouldUpdateExistingNode(){
        Group group = groupRepository.store(new Group("A", Collections.emptyList()));
        Match m2 = new Match("home2", "away2", new Date(), GROUP, group.getGroupId());
        m2.setMatchId(match.getMatchId());
        neoMatchRepository.store(m2);
        List<Match> matches = neoMatchRepository.read();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0).getMatchId(), is(equalTo(match.getMatchId())));
    }

    @Test
    public void matchPredictionsShouldFindTwoResultsForUser(){
        Group group = groupRepository.store(new Group("A", Collections.emptyList()));
        Match m2 = new Match("home2", "away2", new Date(), GROUP, group.getGroupId());
        m2 = neoMatchRepository.store(m2);
        User user = new NeoUserRepository("").store(new User("111", "111", false, "111"));
        Result result = new Result(m2, 2, 2, user.id);
        m2.add(result);
        new Result(match, 1,1, user.id);
        neoMatchRepository.store(m2);
        neoMatchRepository.store(match);
        List<Result> results = neoMatchRepository.userPredictions(user.id);
        assertThat(results.size(), is(2));

    }

}
