package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import com.unipi.diplomaThesis.rideshare.Interface.OnFilterResult;

import java.io.Serializable;
import java.time.Duration;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

public class RouteFilter implements Serializable {
    private double maximumDistance = 0.1;
    private String originRiderPlaceId = "";
    private String destinationRiderPlaceId = "";
    private int defaultTimetable = -1;
    private int defaultClassification = -1;
    private double defaultMinPrice = 0.f, defaultMaxPrice = 100.f;
    private float defaultMinTime = -12, defaultMaxTime = 12;
    private float defaultMinRating = 0.f, defaultMaxRating = 5.f;
    private int timetable,classification;
    private long timeUnix = -1;
    private double minPricePerPassenger, maxPricePerPassenger;
    private float minTime, maxTime;
    private float minRating, maxRating;

    public RouteFilter() {
        minPricePerPassenger = defaultMinPrice;
        maxPricePerPassenger = defaultMaxPrice;
        minTime = defaultMinTime;
        maxTime = defaultMaxTime;
        minRating = defaultMinRating;
        maxRating = defaultMaxRating;
        timetable = defaultTimetable;
        classification = defaultClassification;
    }

    public int getDefaultClassification() {
        return defaultClassification;
    }

    public void setDefaultClassification(int defaultClassification) {
        this.defaultClassification = defaultClassification;
    }

    public int getClassification() {
        return classification;
    }

    public void setClassification(int classification) {
        this.classification = classification;
    }

    public void setDefaultMinPrice(double defaultMinPrice) {
        this.defaultMinPrice = defaultMinPrice;
    }

    public void setDefaultMaxPrice(double defaultMaxPrice) {
        this.defaultMaxPrice = defaultMaxPrice;
    }

    public int getDefaultTimetable() {
        return defaultTimetable;
    }

    public void setDefaultTimetable(int defaultTimetable) {
        this.defaultTimetable = defaultTimetable;
    }

    public float getDefaultMinTime() {
        return defaultMinTime;
    }

    public void setDefaultMinTime(float defaultMinTime) {
        this.defaultMinTime = defaultMinTime;
    }

    public float getDefaultMaxTime() {
        return defaultMaxTime;
    }

    public void setDefaultMaxTime(float defaultMaxTime) {
        this.defaultMaxTime = defaultMaxTime;
    }

    public float getDefaultMinRating() {
        return defaultMinRating;
    }

    public void setDefaultMinRating(float defaultMinRating) {
        this.defaultMinRating = defaultMinRating;
    }

    public float getDefaultMaxRating() {
        return defaultMaxRating;
    }

    public void setDefaultMaxRating(float defaultMaxRating) {
        this.defaultMaxRating = defaultMaxRating;
    }

    public double getDefaultMinPrice() {
        return defaultMinPrice;
    }

    public double getDefaultMaxPrice() {
        return defaultMaxPrice;
    }

    public long getTimeUnix() {
        return timeUnix;
    }

    public void setTimeUnix(long timeUnix) {
        this.timeUnix = timeUnix;
    }

    public int getFilterCount() {
        int countFilters = 0;
        if (timetable != defaultTimetable){
            countFilters++;
        }
        if (classification != defaultClassification){
            countFilters++;
        }
        if (minPricePerPassenger != defaultMinPrice || maxPricePerPassenger != defaultMaxPrice){
            countFilters++;
        }
        if (minTime != defaultMinTime || maxTime != defaultMaxTime){
            countFilters++;
        }
        if (minRating != defaultMinRating || maxRating != defaultMaxRating){
            countFilters++;
        }
        return countFilters;
    }

    public void setMinTime(float minTime) {
        this.minTime = minTime;
    }

    public void setMaxTime(float maxTime) {
        this.maxTime = maxTime;
    }

    public float getMinRating() {
        return minRating;
    }

    public void setMinRating(float minRating) {
        this.minRating = minRating;
    }

    public float getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(float maxRating) {
        this.maxRating = maxRating;
    }

    public float getMinTime() {
        return minTime;
    }

    public void setMinTimeUnix(long minTimeUnix) {
        this.minTime = minTimeUnix;
    }

    public float getMaxTime() {
        return maxTime;
    }

    public void setMaxTimeUnix(long maxTimeUnix) {
        this.maxTime = maxTimeUnix;
    }


    public double getMaximumDistance() {
        return maximumDistance;
    }

    public void setMaximumDistance(double maximumDistance) {
        this.maximumDistance = maximumDistance;
    }

    public String getOriginRiderPlaceId() {
        return originRiderPlaceId;
    }

    public void setOriginRiderPlaceId(String originRiderPlaceId) {
        this.originRiderPlaceId = originRiderPlaceId;
    }

    public String getDestinationRiderPlaceId() {
        return destinationRiderPlaceId;
    }

    public void setDestinationRiderPlaceId(String destinationRiderPlaceId) {
        this.destinationRiderPlaceId = destinationRiderPlaceId;
    }

    public double getMinPricePerPassenger() {
        return minPricePerPassenger;
    }

    public void setMinPricePerPassenger(double minPricePerPassenger) {
        this.minPricePerPassenger = minPricePerPassenger;
    }

    public double getMaxPricePerPassenger() {
        return maxPricePerPassenger;
    }

    public void setMaxPricePerPassenger(double maxPricePerPassenger) {
        this.maxPricePerPassenger = maxPricePerPassenger;
    }

    public int getTimetable() {
        return timetable;
    }

    public void setTimetable(int timetable) {
        this.timetable = timetable;
    }

    /**
     * check all the user parameters and finally returns the route.
     * I use the google Directions Api and add the user origin and destination points as waypoints.
     * Then i compare the new Distance Routes with the old and the maximum Deviation
     * @param c
     * @param r
     * @param onFilterResult
     */
    public void filterCheck(Context c, Routes r, OnFilterResult onFilterResult){
        ApiCalls.getDistanceWithWaypoints(c,
                r.getRouteLatLng().getStartPlaceId(),
                r.getRouteLatLng().getEndPlaceId(),
                originRiderPlaceId,
                destinationRiderPlaceId, (response, distance) -> {
                    if (distance >= r.getRouteLatLng().getDistance() + r.getRouteLatLng().getMaximumDeviation()*1000) {
                            onFilterResult.result(false,0);
                            return;
                    }
//                        Cost Check
                    if (!checkCost(r)) {
                        onFilterResult.result(false,0);
                        return;
                    }
//                        date check
                    if (!checkDate(r)) {
                        onFilterResult.result(false,0);
                        return;
                    }
//                        time check
                    if (!checkTime(r)) {
                        onFilterResult.result(false,0);
                        return;
                    }
                    onFilterResult.result(true, Math.abs(distance - r.getRouteLatLng().getDistance()));
                });
    }

    private boolean checkDate(Routes r){
//      Date
//      start
        Calendar routeCalendarStart = new GregorianCalendar();
        routeCalendarStart.setTimeInMillis(r.getRouteDateTime().getStartDateUnix());
        routeCalendarStart.set(Calendar.HOUR,0);
        routeCalendarStart.set(Calendar.MINUTE,0);
        routeCalendarStart.set(Calendar.SECOND,0);
//      end
        Calendar routeCalendarEnd = new GregorianCalendar();
        routeCalendarEnd.set(Calendar.HOUR,24);
        routeCalendarEnd.set(Calendar.MINUTE,60);
        routeCalendarEnd.set(Calendar.SECOND,60);
        routeCalendarEnd.setTimeInMillis(r.getRouteDateTime().getEndDateUnix());
//      userDate
        Calendar userInput = new GregorianCalendar();
        userInput.setTimeInMillis(RouteFilter.this.timeUnix);

        if (routeCalendarStart.getTimeInMillis() > userInput.getTimeInMillis() || userInput.getTimeInMillis() > routeCalendarEnd.getTimeInMillis()){
            return false;
        }

        if (!r.getRouteDateTime().isRepeat()){
            if (routeCalendarStart.get(Calendar.YEAR) != userInput.get(Calendar.YEAR) ||
                    routeCalendarStart.get(Calendar.MONTH) != userInput.get(Calendar.MONTH) ||
                    routeCalendarStart.get(Calendar.DAY_OF_MONTH) != userInput.get(Calendar.DAY_OF_MONTH)) {
                return false;
            }
        }else {
            if (timetable-1 != r.getRouteDateTime().getTimetable() && timetable!=defaultTimetable) return false;
            switch (r.getRouteDateTime().getTimetable()){
                case 0: // daily
                    if (routeCalendarStart.getTimeInMillis() > RouteFilter.this.timeUnix ||
                            RouteFilter.this.timeUnix > routeCalendarEnd.getTimeInMillis()){
                        return false;
                    }
                    break;
                case 1://weekly
//              check if the day of week is the same and if the user date is between start and end route date
                    for (Map.Entry<String,String> d: r.getRouteDateTime().getSelectedDays().entrySet()){
                        if (Integer.parseInt(d.getValue()) == userInput.get(Calendar.DAY_OF_WEEK)){
                            return true;
                        }
                    }
                    return false;
            }
        }
        return true;
    }

    private boolean checkCost(Routes r){
//      cost Check
        if (RouteFilter.this.minPricePerPassenger != defaultMinPrice || RouteFilter.this.maxPricePerPassenger != defaultMaxPrice){
            try {
                Double costPerRider = r.getCostPerRider();
                if (RouteFilter.this.minPricePerPassenger > costPerRider || RouteFilter.this.maxPricePerPassenger < costPerRider) {
                    return false;
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean checkTime(Routes r){
//      time check
        if (minTime != defaultMinTime || maxTime != defaultMaxTime){
            Calendar routeCalendar =new GregorianCalendar();
            routeCalendar.setTimeInMillis(r.getRouteDateTime().getStartDateUnix());
            routeCalendar.set(Calendar.YEAR,0);
            routeCalendar.set(Calendar.MONTH,0);
            routeCalendar.set(Calendar.DAY_OF_MONTH,0);
            Calendar routeFilterCalendar =new GregorianCalendar();
            routeFilterCalendar.setTimeInMillis(RouteFilter.this.timeUnix);
            routeFilterCalendar.set(Calendar.YEAR,0);
            routeFilterCalendar.set(Calendar.MONTH,0);
            routeFilterCalendar.set(Calendar.DAY_OF_MONTH,0);

            Duration duration = Duration.between(routeFilterCalendar.toInstant(),routeCalendar.toInstant());
            float hourDifference = duration.toHours();
            if (RouteFilter.this.minTime > hourDifference || RouteFilter.this.maxTime < hourDifference) {
                return false;
            }
        }
        return true;
    }
}
