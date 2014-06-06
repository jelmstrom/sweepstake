package com.jelmstrom.tips.table;

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

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TablePrediction)) return false;

        TablePrediction that = (TablePrediction) o;

        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        if (tablePrediction != null ? !tablePrediction.equals(that.tablePrediction) : that.tablePrediction != null)
            return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (tablePrediction != null ? tablePrediction.hashCode() : 0);
        return result;
    }
}
