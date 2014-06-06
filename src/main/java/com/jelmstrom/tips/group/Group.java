package com.jelmstrom.tips.group;

import java.util.List;

public class Group {
    public final String groupName;
    public final List<String> teams;

    public Group(String groupName, List<String> teams) {
        this.groupName = groupName;
        this.teams = teams;
    }


    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;

        Group group = (Group) o;

        if (!groupName.equals(group.groupName)) return false;
        if (!teams.equals(group.teams)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupName.hashCode();
        result = 31 * result + teams.hashCode();
        return result;
    }
}
