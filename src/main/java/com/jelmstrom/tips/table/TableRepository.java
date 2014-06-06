package com.jelmstrom.tips.table;

import com.jelmstrom.tips.persistence.MongoRepository;
import com.mongodb.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TableRepository extends MongoRepository {

    public static final DBCollection tablePredictionCollection = getDb().getCollection("tablePrediction");

    public static void store(TablePrediction prediction){
            BasicDBObject dbPrediction = new BasicDBObject()
                    .append("group", prediction.group)
                    .append("user", prediction.user)
                    .append("prediction", prediction.tablePrediction);
        if(readPrediction(prediction.user, prediction.group).tablePrediction.isEmpty()){
            tablePredictionCollection.insert(dbPrediction);
        } else  {
            tablePredictionCollection.update(new BasicDBObject()
                    .append("group", prediction.group)
                    .append("user", prediction.user)
                    , dbPrediction);
        }
    }

    public static TablePrediction readPrediction(String user, String group) {
        DBObject dbPrediction = tablePredictionCollection.findOne(new BasicDBObject("user", user).append("group", group));
        if(dbPrediction != null && null != dbPrediction.get("user")){
            return buildTablePrediction(dbPrediction);
        }
        return new TablePrediction(user, group, Collections.emptyList());
    }

    private static TablePrediction buildTablePrediction(DBObject dbMatch) {

        List<String> predictions = new ArrayList<>();
        ((BasicDBList) dbMatch.get("prediction")).forEach(entry -> predictions.add((String) entry));
        return new TablePrediction(dbMatch.get("user").toString(), dbMatch.get("group").toString(), predictions);
    }

    public static List<TablePrediction> read() {
        DBCursor teams = tablePredictionCollection.find();
        ArrayList<TablePrediction> predictions = new ArrayList<>(teams.size());
        teams.forEach(entry -> predictions.add(buildTablePrediction(entry)));
        return predictions;

    }


}
