package com.jelmstrom.tips.group;

import com.jelmstrom.tips.match.Match;

import java.util.List;

public interface GroupRepository {
    Group store(Group group);

    Group read(Long groupId);

    List<Group> allGroups();

    void dropAll();


    List<Group> read(Match.Stage group);
}
