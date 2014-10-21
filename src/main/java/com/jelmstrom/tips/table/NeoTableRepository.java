package com.jelmstrom.tips.table;

import com.jelmstrom.tips.persistence.NeoRepository;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class NeoTableRepository extends NeoRepository implements TableRepository{
    public NeoTableRepository(String testRepo) {
        super();
    }

    @Override
    public void store(TablePrediction prediction) {
        try(Transaction tx = vmTips.beginTx()) {
            Node node;
            if(null == prediction.getId()){
                node = vmTips.createNode(TABLE_PREDICTION);
            } else  {
                node = vmTips.getNodeById(prediction.getId());
            }
            buildTablePrediction(prediction, node);
            tx.success();
        }
    }

    public void buildTablePrediction(TablePrediction prediction, Node node) {
        node.setProperty("group", prediction.group);
        node.setProperty("groupPrediction", prediction.tablePrediction.stream().collect(joining(":")));
        node.setProperty("userId", prediction.userId);
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
        String prediction = nodeById.getProperty("groupPrediction").toString();
        String group = nodeById.getProperty("group").toString();
        String userId = nodeById.getProperty("userId").toString();
        return new TablePrediction(group, userId, Arrays.asList(prediction.split(":")));
    }

    @Override
    public TablePrediction readPrediction(String userId, String group) {
        try(Transaction tx = vmTips.beginTx()){
            List<TablePrediction> predictions = new ArrayList<>();
            ResourceIterator<Node> nodes = engine.execute(String.format("MATCH (n:%s {userId : '%s', group : '%s'}) return n"
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
}
