package com.unipi.diplomaThesis.rideshare.Model;

import java.io.Serializable;
import java.util.Map;

public class RouteDateTime implements Serializable {
    private String routeDateId;
    private long startDateUnix;
    private long startTimeUnix;
    private int repeatness;
    Map<String,String> selectedDays;
    private long endDateUnix;

    public RouteDateTime() {
    }

    public RouteDateTime(String routeDateId, long startDateUnix, long startTimeUnix, int repeatness, Map<String, String> selectedDays, long endDateUnix) {
        this.routeDateId = routeDateId;
        this.startDateUnix = startDateUnix;
        this.startTimeUnix = startTimeUnix;
        this.repeatness = repeatness;
        this.selectedDays = selectedDays;
        this.endDateUnix = endDateUnix;
    }

    public RouteDateTime(String routeDateId, long startDateUnix, long startTimeUnix, int repeatness, long endDateUnix) {
        this.routeDateId = routeDateId;
        this.startDateUnix = startDateUnix;
        this.startTimeUnix = startTimeUnix;
        this.repeatness = repeatness;
        this.endDateUnix = endDateUnix;
    }

    public String getRouteDateId() {
        return routeDateId;
    }

    public void setRouteDateId(String routeDateId) {
        this.routeDateId = routeDateId;
    }

    public long getStartDateUnix() {
        return startDateUnix;
    }

    public void setStartDateUnix(long startDateUnix) {
        this.startDateUnix = startDateUnix;
    }

    public long getStartTimeUnix() {
        return startTimeUnix;
    }

    public void setStartTimeUnix(long startTimeUnix) {
        this.startTimeUnix = startTimeUnix;
    }

    public int getRepeatness() {
        return repeatness;
    }

    public void setRepeatness(int repeatness) {
        this.repeatness = repeatness;
    }

    public Map<String, String> getSelectedDays() {
        return selectedDays;
    }

    public void setSelectedDays(Map<String, String> selectedDays) {
        this.selectedDays = selectedDays;
    }

    public long getEndDateUnix() {
        return endDateUnix;
    }

    public void setEndDateUnix(long endDateUnix) {
        this.endDateUnix = endDateUnix;
    }
}
