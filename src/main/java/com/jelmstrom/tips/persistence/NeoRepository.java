package com.jelmstrom.tips.persistence;


import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
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

    public static enum RelationShips implements RelationshipType {
        MATCH_PREDICTION,
        USER_PREDICTION,
        MATCH_IN;
    }

    protected static Label groupLabel =  new Label() {
        @Override
        public String name() {
            return "Group";
        }
    };


}
