package com.jelmstrom.tips.match;

import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.jelmstrom.tips.match.Match.Stage.GROUP;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NeoMatchRepositoryTest {

    private NeoMatchRepository neoMatchRepository = new NeoMatchRepository("test");
    private GroupRepository groupRepository = new NeoGroupRepository("");
    private UserRepository userRepository = new NeoUserRepository("");
    private Match match;
    private User user;
    private Group group;

    @Before
    public void before(){
        user = userRepository.store(new User("test", "test", false, "dkjhskfhs"));
        group = groupRepository.store(new Group("A", Collections.emptyList()));
        match = new Match("home", "away", new Date(), GROUP, group.getGroupId());
        neoMatchRepository.store(match);
    }

    @After
    public void after(){
        neoMatchRepository.dropAll();
        groupRepository.dropAll();
        userRepository.dropAll();
    }

    @Test
    public void createGroupShouldStoreGroup(){
        Match persisted = neoMatchRepository.read(match.getId());
        assertThat(match.getId(), is(equalTo(persisted.getId())));
        assertThat(match, is(equalTo(persisted)));
    }

    @Test
    public void getGroupsShouldReturnListOfOne(){
        List<Match> matches = neoMatchRepository.read();
        assertThat(matches.size(), is(1));
    }

    @Test
    public void updateMatchShouldUpdateExisting(){
        Group group = groupRepository.store(new Group("A", Collections.emptyList()));
        Match m2 = new Match("home2", "away2", new Date(), GROUP, group.getGroupId());
        m2.setId(match.getId());
        neoMatchRepository.store(m2);
        List<Match> matches = neoMatchRepository.read();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0).getId(), is(equalTo(match.getId())));
    }

    @Test
    public void matchPredictionsShouldFindTwoResultsForUser(){
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

    @Test
    public void deleteMatchAndResults(){
        Match m2 = new Match("home2", "away2", new Date(), GROUP, group.getGroupId());
        Result res = new Result(m2, 2, 2, user.id);

        assertThat(m2.results.size(), is(1));
        neoMatchRepository.store(m2);
        assertThat(neoMatchRepository.userPredictions(user.id).size(), is(1));
        neoMatchRepository.drop(m2.getId());
        assertThat(neoMatchRepository.userPredictions(user.id).size(), is(0));
    }

    @Test
    public void listMatchesForGroup(){
        List<Match> matches = neoMatchRepository.groupMatches(group.getGroupId());
        assertThat(matches.size(), is(1));
    }

}
