package com.jelmstrom.tips.group;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class MongoGroupRepository extends MongoRepository implements GroupRepository {

    public final DBCollection groupCollection;
    public final String NAME = "name";
    public final String TEAMS = "teams";
    public final String ID = "_id";

    public MongoGroupRepository(String context) {
        super(context);
        groupCollection =  getDb().getCollection("groups");
    }

    @Override
    public Group store(Group group){
            BasicDBObject toStore = new BasicDBObject(NAME, group.groupName)
                    .append(TEAMS, group.teams);
        if(null == group.getGroupId()){
            group.setGroupId(UUID.randomUUID().getMostSignificantBits());
            toStore.append(ID, group.getGroupId());
            groupCollection.insert(toStore);
        } else  {
            groupCollection.update(new BasicDBObject(ID, group.getGroupId()), toStore);
        }
        return group;
    }

    @Override
    public Group read(Long groupName) {
        DBObject teams = groupCollection.findOne(new BasicDBObject(ID, groupName));
        if(teams != null && null != teams.get(NAME)){
            return buildGroup(teams);
        }
        return new Group(groupName.toString(), Collections.emptyList());
    }

    @Override
    public List<Group> allGroups() {
        DBCursor groups = groupCollection.find();
        return groups.toArray().stream().map(group -> buildGroup(group)).collect(toList());
    }

    @Override
    public void dropAll() {
        groupCollection.drop();
    }

    private Group buildGroup(DBObject dbGroup) {

        List<String> teams = new ArrayList<>();
        ((BasicDBList) dbGroup.get(TEAMS)).forEach(entry -> teams.add((String) entry));
        Group group = new Group(dbGroup.get(NAME).toString(), teams);
        group.setGroupId(Long.parseLong(dbGroup.get(ID).toString()));
        return group;
    }
}
