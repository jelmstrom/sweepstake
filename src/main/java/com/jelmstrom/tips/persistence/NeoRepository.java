package com.jelmstrom.tips.persistence;


import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class NeoRepository {

    protected static final GraphDatabaseService vmTips;

    protected static ExecutionEngine engine;

    static {
        vmTips = new GraphDatabaseFactory().newEmbeddedDatabase("VmTips");
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

    public static enum Relationships implements RelationshipType {
        MATCH_PREDICTION,
        USER_PREDICTION,
        MATCH_IN
    }

    public void dropAll(Label label) {
        try(Transaction tx = vmTips.beginTx()){
            ExecutionResult execute = engine.execute("MATCH (n:" + label.name() + ") return n");
            ResourceIterator<Node> nodes = execute.columnAs("n");
            nodes.forEachRemaining(Node::delete);
            tx.success();
        }
    }

    public static final Label GROUP_LABEL =  new Label() {
        @Override
        public String name() {
            return "Group";
        }
    };

    public static final Label MATCH_LABEL =  new Label() {
        @Override
        public String name() {
            return "Match";
        }
    };
}