package com.jelmstrom.tips;

public class TableEntry implements Comparable<TableEntry>{


    public final String team;
    public final int goalsFor;
    public final int goalsAgainst;
    public final int points;

    public TableEntry(String team, int goalsFor, int goalsAgainst, int points) {

        this.team = team;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.points = points;
    }


    @Override
    public int compareTo(TableEntry other) {
        int pointDiff = Integer.compare(this.points, other.points);
        if(pointDiff != 0) {
            return pointDiff;
        }

        int goalDifference = Integer.compare(this.goalDifference(), other.goalDifference());
        if(goalDifference != 0){
            return goalDifference;
        }

        return Integer.compare(this.goalsFor, other.goalsFor);

    }

    public int goalDifference() {
        return goalsFor - goalsAgainst;
    }

    public String toString(){
        return team + "\t" + " " + goalsFor + " - " + goalsAgainst + " ("+goalDifference()+") " +points;

    }
}
