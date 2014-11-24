package com.jelmstrom.tips.group;

import java.util.List;

public interface GroupRepository {
    Group store(Group group);

    Group read(Long groupId);

    List<Group> allGroups();

    void dropAll();


}
