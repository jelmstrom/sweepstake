package com.jelmstrom.tips.group;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;


public class GroupRepository extends MongoRepository {

    public final DBCollection groupCollection;
    public final String NAME = "name";
    public final String TEAMS = "teams";

    public GroupRepository(String context) {
        super(context);
        groupCollection =  getDb().getCollection("groups");
    }

    public void store(Group group){
            BasicDBObject dbGroup = new BasicDBObject(NAME, group.groupName)
                    .append(TEAMS, group.teams);
        if(read(group.groupName).teams.isEmpty()){
            groupCollection.insert(dbGroup);
        } else  {
            groupCollection.update(new BasicDBObject(NAME, group.groupName)
                    , dbGroup);
        }
    }

    public Group read(String groupName) {
        DBObject teams = groupCollection.findOne(new BasicDBObject(NAME, groupName));
        if(teams != null && null != teams.get(NAME)){
            return buildGroup(teams);
        }
        return new Group(groupName, Collections.emptyList());
    }

    private Group buildGroup(DBObject dbGroup) {

        List<String> teams = new ArrayList<>();
        ((BasicDBList) dbGroup.get(TEAMS)).forEach(entry -> teams.add((String) entry));
        return new Group(dbGroup.get(NAME).toString(), teams);
    }

    public void remove(String group) {
        groupCollection.remove(new BasicDBObject(NAME, group));
    }

    public List<Group> read() {
        return groupCollection.find().toArray().stream().map(this::buildGroup).collect(toList());

    }
}
