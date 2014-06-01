package com.jelmstrom.tips;

import java.util.List;

public class TablePrediction {

    public final String user;
    public final String group;

    public final List<String> tablePrediction;

    public TablePrediction(String user, String group, List<String> tablePrediction) {
        this.user = user;
        this.group = group;
        this.tablePrediction = tablePrediction;
    }
}
