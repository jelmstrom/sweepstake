package com.jelmstrom.tips.match;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jelmstrom on 20/10/14.
 */
public class NeoMatchRepository extends NeoRepository implements MatchRepository{

    public NeoMatchRepository(String context) {
        //TODO: Get Competition(?) node from context.
        super();
    }

    @Override
    public void store(Match match) {
        try(Transaction tx = vmTips.beginTx()){
            Node node;
            if(match.getNodeId() == null){
                node = vmTips.createNode(MATCH_LABEL)
                ;
            } else {
                node = vmTips.getNodeById(match.getNodeId());
            }

            node.setProperty("awayTeam", match.awayTeam);
            node.setProperty("homeTeam", match.homeTeam);
            node.setProperty("matchId", match.id);
            node.setProperty("matchStart", match.matchStart.getTime());
            node.setProperty("stage", match.stage.toString());
            match.setNodeId(node.getId());
            tx.success();
        }
    }

    @Override
    public Match read(String matchId) {
        try(Transaction tx = vmTips.beginTx()){
            ExecutionResult execute = engine.execute("MATCH (n:" + MATCH_LABEL.name() + "{matchId : '"+matchId+"'}) return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            List<Match> matches = new ArrayList<>();
            nodes.forEachRemaining(item -> matches.add(buildMatch(item)));
            tx.success();
            if(matches.isEmpty()){
                return new Match("", "", null, matchId);
            } else if (matches.size() == 1){
                return matches.get(0);
            } else {
                throw new IllegalStateException("Duplicate matches with ID " + matchId);
            }
        }
    }

    private Match buildMatch(Node item) {
        Match match = new Match(
                item.getProperty("homeTeam").toString(),
                item.getProperty("awayTeam").toString(),
                new Date(Long.parseLong(item.getProperty("matchStart").toString())),
                item.getProperty("matchId").toString(),
                Match.Stage.valueOf(item.getProperty("stage").toString())

        );
        match.setNodeId(item.getId());
        return match;
    }

    @Override
    public List<Match> read() {
        List<Match> matches = new ArrayList<>();
        try(Transaction tx = vmTips.beginTx()){
            ExecutionResult execute = engine.execute("MATCH (n:" + MATCH_LABEL.name() + ") return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            nodes.forEachRemaining(item -> matches.add(buildMatch(item)));
            tx.success();
        }
        return matches;
    }

    @Override
    public void store(List<Match> matches) {
        for(Match match : matches){
            store(match);
        }

    }
}

