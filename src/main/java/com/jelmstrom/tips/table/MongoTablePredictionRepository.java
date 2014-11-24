package com.jelmstrom.tips.table;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MongoTablePredictionRepository extends MongoRepository implements TablePredictionRepository {

    public final DBCollection tablePredictionCollection;


    public MongoTablePredictionRepository(String context) {
        super(context);
        tablePredictionCollection = getDb().getCollection("tablePreditions");
    }

    @Override
    public void store(TablePrediction prediction){
            BasicDBObject dbPrediction = new BasicDBObject()
                    .append("group", prediction.group)
                    .append("prediction", prediction.tablePrediction)
                    .append("userId", prediction.userId);
        TablePrediction storedPrediction = readPrediction(prediction);
        if(storedPrediction.tablePrediction.isEmpty()){
            tablePredictionCollection.insert(dbPrediction);
        } else  {
            tablePredictionCollection.update(new BasicDBObject()
                    .append("group", prediction.group)
                    .append("userId", prediction.userId)
                    , dbPrediction);
        }
    }

    @Override
    public TablePrediction readPrediction(TablePrediction user) {
        DBObject dbPrediction = tablePredictionCollection.findOne(new BasicDBObject("userId", user.userId).append("group", user.group));
        if(dbPrediction != null && null != dbPrediction.get("userId")){
            return buildTablePrediction(dbPrediction);
        }
        return new TablePrediction(user.group, user.userId, Collections.emptyList());
    }

    @Override
    public TablePrediction readPrediction(Long userId, String group) {
        return readPrediction(new TablePrediction(group, userId, null));
    }

    private TablePrediction buildTablePrediction(DBObject dbMatch) {

        List<String> predictions = new ArrayList<>();
        ((BasicDBList) dbMatch.get("prediction")).forEach(entry -> predictions.add((String) entry));
        return new TablePrediction(
                dbMatch.get("group").toString()
                                , (Long)dbMatch.get("userId")
                                , predictions);
    }

    @Override
    public List<TablePrediction> read() {
        DBCursor teams = tablePredictionCollection.find();
        ArrayList<TablePrediction> predictions = new ArrayList<>(teams.size());
        teams.forEach(entry -> predictions.add(buildTablePrediction(entry)));
        return predictions;

    }

    @Override
    public void dropAll() {
        tablePredictionCollection.drop();
    }

    @Override
    public List<TablePrediction> predictionsFor(Long id) {
        return null;
    }


}
