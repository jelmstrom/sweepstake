package com.jelmstrom.tips.table;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Traverser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.jelmstrom.tips.persistence.NeoRepository.Relationships.GROUP;
import static java.util.stream.Collectors.joining;
import static org.neo4j.graphdb.Direction.*;

public class NeoTablePredictionRepository extends NeoRepository implements TablePredictionRepository {
    public NeoTablePredictionRepository(String testRepo) {
        super();
    }

    @Override
    public void store(TablePrediction prediction) {
        try(Transaction tx = vmTips.beginTx()) {
            Node predictionNode;
            if(null == prediction.getId()){
                Node group = vmTips.getNodeById(prediction.group);

                Optional<Relationship> existing = StreamSupport.stream(group.getRelationships(INCOMING, GROUP).spliterator(), false)
                        .filter(rel -> rel.getStartNode().hasLabel(TABLE_PREDICTION) &&
                                      prediction.userId.equals(rel.getProperty("userId")))
                        .findFirst();
                if(existing.isPresent()){
                    predictionNode = existing.get().getStartNode();
                } else {
                    predictionNode = vmTips.createNode(TABLE_PREDICTION);
                    Relationship relationshipTo = predictionNode.createRelationshipTo(group, GROUP);
                    relationshipTo.setProperty("userId", prediction.userId);
                }
            } else  {
                predictionNode = vmTips.getNodeById(prediction.getId());
            }

            populateNode(prediction, predictionNode);
            tx.success();
        }
    }

    public void populateNode(TablePrediction prediction, Node node) {
        node.setProperty("group", prediction.group);
        node.setProperty("groupPrediction", prediction.tablePrediction.stream().collect(joining(":")));
        node.setProperty("userId", prediction.userId);
        vmTips.getNodeById(prediction.userId).createRelationshipTo(node, Relationships.TABLE_PREDICTION);
        prediction.setId(node.getId());
    }

    @Override
    public TablePrediction readPrediction(TablePrediction user) {
        if(null == user.getId()){
            return readPrediction(user.userId, user.group);
        } else {
            try(Transaction tx = vmTips.beginTx()){
                TablePrediction prediction = buildTablePrediction(vmTips.getNodeById(user.getId()));
                tx.success();
                return prediction;
            }
        }
    }

    private TablePrediction buildTablePrediction(Node nodeById) {
        String table = nodeById.getProperty("groupPrediction").toString();
        Long group = Long.parseLong(nodeById.getProperty("group").toString());
        Long userId = Long.parseLong(nodeById.getProperty("userId").toString());
        TablePrediction prediction =  new TablePrediction(group, userId, Arrays.asList(table.split(":")));
        prediction.setId(nodeById.getId());
        return prediction;

    }

    @Override
    public TablePrediction readPrediction(Long userId, Long group) {
        try(Transaction tx = vmTips.beginTx()){
            List<TablePrediction> predictions = new ArrayList<>();
            ResourceIterator<Node> nodes = engine.execute(String.format("MATCH (n:%s {userId : %s, group : %s}) return n"
                    , TABLE_PREDICTION.name()
                    , userId
                    , group)).columnAs("n");
            nodes.forEachRemaining(node -> predictions.add(buildTablePrediction(node)));

            tx.success();
            return predictions.get(0);
        }
    }

    @Override
    public List<TablePrediction> read() {
        try(Transaction tx = vmTips.beginTx()){
            List<TablePrediction> predictions = new ArrayList<>();
            ResourceIterator<Node> nodes = engine.execute(String.format("MATCH (n:%s) return n"
                    , TABLE_PREDICTION.name())).columnAs("n");
            nodes.forEachRemaining(node -> predictions.add(buildTablePrediction(node)));
            tx.success();
            return predictions;
        }
    }

    @Override
    public void dropAll() {
        dropAll(TABLE_PREDICTION);
    }

    @Override
    public List<TablePrediction> predictionsFor(Long id) {
        try (Transaction ignored = vmTips.beginTx() ) {
            Traverser traverse = allRelationshipsFor(vmTips.getNodeById(id), Relationships.TABLE_PREDICTION, OUTGOING);
            List<TablePrediction> predictions = new ArrayList<>();
            for(Path path : traverse){
                Node predictionNode = path.endNode();
                predictions.add(buildTablePrediction(predictionNode));
            }
            return predictions;
        }
    }

}
