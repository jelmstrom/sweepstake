package com.jelmstrom.tips.match;

import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.GroupRepository;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.persistence.NeoRepository;
import com.jelmstrom.tips.user.NeoUserRepository;
import com.jelmstrom.tips.user.User;
import com.jelmstrom.tips.user.UserRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.jelmstrom.tips.match.Match.Stage.GROUP;
import static com.jelmstrom.tips.match.Match.Stage.LAST_SIXTEEN;
import static org.hamcrest.Matchers.notNullValue;
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
        match = new Match("home", "away", ZonedDateTime.now(Config.STOCKHOLM), GROUP, group.getGroupId());
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
        Match m2 = new Match("home2", "away2", ZonedDateTime.now(Config.STOCKHOLM), GROUP, group.getGroupId());
        m2.setId(match.getId());
        neoMatchRepository.store(m2);
        List<Match> matches = neoMatchRepository.read();
        assertThat(matches.size(), is(1));
        assertThat(matches.get(0).getId(), is(equalTo(match.getId())));
    }

    @Test
    public void matchPredictionsShouldFindTwoResultsForUser(){
        Match m2 = new Match("home2", "away2", ZonedDateTime.now(Config.STOCKHOLM), GROUP, group.getGroupId());
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
        Match m2 = new Match("home2", "away2", ZonedDateTime.now(Config.STOCKHOLM), GROUP, group.getGroupId());
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


    @Test
    public void createPlayoffMatch(){
        Group playoff = new Group("playoff", Collections.EMPTY_LIST);
        playoff = groupRepository.store(playoff);
        Match match = new Match("", "", ZonedDateTime.now(Config.STOCKHOLM), LAST_SIXTEEN, playoff.getGroupId());
        match = neoMatchRepository.store(match);
        assertThat(match.getId(), is(notNullValue()));
    }

    @Test
    public void getByStage(){
        Group playoff = new Group("playoff", Collections.EMPTY_LIST);
        playoff = groupRepository.store(playoff);
        Match match = new Match("", "", ZonedDateTime.now(Config.STOCKHOLM), LAST_SIXTEEN, playoff.getGroupId());
        match = neoMatchRepository.store(match);
        assertThat(match.getId(), is(notNullValue()));
        assertThat(neoMatchRepository.stageMatches(LAST_SIXTEEN).size(), is(1));
    }



    @Test
    public void playoffRelationShouldPopulateDownstream() {
        Group ls = new Group("playoff", Collections.EMPTY_LIST, Match.Stage.LAST_SIXTEEN);
        Group qf = new Group("playoff", Collections.EMPTY_LIST, Match.Stage.QUARTER_FINAL);
        ls = groupRepository.store(ls);
        qf = groupRepository.store(qf);
        Match match = new Match("", "", ZonedDateTime.now(Config.STOCKHOLM), Match.Stage.LAST_SIXTEEN, ls.getGroupId());
        Match matchQf = new Match("", "", ZonedDateTime.now(Config.STOCKHOLM), Match.Stage.QUARTER_FINAL, ls.getGroupId());
        match = neoMatchRepository.store(match);
        matchQf = neoMatchRepository.store(matchQf);
        neoMatchRepository.addRelation(match, "homeTeam", matchQf);
        match.setCorrectResult(new Result(match, 1, 2, user.id, "Team2"));
        neoMatchRepository.store(match);
        String homeTeam = neoMatchRepository.read(matchQf.getId()).homeTeam;
        assertThat(homeTeam, Matchers.equalTo("Team2"));

    }
}
