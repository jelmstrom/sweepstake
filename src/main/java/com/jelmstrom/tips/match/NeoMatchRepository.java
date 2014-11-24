package com.jelmstrom.tips.match;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import java.util.*;

import static org.neo4j.graphdb.traversal.Evaluators.*;

public class NeoMatchRepository extends NeoRepository implements MatchRepository{

    public NeoMatchRepository(String context) {
        //TODO: Get Competition(?) node from context.
        super();
    }

    @Override
    public void store(Match match) {
        try(Transaction tx = vmTips.beginTx()){
            Node matchNode;
            if(match.getNodeId() == null){
                matchNode = vmTips.createNode(MATCH_LABEL);
                match.setNodeId(matchNode.getId());
            } else {
                matchNode = vmTips.getNodeById(match.getNodeId());
            }

            matchNode.setProperty("awayTeam", match.awayTeam);
            matchNode.setProperty("homeTeam", match.homeTeam);
            matchNode.setProperty("matchId", match.id); //todo: relationship( ->Group)
            matchNode.setProperty("matchStart", match.matchStart.getTime());
            matchNode.setProperty("stage", match.stage.toString());
            if(match.hasResult()){
                matchNode.setProperty("homeGoals", match.getCorrectResult().homeGoals);
                matchNode.setProperty("awayGoals", match.getCorrectResult().awayGoals);
                matchNode.setProperty("adminUser", match.getCorrectResult().userId);
                matchNode.setProperty("promoted", match.getCorrectResult().promoted);
            }
            storeMatches(match.results, matchNode);
            tx.success();
        }
    }

    private void storeMatches(HashSet<Result> results, Node parent) {
        for(Result result : results){
            Node resultNode;
            if(null == result.getId()){
                resultNode = vmTips.createNode(RESULT_LABEL);
                parent.createRelationshipTo(resultNode, Relationships.MATCH_PREDICTION);
            } else {
                resultNode = vmTips.getNodeById(result.getId());
            }
            result.setId(resultNode.getId());
            resultNode.setProperty("awayGoals", result.awayGoals.toString());
            resultNode.setProperty("homeGoals", result.homeGoals.toString());
            resultNode.setProperty("promoted", result.promoted);
            resultNode.setProperty("userId", result.userId);
            vmTips.getNodeById(result.userId).createRelationshipTo(resultNode, Relationships.USER_PREDICTION);
        }
    }

    @Override
    public Match read(String matchId) {
        try(Transaction tx = vmTips.beginTx()){
            ExecutionResult execute = engine.execute(
                    String.format("MATCH (n:%s{matchId : '%s'}) return n",
                            MATCH_LABEL.name()
                            , matchId));
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

    private Match buildMatch(Node matchNode) {
        Match match = new Match(
                matchNode.getProperty("homeTeam").toString(),
                matchNode.getProperty("awayTeam").toString(),
                new Date(Long.parseLong(matchNode.getProperty("matchStart").toString())),
                matchNode.getProperty("matchId").toString(),
                Match.Stage.valueOf(matchNode.getProperty("stage").toString())

        );

        if(matchNode.hasProperty("homeGoals")){
            Result correct = new Result(match
                        , Integer.parseInt(matchNode.getProperty("homeGoals").toString())
                        , Integer.parseInt(matchNode.getProperty("awayGoals").toString())
                        , Long.parseLong(matchNode.getProperty("adminUser").toString())
                        , (String)matchNode.getProperty("promoted") //  nullable
            );

            match.setCorrectResult(correct);
        }

        Collection<Result> results = new HashSet<>();
        TraversalDescription td = vmTips.traversalDescription().breadthFirst()
                .relationships(Relationships.MATCH_PREDICTION, Direction.OUTGOING)
                .evaluator(includeWhereLastRelationshipTypeIs(Relationships.MATCH_PREDICTION));
        Traverser traverse = td.traverse(matchNode);
        for(Path path: traverse){
            Node resultNode = path.endNode();
            Result result = buildResult(resultNode, match);
            results.add(result);
        }

        match.results.addAll(results);
        match.setNodeId(matchNode.getId());
        return match;
    }

    private Result buildResult(Node resultNode, Match match) {
        Result result =  new Result(match
                    , Integer.parseInt(resultNode.getProperty("homeGoals").toString())
                    , Integer.parseInt(resultNode.getProperty("awayGoals").toString())
                    , Long.parseLong(resultNode.getProperty("userId").toString())
                    , (String)resultNode.getProperty("promoted"));
        result.setId(resultNode.getId());
        return result;
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

    @Override
    public void dropAll() {
        super.dropAll(RESULT_LABEL);
        super.dropAll(MATCH_LABEL);
    }

    @Override
    public List<Result> userPredictions(long userId) {
        List<Result> results  = new ArrayList<>();
        try(Transaction tx = vmTips.beginTx()){
            TraversalDescription td = vmTips.traversalDescription().breadthFirst().relationships(Relationships.USER_PREDICTION, Direction.OUTGOING)
                    .evaluator(includeWhereLastRelationshipTypeIs(Relationships.USER_PREDICTION));
            Traverser paths = td.traverse(vmTips.getNodeById(userId));
            for(Path path : paths){
                Node resultNode = path.endNode();
                Node matchNode = resultNode.getRelationships(Relationships.MATCH_PREDICTION, Direction.INCOMING).iterator().next().getStartNode();
                matchNode.getPropertyKeys().forEach(key -> System.out.println(key  + " = " + matchNode.getProperty(key)));
                results.add(buildResult(resultNode, buildMatch(matchNode)));
            }
            return results;
        }
    }
}