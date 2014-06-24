package com.jelmstrom.tips.table;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TableRepository extends MongoRepository {

    public final DBCollection tablePredictionCollection;


    public TableRepository(String context) {
        super(context);
        tablePredictionCollection = getDb().getCollection("tablePreditions");
    }

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

    public TablePrediction readPrediction(TablePrediction user) {
        DBObject dbPrediction = tablePredictionCollection.findOne(new BasicDBObject("userId", user.userId).append("group", user.group));
        if(dbPrediction != null && null != dbPrediction.get("userId")){
            return buildTablePrediction(dbPrediction);
        }
        return new TablePrediction(user.group, user.userId, Collections.emptyList());
    }

    public TablePrediction readPrediction(String userId, String group) {
        return readPrediction(new TablePrediction(group, userId, null));
    }

    private TablePrediction buildTablePrediction(DBObject dbMatch) {

        List<String> predictions = new ArrayList<>();
        ((BasicDBList) dbMatch.get("prediction")).forEach(entry -> predictions.add((String) entry));
        return new TablePrediction(
                dbMatch.get("group").toString()
                                , (String) dbMatch.get("userId")
                                , predictions);
    }

    public List<TablePrediction> read() {
        DBCursor teams = tablePredictionCollection.find();
        ArrayList<TablePrediction> predictions = new ArrayList<>(teams.size());
        teams.forEach(entry -> predictions.add(buildTablePrediction(entry)));
        return predictions;

    }


}
