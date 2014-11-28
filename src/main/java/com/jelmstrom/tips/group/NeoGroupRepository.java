package com.jelmstrom.tips.group;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.apache.lucene.search.Collector;
import org.hsqldb.lib.StringUtil;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.parboiled.common.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class NeoGroupRepository extends NeoRepository implements GroupRepository {


    public NeoGroupRepository(String context) {
        //TODO: Get Competition(?) node from context.
        super();
    }

    @Override
    public Group store(Group group) {
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
        return group;
    }

    @Override
    public Group read(Long groupId) {
        try(Transaction tx = vmTips.beginTx()){
            return buildGroup(vmTips.getNodeById(groupId));
        }
    }



    public Group buildGroup(Node node) {
        List<String> teams = new ArrayList<>();
        if(node.hasProperty("Teams")){
            String teamString = node.getProperty("Teams").toString();
            teams.addAll(Arrays.asList(teamString.split(":")).stream().filter(StringUtils::isNotEmpty).collect(toList()));
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
