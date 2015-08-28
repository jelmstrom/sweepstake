package com.jelmstrom.tips.match;

import com.jelmstrom.tips.configuration.Config;
import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.jelmstrom.tips.persistence.NeoRepository.Relationships.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.traversal.Evaluators.includeWhereLastRelationshipTypeIs;

public class NeoMatchRepository extends NeoRepository implements MatchRepository {

    @SuppressWarnings("UnusedParameters")
    public NeoMatchRepository(String context) {
        super();
    }

    @Override
    public Match store(Match newMatch) {
        try (Transaction tx = vmTips.beginTx()) {
            Node matchNode;

            if (newMatch.getId() == null) {
                matchNode = vmTips.createNode(MATCH_LABEL);
                newMatch.setId(matchNode.getId());
                Relationship groupRelation = matchNode.createRelationshipTo(vmTips.getNodeById(newMatch.groupId), GROUP);
                groupRelation.setProperty("groupId", newMatch.groupId);
            } else {
                matchNode = vmTips.getNodeById(newMatch.getId());
            }

            if (!StringUtils.isEmpty(newMatch.awayTeam) || !matchNode.hasProperty("awayTeam")) {
                matchNode.setProperty("awayTeam", newMatch.awayTeam);
            }
            if (!StringUtils.isEmpty(newMatch.homeTeam) || !matchNode.hasProperty("homeTeam")) {
                matchNode.setProperty("homeTeam", newMatch.homeTeam);
            }
            matchNode.setProperty("matchStart", newMatch.matchStart.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            matchNode.setProperty("stage", newMatch.stage.toString());
            matchNode.setProperty("groupId", newMatch.groupId);

            if (newMatch.hasResult()) {
                matchNode.setProperty("homeGoals", newMatch.getCorrectResult().homeGoals);
                matchNode.setProperty("awayGoals", newMatch.getCorrectResult().awayGoals);
                matchNode.setProperty("adminUser", newMatch.getCorrectResult().userId);
                matchNode.setProperty("promoted", newMatch.getCorrectResult().promoted);
                Iterable<Relationship> relationships = matchNode.getRelationships(OUTGOING, Relationships.WINNER);
                relationships.forEach(rel -> updatePromotedTeam(rel, newMatch.getCorrectResult().promoted));
            }

            storeResults(newMatch.results, matchNode);
            tx.success();
        }
        return newMatch;
    }

    private void updatePromotedTeam(Relationship rel, String promoted) {
        System.out.println(rel.getEndNode().getId() + " - " + rel.getProperty("teamPosition") + " => " + promoted);
        rel.getEndNode().setProperty(rel.getProperty("teamPosition").toString(), promoted);
    }

    private void storeResults(HashSet<Result> results, Node match) {
        for (Result result : results) {
            Node resultNode;
            resultNode = getResultNode(match, result);
            result.setId(resultNode.getId());
            resultNode.setProperty("awayGoals", result.awayGoals.toString());
            resultNode.setProperty("homeGoals", result.homeGoals.toString());
            resultNode.setProperty("promoted", result.promoted);
            resultNode.setProperty("userId", result.userId);
            vmTips.getNodeById(result.userId).createRelationshipTo(resultNode, USER_PREDICTION);
        }
    }

    private Node getResultNode(Node match, Result result) {
        if (null != result.getId()) {
            return vmTips.getNodeById(result.getId());
        } else {

            Optional<Relationship> relationship = StreamSupport.stream(
                    match.getRelationships(OUTGOING, MATCH_PREDICTION).spliterator(), false)
                    .filter(rel -> checkRelationshipProperty(result.userId, rel))
                    .findFirst();

            Node resultNode;
            if (relationship.isPresent()) {
                resultNode = relationship.get().getEndNode();
            } else {
                resultNode = vmTips.createNode(RESULT_LABEL);
                Relationship relationshipTo = match.createRelationshipTo(resultNode, MATCH_PREDICTION);
                relationshipTo.setProperty("userId", result.userId);
            }

            return resultNode;
        }
    }

    private boolean checkRelationshipProperty(Long userId, Relationship endNode) {
        return userId.equals(endNode.getProperty("userId"));
    }


    @Override
    public Match read(Long matchId) {
        try (Transaction tx = vmTips.beginTx()) {
            Match match = buildMatch(vmTips.getNodeById(matchId));
            tx.success();
            return match;
        }
    }

    private Match buildMatch(Node matchNode) {
        Match match = new Match(
                matchNode.getProperty("homeTeam").toString(),
                matchNode.getProperty("awayTeam").toString(),
                ZonedDateTime.parse(matchNode.getProperty("matchStart").toString(), DateTimeFormatter.ISO_ZONED_DATE_TIME),
                Match.Stage.valueOf(matchNode.getProperty("stage").toString()),
                Long.parseLong(matchNode.getProperty("groupId").toString()));

        match.setId(matchNode.getId());


        if (matchNode.hasProperty("homeGoals")) {
            Result correct = new Result(match
                    , Integer.parseInt(matchNode.getProperty("homeGoals").toString())
                    , Integer.parseInt(matchNode.getProperty("awayGoals").toString())
                    , Long.parseLong(matchNode.getProperty("adminUser").toString())
                    , (String) matchNode.getProperty("promoted") //  nullable
            );

            match.setCorrectResult(correct);
        }

        matchNode.getRelationships(OUTGOING, MATCH_PREDICTION).forEach(rel -> buildResult(rel.getEndNode(), match));

        match.results.addAll(new HashSet<>());
        return match;
    }

    private Result buildResult(Node resultNode, Match match) {
        Result result = new Result(match
                , Integer.parseInt(resultNode.getProperty("homeGoals").toString())
                , Integer.parseInt(resultNode.getProperty("awayGoals").toString())
                , Long.parseLong(resultNode.getProperty("userId").toString())
                , (String) resultNode.getProperty("promoted"));
        result.setId(resultNode.getId());
        return result;
    }

    @Override
    public List<Match> read() {
        List<Match> matches = new ArrayList<>();
        try (Transaction tx = vmTips.beginTx()) {
            ExecutionResult execute = engine.execute("MATCH (n:" + MATCH_LABEL.name() + ") return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            nodes.forEachRemaining(item -> matches.add(buildMatch(item)));
            tx.success();
        }
        return matches;
    }


    @Override
    public void store(List<Match> matches) {
        for (Match match : matches) {
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
        List<Result> results = new ArrayList<>();
        try (Transaction tx = vmTips.beginTx()) {
            TraversalDescription td = vmTips.traversalDescription().breadthFirst().relationships(USER_PREDICTION, OUTGOING)
                    .evaluator(includeWhereLastRelationshipTypeIs(USER_PREDICTION));
            Traverser paths = td.traverse(vmTips.getNodeById(userId));

            for (Path path : paths) {
                Node resultNode = path.endNode();
                Node matchNode = resultNode.getRelationships(MATCH_PREDICTION, INCOMING).iterator().next().getStartNode();
                results.add(buildResult(resultNode, buildMatch(matchNode)));
            }
            tx.success();
            return results;
        }
    }

    @Override
    public void drop(Long id) {
        try (Transaction tx = vmTips.beginTx()) {
            Node matchNode = vmTips.getNodeById(id);
            Iterable<Relationship> relationships = matchNode.getRelationships(Direction.BOTH);
            relationships.forEach(this::remove);
            matchNode.delete();
            tx.success();
        }
    }

    @Override
    public List<Match> groupMatches(Long groupId) {

        try (Transaction tx = vmTips.beginTx()) {
            List<Match> matches = new ArrayList<>();
            Node group = vmTips.getNodeById(groupId);
            StreamSupport.stream(group.getRelationships(INCOMING, GROUP).spliterator(), false)
                    .filter(rel -> rel.getStartNode().hasLabel(MATCH_LABEL))
                    .forEach(rel -> matches.add(buildMatch(rel.getStartNode())));
            tx.success();
            return matches;
        }
    }

    @Override
    public List<Match> stageMatches(Match.Stage stage) {
        List<Match> matches = new ArrayList<>();
        try (Transaction tx = vmTips.beginTx()) {
            ExecutionResult execute = engine.execute("MATCH (n:" + MATCH_LABEL.name()
                    + " {stage:'" + stage.toString() + "'}"
                    + ") return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            nodes.forEachRemaining(item -> matches.add(buildMatch(item)));
            tx.success();
        }
        return matches;
    }

    private void remove(Relationship rel) {
        System.out.printf("Relation  %d [%s] Nodes %d (%s) ->  %d(%s) \n"
                , rel.getId()
                , rel.getType().name()
                , rel.getStartNode().getId()
                , rel.getStartNode().getLabels().iterator().next().name()
                , rel.getEndNode().getId()
                , rel.getEndNode().getLabels().iterator().next().name());

        if (rel.getType().name().equals(Relationships.MATCH_PREDICTION.name())) {
            System.out.printf("Delete Prediction %d %s \n"
                    , rel.getEndNode().getId(),
                    rel.getEndNode().getLabels().iterator().next().name());
            rel.getEndNode().getRelationships().forEach(Relationship::delete);
            rel.getEndNode().delete();
        } else {
            rel.delete();
        }
    }

    public void addRelation(Match match, String label, Match nextStage) {
        try (Transaction tx = vmTips.beginTx()) {
            Node matchNode = vmTips.getNodeById(match.getId());
            Node nextStageNode = vmTips.getNodeById(nextStage.getId());
            Relationship relationshipTo = matchNode.createRelationshipTo(nextStageNode, Relationships.WINNER);
            relationshipTo.setProperty("teamPosition", label);
            tx.success();

        }

    }

    @Override
    public SortedMap<Match.Stage, List<Match>> getPlayoffMatches() {
        List<Match> last16 = stageMatches(Match.Stage.LAST_SIXTEEN);
        List<Match> quarterFinal = stageMatches(Match.Stage.QUARTER_FINAL);
        List<Match> semiFinal = stageMatches(Match.Stage.SEMI_FINAL);
        List<Match> finals = stageMatches(Match.Stage.FINAL);
        SortedMap<Match.Stage, List<Match>> playoffMap = new TreeMap<>();
        playoffMap.put(Match.Stage.LAST_SIXTEEN, last16);
        playoffMap.put(Match.Stage.QUARTER_FINAL, quarterFinal);
        playoffMap.put(Match.Stage.SEMI_FINAL, semiFinal);
        playoffMap.put(Match.Stage.FINAL, finals);
        return playoffMap;
    }
}