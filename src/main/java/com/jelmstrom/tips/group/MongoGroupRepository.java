package com.jelmstrom.tips.group;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MongoGroupRepository extends MongoRepository implements GroupRepository {

    public final DBCollection groupCollection;
    public final String NAME = "name";
    public final String TEAMS = "teams";

    public MongoGroupRepository(String context) {
        super(context);
        groupCollection =  getDb().getCollection("groups");
    }

    @Override
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

    @Override
    public Group read(String groupName) {
        DBObject teams = groupCollection.findOne(new BasicDBObject(NAME, groupName));
        if(teams != null && null != teams.get(NAME)){
            return buildGroup(teams);
        }
        return new Group(groupName, Collections.emptyList());
    }

    @Override
    public List<Group> allGroups() {
        DBCursor groups = groupCollection.find();
        return groups.toArray().stream().map(group -> buildGroup(group)).collect(toList());
    }

    private Group buildGroup(DBObject dbGroup) {

        List<String> teams = new ArrayList<>();
        ((BasicDBList) dbGroup.get(TEAMS)).forEach(entry -> teams.add((String) entry));
        return new Group(dbGroup.get(NAME).toString(), teams);
    }
}
