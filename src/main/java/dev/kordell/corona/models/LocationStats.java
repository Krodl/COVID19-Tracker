package dev.kordell.corona.models;

public class LocationStats {

    private String state;
    private int latestReportedCases;
    private int diffFromPrevDay;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getLatestReportedCases() {
        return latestReportedCases;
    }

    public void setLatestReportedCases(int latestReportedCases) {
        this.latestReportedCases = latestReportedCases;
    }

    public int getDiffFromPrevDay() {
        return diffFromPrevDay;
    }

    public void setDiffFromPrevDay(int diffFromPrevDay) {
        this.diffFromPrevDay = diffFromPrevDay;
    }

    @Override
    public String toString() {
        return "LocationStats{" +
                "state='" + state + '\'' +
                ", latestReportedCases=" + latestReportedCases +
                '}';
    }
}
