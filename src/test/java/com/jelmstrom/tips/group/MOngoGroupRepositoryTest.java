package com.jelmstrom.tips.group;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class MongoGroupRepositoryTest {

    private GroupRepository neoGroupRepository =  new MongoGroupRepository("test");
    private Group group;

    @Before
    public void before(){
        group = new Group("A", Arrays.asList("T1", "T2 FF", "T3"));
        neoGroupRepository.store(group);
    }

    @After
    public void after(){
        neoGroupRepository.dropAll();
    }

    @Test
    public void createGroupShouldStoreGroup(){
        Group persisted = neoGroupRepository.read(group.groupName);
        assertThat(group.getGroupId(), is(equalTo(persisted.getGroupId())));
        assertThat(group, is(equalTo(persisted)));
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

}
