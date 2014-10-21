package com.jelmstrom.tips.group;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class NeoGroupRepository extends NeoRepository implements GroupRepository {


    public NeoGroupRepository(String context) {
        //TODO: Get Competition(?) node from context.
        super();
    }

    @Override
    public void store(Group group) {
        try(Transaction tx = vmTips.beginTx()){
            Node node;
            if(null == group.getGroupId()){
                node = vmTips.createNode(GROUP_LABEL);
            } else {
                node = vmTips.getNodeById(group.getGroupId());
            }
            node.setProperty("Name", group.groupName);
            node.setProperty("Teams", group.teams.stream().collect(joining(":")));
            group.setGroupId(node.getId());
            tx.success();
        }
    }

    @Override
    public Group read(String groupId) {
        try(Transaction tx = vmTips.beginTx()){
            ExecutionResult execute = engine.execute("MATCH (n:" + GROUP_LABEL.name() + "{Name : '"+groupId+"'}) return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            List<Group> groups = new ArrayList<>();
            nodes.forEachRemaining(item -> groups.add(buildGroup(item)));
            tx.success();
            if(groups.isEmpty()){
                return new Group(groupId, Collections.emptyList());
            } else if (groups.size() == 1){
                return groups.get(0);
            } else {
                throw new IllegalStateException("Duplicate group names with name " + groupId);
            }
        }
    }



    public Group buildGroup(Node node) {
        List<String> teams = new ArrayList<>();
        if(node.hasProperty("Teams")){
            String teamString = node.getProperty("Teams").toString();
            teams.addAll(Arrays.asList(teamString.split(":")));
        }
        Group group = new Group(node.getProperty("Name").toString(), teams);
        group.setGroupId(node.getId());
        return group;
    }

    @Override
    public List<Group> allGroups() {
        try(Transaction tx = vmTips.beginTx()){
            ExecutionResult execute = engine.execute("MATCH (n:" + GROUP_LABEL.name() + ") return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            List<Group> groups = new ArrayList<>();
            nodes.forEachRemaining(item -> groups.add(buildGroup(item)));
            tx.success();
            return groups;
        }
    }

    @Override
    public void dropAll() {
        super.dropAll(GROUP_LABEL);
    }
}
