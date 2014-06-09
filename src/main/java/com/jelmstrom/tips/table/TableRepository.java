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
                    .append("userEmail", prediction.user)
                    .append("prediction", prediction.tablePrediction);
        if(readPrediction(prediction.user, prediction.group).tablePrediction.isEmpty()){
            tablePredictionCollection.insert(dbPrediction);
        } else  {
            tablePredictionCollection.update(new BasicDBObject()
                    .append("group", prediction.group)
                    .append("userEmail", prediction.user)
                    , dbPrediction);
        }
    }

    public TablePrediction readPrediction(String user, String group) {
        DBObject dbPrediction = tablePredictionCollection.findOne(new BasicDBObject("userEmail", user).append("group", group));
        if(dbPrediction != null && null != dbPrediction.get("userEmail")){
            return buildTablePrediction(dbPrediction);
        }
        return new TablePrediction(user, group, Collections.emptyList());
    }

    private TablePrediction buildTablePrediction(DBObject dbMatch) {

        List<String> predictions = new ArrayList<>();
        ((BasicDBList) dbMatch.get("prediction")).forEach(entry -> predictions.add((String) entry));
        return new TablePrediction(dbMatch.get("userEmail").toString(), dbMatch.get("group").toString(), predictions);
    }

    public List<TablePrediction> read() {
        DBCursor teams = tablePredictionCollection.find();
        ArrayList<TablePrediction> predictions = new ArrayList<>(teams.size());
        teams.forEach(entry -> predictions.add(buildTablePrediction(entry)));
        return predictions;

    }


}
