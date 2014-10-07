package com.jelmstrom.tips.group;

import java.util.List;

public interface GroupRepository {
    void store(Group group);

    Group read(String groupName);

    List<Group> allGroups();
}
