package com.jelmstrom.tips.match;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import java.util.*;
import java.util.stream.StreamSupport;

import static com.jelmstrom.tips.persistence.NeoRepository.Relationships.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.traversal.Evaluators.*;

public class NeoMatchRepository extends NeoRepository implements MatchRepository{

    public NeoMatchRepository(String context) {
        //TODO: Get Competition(?) node from context.
        super();
    }

    @Override
    public Match store(Match match) {
        try(Transaction tx = vmTips.beginTx()){
            Node matchNode;
            if(match.getMatchId() == null){
                matchNode = vmTips.createNode(MATCH_LABEL);
                match.setMatchId(matchNode.getId());
            } else {
                matchNode = vmTips.getNodeById(match.getMatchId());
            }

            matchNode.setProperty("awayTeam", match.awayTeam);
            matchNode.setProperty("homeTeam", match.homeTeam);
            matchNode.setProperty("matchStart", match.matchStart.getTime());
            matchNode.setProperty("stage", match.stage.toString());
            matchNode.setProperty("groupId", match.groupId);
            if(match.hasResult()){
                matchNode.setProperty("homeGoals", match.getCorrectResult().homeGoals);
                matchNode.setProperty("awayGoals", match.getCorrectResult().awayGoals);
                matchNode.setProperty("adminUser", match.getCorrectResult().userId);
                matchNode.setProperty("promoted", match.getCorrectResult().promoted);
            }
            storeResults(match.results, matchNode);
            tx.success();
        }
        return match;
    }

    private void storeResults(HashSet<Result> results, Node parent) {
        for(Result result : results){
            Node resultNode;
            resultNode = getResultNode(parent, result);
            result.setId(resultNode.getId());
            resultNode.setProperty("awayGoals", result.awayGoals.toString());
            resultNode.setProperty("homeGoals", result.homeGoals.toString());
            resultNode.setProperty("promoted", result.promoted);
            resultNode.setProperty("userId", result.userId);
            vmTips.getNodeById(result.userId).createRelationshipTo(resultNode, USER_PREDICTION);
        }
    }

    private Node getResultNode(Node parent, Result result) {
        if(null != result.getId()){
            return vmTips.getNodeById(result.getId());
        } else {

            Optional<Relationship> relationship = StreamSupport.stream(
                    parent.getRelationships(OUTGOING, MATCH_PREDICTION).spliterator(), false)
                    .filter(rel -> checkRelationshipProperty(result.userId, rel))
                    .findFirst();

            Node resultNode;
            if(relationship.isPresent()){
                resultNode = relationship.get().getEndNode();
            } else {
                resultNode = vmTips.createNode(RESULT_LABEL);
                Relationship relationshipTo = parent.createRelationshipTo(resultNode, MATCH_PREDICTION);
                relationshipTo.setProperty("userId", result.userId);
            }

            return resultNode;
        }
    }

    private boolean checkRelationshipProperty(Long userId, Relationship endNode) {
        return userId.equals(endNode.getProperty("userId"));
    }

    private void removeIfUpdate(Relationship item, Result result) {
        Long userId = (Long) item.getEndNode().getProperty("userId");
        if(result.userId.equals(userId));
        item.getEndNode().delete();
    }


    private Long checkForUpdate(Result result) {
        List<Long> results = new ArrayList<>();
        String query = String.format("MATCH (n:%s {userId:%s , matchId:%s }) return n:id"
                , RESULT_LABEL.name()
                , result.userId
                , result.match.getMatchId());
        ResourceIterator<Long> nodes = engine.execute(query).columnAs("n");
        nodes.forEachRemaining(results::add);
        if(results.isEmpty()){
            return -1L;
        } else {
            return results.get(0);
        }
    }

    @Override
    public Match read(Long matchId) {
        try(Transaction tx = vmTips.beginTx()){
            Match match = buildMatch(vmTips.getNodeById(matchId));
            tx.success();
            return match;
        }
    }

    private Match buildMatch(Node matchNode) {
        Match match = new Match(
                matchNode.getProperty("homeTeam").toString(),
                matchNode.getProperty("awayTeam").toString(),
                new Date(Long.parseLong(matchNode.getProperty("matchStart").toString())),
                Match.Stage.valueOf(matchNode.getProperty("stage").toString()),
                Long.parseLong(matchNode.getProperty("groupId").toString()));

        match.setMatchId(matchNode.getId());


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
                .relationships(MATCH_PREDICTION, OUTGOING)
                .evaluator(includeWhereLastRelationshipTypeIs(MATCH_PREDICTION));
        Traverser traverse = td.traverse(matchNode);

        for(Path path: traverse){
            Node resultNode = path.endNode();
            Result result = buildResult(resultNode, match);
            results.add(result);
        }

        match.results.addAll(results);
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
            TraversalDescription td = vmTips.traversalDescription().breadthFirst().relationships(USER_PREDICTION, OUTGOING)
                    .evaluator(includeWhereLastRelationshipTypeIs(USER_PREDICTION));
            Traverser paths = td.traverse(vmTips.getNodeById(userId));
            for(Path path : paths){
                Node resultNode = path.endNode();
                Node matchNode = resultNode.getRelationships(MATCH_PREDICTION, INCOMING).iterator().next().getStartNode();
                matchNode.getPropertyKeys().forEach(key -> System.out.println(key  + " = " + matchNode.getProperty(key)));
                results.add(buildResult(resultNode, buildMatch(matchNode)));
            }
            return results;
        }
    }
}