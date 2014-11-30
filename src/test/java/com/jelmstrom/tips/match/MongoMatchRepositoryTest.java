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
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MongoMatchRepositoryTest {

    private MatchRepository matchRepository = new MongoMatchRepository("test");
    private GroupRepository groupRepository = new NeoGroupRepository("");
    private Match match;
    private Group group;
    private User u;
    private NeoUserRepository userRepository;

    @Before
    public void before(){
        group = groupRepository.store(new Group("A", Collections.emptyList()));
        match = new Match("home", "away", new Date(), GROUP, group.getGroupId());
        match = matchRepository.store(match);
        userRepository = new NeoUserRepository("");
        u = userRepository.store(new User("a", "a", false, "a"));
    }

    @After
    public void after(){
        matchRepository.dropAll();
        groupRepository.dropAll();
        userRepository.dropAll();
    }

    @Test
    public void createMatchShouldStoreMatch(){
        Match persisted = matchRepository.read(match.getId());
        assertThat(match.getId(), is(equalTo(persisted.getId())));
        assertThat(match, is(equalTo(persisted)));
    }

    @Test
    public void readMatchesShouldReturnListOfOne(){
        List<Match> matches = matchRepository.read();
        assertThat(matches.size(), is(1));
    }

    @Test
    public void readMatchShouldReturnMatchWithResults(){
        new Result(match, 1, 1, u.id);
        matchRepository.store(match);
        Match matches = matchRepository.read(match.getId());
        assertThat(matches.results.size(), is(1));

    }

    @Test
    public void updateNodeShouldUpdateExistingNode(){
        Group group = groupRepository.store(new Group("A", Collections.emptyList()));

        Match m2 = new Match("home2", "away2", new Date(), GROUP, group.getGroupId());
        m2.setId(match.getId());

        matchRepository.store(m2);

        List<Match> matches = matchRepository.read();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0).getId(), is(equalTo(match.getId())));
    }



}
