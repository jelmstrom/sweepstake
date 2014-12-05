package com.jelmstrom.tips.group;


import com.jelmstrom.tips.group.Group;
import com.jelmstrom.tips.group.NeoGroupRepository;
import com.jelmstrom.tips.match.Match;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NeoGroupRepositoryTest {

    private NeoGroupRepository neoGroupRepository =  new NeoGroupRepository("test");
    private Group group;

    @Before
    public void before(){
        List<String> teams = new ArrayList<>(Arrays.asList("T1", "T2 FF", "T3"));
        group = new Group("A", teams);
        neoGroupRepository.store(group);
    }

    @After
    public void after(){
        neoGroupRepository.dropAll();
    }

    @Test
    public void createGroupShouldStoreGroup(){
        Group persisted = neoGroupRepository.read(group.getGroupId());
        assertThat(group.getGroupId(), is(equalTo(persisted.getGroupId())));
        assertThat(group, is(equalTo(persisted)));
        assertThat(group.stage, is(Match.Stage.GROUP));
    }

    @Test
    public void getGroupByStageReturnsGroup(){
        Group read = neoGroupRepository.read(Match.Stage.GROUP).get(0);
        assertThat(read, is(equalTo(group)));
        assertThat(read.stage, is(Match.Stage.GROUP));
    }

    @Test
    public void createPlayoffGroupShouldStoreGroup(){
        Group playoff = new Group("pl", Collections.<String>emptyList(), Match.Stage.FINAL);
        Group persisted = neoGroupRepository.store(playoff);
        assertThat(playoff.getGroupId(), is(equalTo(persisted.getGroupId())));
        assertThat(playoff, is(equalTo(persisted)));
        assertThat(persisted.stage, is(Match.Stage.FINAL));
    }

    @Test
    public void getGroupsShouldReturnListOfOne(){
        List<Group> groups = neoGroupRepository.allGroups();
        assertThat(groups.size(), is(1));
    }

    @Test
    public void updateNodeShouldUpdateExistingNode(){
        Group g = new Group("B", group.teams);
        g.setGroupId(group.getGroupId());
        neoGroupRepository.store(g);
        List<Group> groups = neoGroupRepository.allGroups();
        assertThat(groups.size(), is(1));
        assertThat(groups.get(0).getGroupId(), is(equalTo(group.getGroupId())));
    }

    @Test
    public void addTeamToGroupShouldAddATeam(){
        int originalSize = group.teams.size();
        group.teams.add("t5");
        neoGroupRepository.store(group);
        assertThat(neoGroupRepository.read(group.getGroupId()).teams.size(), is(originalSize+1));
    }

}
