package com.unipi.diplomaThesis.rideshare.Model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RouteDateTime implements Serializable {
    private String routeDateId;
    private long startDateUnix;
    private boolean repeat;
    private int timetable;
    Map<String,String> selectedDays = new HashMap<>();
    private long endDateUnix;

    public RouteDateTime() {
    }

    public RouteDateTime(String routeDateId, long startDateUnix, boolean repeat, int timetable, Map<String, String> selectedDays, long endDateUnix) {
        this.routeDateId = routeDateId;
        this.startDateUnix = startDateUnix;
        this.repeat = repeat;
        this.timetable = timetable;
        this.selectedDays = selectedDays;
        this.endDateUnix = endDateUnix;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
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

    public int getTimetable() {
        return timetable;
    }

    public void setTimetable(int timetable) {
        this.timetable = timetable;
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
