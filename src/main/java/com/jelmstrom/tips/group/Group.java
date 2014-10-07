package com.jelmstrom.tips.group;

import java.util.List;

public class Group {
    public final String groupName;
    public final List<String> teams;
    private Long groupId;


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

    public String toString(){
        return groupName + " " + teams;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
