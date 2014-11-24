package com.jelmstrom.tips.table;

import java.util.List;

/**
 * Created by jelmstrom on 21/10/14.
 */
public interface TablePredictionRepository {
    void store(TablePrediction prediction);

    TablePrediction readPrediction(TablePrediction user);

    TablePrediction readPrediction(Long userId, String group);

    List<TablePrediction> read();

    void dropAll();

    List<TablePrediction> predictionsFor(Long id);
}
