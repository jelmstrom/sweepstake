package com.jelmstrom.tips.persistence;


import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.*;

import java.util.Optional;
import java.util.stream.StreamSupport;

public abstract class NeoRepository {

    protected static final GraphDatabaseService vmTips;
    protected static ExecutionEngine engine;

    static {
        vmTips = new GraphDatabaseFactory().newEmbeddedDatabase("VmTipsDbd");
        engine = new ExecutionEngine( vmTips );
        registerShutdownHook(vmTips);
    }



    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    public abstract void dropAll();

    public org.neo4j.graphdb.traversal.Traverser allRelationshipsFor(Node node, Relationships relationships, Direction direction) {
        return vmTips.traversalDescription().depthFirst()
                        .relationships(relationships, direction)
                        .evaluator(Evaluators.atDepth(1))
                        .traverse(node);
    }


    public static enum Relationships implements RelationshipType {
        MATCH_PREDICTION,
        USER_PREDICTION,
        TABLE_PREDICTION,
        GROUP
    }

    protected void dropAll(Label label) {
        try(Transaction tx = vmTips.beginTx()){
            ExecutionResult execute = engine.execute("MATCH (n:" + label.name() + ") return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            nodes.forEachRemaining(this::deleteNode);
            tx.success();
        }
    }

    private void deleteNode(Node n) {
        n.getRelationships().forEach(Relationship::delete);
        n.delete();

    }

    protected static final Label GROUP_LABEL = () -> "Group";
    protected static final Label MATCH_LABEL = () -> "Match";
    protected static final Label TABLE_PREDICTION = () -> "TablePrediction";
    protected static final Label RESULT_LABEL = () -> "Result";
    protected static final Label USER_LABEL = () -> "User";
}