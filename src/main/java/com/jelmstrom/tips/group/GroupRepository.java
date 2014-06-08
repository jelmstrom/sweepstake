package com.jelmstrom.tips.group;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GroupRepository extends MongoRepository {

    public static final DBCollection groupCollection = getDb().getCollection("groups");
    public static final String NAME = "name";
    public static final String TEAMS = "teams";

    public static void store(Group group){
            BasicDBObject dbGroup = new BasicDBObject(NAME, group.groupName)
                    .append(TEAMS, group.teams);
        if(read(group.groupName).teams.isEmpty()){
            groupCollection.insert(dbGroup);
        } else  {
            groupCollection.update(new BasicDBObject(NAME, group.groupName)
                    , dbGroup);
        }
    }

    public static Group read(String groupName) {
        DBObject teams = groupCollection.findOne(new BasicDBObject(NAME, groupName));
        if(teams != null && null != teams.get(NAME)){
            return buildGroup(teams);
        }
        return new Group(groupName, Collections.emptyList());
    }

    private static Group buildGroup(DBObject dbMatch) {

        List<String> teams = new ArrayList<>();
        ((BasicDBList) dbMatch.get(TEAMS)).forEach(entry -> teams.add((String) entry));
        return new Group(dbMatch.get(NAME).toString(), teams);
    }

    public static void remove(String group) {
        groupCollection.remove(new BasicDBObject(NAME, group));
    }
}
