package com.jelmstrom.tips.table;

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

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(TableEntry other) {
        // numbers sorted in reverse order, i.e. descending
        int pointDiff = Integer.compare(other.points, this.points);
        if(pointDiff != 0) {
            return pointDiff;
        }

        int goalDifference = Integer.compare(other.goalDifference(), this.goalDifference());
        if(goalDifference != 0){
            return goalDifference;
        }

        int goalsFor = Integer.compare(other.goalsFor, this.goalsFor);
        if(goalsFor != 0){
            return goalDifference;
        }
        //TODO: use the score from any match played here
        //name sorted in normal order, i.e. ascending.
        return team.compareToIgnoreCase(other.team);
    }

    public int goalDifference() {
        return goalsFor - goalsAgainst;
    }

    public String toString(){
        return team + "\t" + " " + goalsFor + " - " + goalsAgainst + " ("+goalDifference()+") " +points;

    }
}
