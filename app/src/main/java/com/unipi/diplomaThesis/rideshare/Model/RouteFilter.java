package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import com.unipi.diplomaThesis.rideshare.Interface.OnDistanceResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnFilterResult;

import org.json.JSONObject;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class RouteFilter implements Serializable {
    private double maximumDistance = 0.1;
    private String originRiderPlaceId = "";
    private String destinationRiderPlaceId = "";
    private int defaultTimetable = -1;
    private int defaultClassification = -1;
    private float defaultMinPrice = 0.f, defaultMaxPrice = 100.f;
    private float defaultMinTime = -12, defaultMaxTime = 12;
    private float defaultMinRating = 0.f, defaultMaxRating = 5.f;
    private int timetable,classification;
    private long timeUnix = -1;
    private float minPricePerPassenger, maxPricePerPassenger;
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

    public void setDefaultMinPrice(float defaultMinPrice) {
        this.defaultMinPrice = defaultMinPrice;
    }

    public void setDefaultMaxPrice(float defaultMaxPrice) {
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

    public float getDefaultMinPrice() {
        return defaultMinPrice;
    }

    public float getDefaultMaxPrice() {
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

    public float getMinPricePerPassenger() {
        return minPricePerPassenger;
    }

    public void setMinPricePerPassenger(float minPricePerPassenger) {
        this.minPricePerPassenger = minPricePerPassenger;
    }

    public float getMaxPricePerPassenger() {
        return maxPricePerPassenger;
    }

    public void setMaxPricePerPassenger(float maxPricePerPassenger) {
        this.maxPricePerPassenger = maxPricePerPassenger;
    }

    public int getTimetable() {
        return timetable;
    }

    public void setTimetable(int timetable) {
        this.timetable = timetable;
    }

    public void filterCheck(Context c, Route r, OnFilterResult onFilterResult){
        ApiCalls.getDistanceWithWaypoints(c,
                r.getRouteLatLng().getStartPlaceId(),
                r.getRouteLatLng().getEndPlaceId(),
                originRiderPlaceId,
                destinationRiderPlaceId, new OnDistanceResponse() {
                    @Override
                    public void returnedData(JSONObject response, Double distance) {
//                          TODO:  Test Mode
                            if (RouteFilter.this.maximumDistance <= 0 || RouteFilter.this.maximumDistance >= 1)
                                RouteFilter.this.maximumDistance = 0.2;
//                         if the route with the rider is maximum 20% bigger than before is acceptable
                            if (distance - r.getRouteLatLng().getDistance() >= RouteFilter.this.maximumDistance * r.getRouteLatLng().getDistance()) {
                                onFilterResult.result(false);
                                return;
                            }

//                        Cost Check
                            if (!checkCost(r)) {
                                onFilterResult.result(false);
                                return;
                            }
//                         timetable check
                            if (RouteFilter.this.timetable != r.getRouteDateTime().getTimetable() && RouteFilter.this.timetable != RouteFilter.this.defaultTimetable) {
                                onFilterResult.result(false);
                                return;
                            }
//                        date check
                            if (!checkDate(r)) {
                                onFilterResult.result(false);
                                return;
                            }
//                        time check
                            if (!checkTime(r)) {
                                onFilterResult.result(false);
                                return;
                            }

//                      TODO:  Rating Check
                        onFilterResult.result(true);
                    }
                });
    }

    private long getCombineDate(long date, long time) {
        Calendar dateCal = new GregorianCalendar();
        Calendar timeCal = new GregorianCalendar();

        dateCal.setTimeInMillis(date);
        timeCal.setTimeInMillis(time);

        int year = dateCal.get(Calendar.YEAR);
        int month = dateCal.get(Calendar.MONTH);
        int day = dateCal.get(Calendar.DAY_OF_MONTH);

        int hour = timeCal.get(Calendar.HOUR_OF_DAY);
        int minute = timeCal.get(Calendar.MINUTE);
        int second = timeCal.get(Calendar.SECOND);
        Calendar c = new GregorianCalendar();
        c.set(year,month,day,hour,minute,second);
        return c.getTimeInMillis();
    }
    private boolean checkDate(Route r){
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
        switch (r.getRouteDateTime().getTimetable()){
            case 0://one time
                if (routeCalendarStart.get(Calendar.YEAR) != userInput.get(Calendar.YEAR) ||
                        routeCalendarStart.get(Calendar.MONTH) != userInput.get(Calendar.MONTH) ||
                        routeCalendarStart.get(Calendar.DAY_OF_MONTH) != userInput.get(Calendar.DAY_OF_MONTH)) {
                    return false;
                }
                break;
            case 1: // daily
                if (routeCalendarStart.getTimeInMillis() > RouteFilter.this.timeUnix ||
                        RouteFilter.this.timeUnix > routeCalendarEnd.getTimeInMillis()){
                    return false;
                }
                break;
            case 2://weekly
//              check if the day of week is the same and if the user date is between start and end route date
                if (routeCalendarStart.get(Calendar.DAY_OF_WEEK) != userInput.get(Calendar.DAY_OF_WEEK)){
                    return false;
                }
                break;
            case 3://monthly
//              find the last day Month of user Input
                int lastDayMonth = YearMonth.of(userInput.get(Calendar.YEAR),userInput.get(Calendar.MONTH)+1).lengthOfMonth();
//              if is it smaller check for the day of the month
                if (lastDayMonth > routeCalendarStart.get(Calendar.DAY_OF_MONTH)){
                    if (routeCalendarStart.get(Calendar.DAY_OF_MONTH) != userInput.get(Calendar.DAY_OF_MONTH)){
                        return false;
                    }else {
                        break;
                    }
                }else {
//                  else check if is it the last day
                    if (lastDayMonth != userInput.get(Calendar.DAY_OF_MONTH)){
                        return false;
                    }
                }
                break;
            case 4://yearly

                break;
            case 5:

                //create a list of the selected Days
                List<String> selectedDays = new ArrayList<>(r.getRouteDateTime().getSelectedDays().values());
                Collections.sort(selectedDays);
                boolean isSameDayOfTheWeek = false;
                for (String days: selectedDays){
                    Calendar currentCalendar = new GregorianCalendar();
                    currentCalendar.setTimeInMillis(r.getRouteDateTime().getStartDateUnix());
//                  for every day create a Calendar with the startRouteDateTime as Unix time
//                   and check if the Day of Week is the same with the users
                    currentCalendar.set(Calendar.DAY_OF_WEEK, Integer.valueOf(days));
                    if (currentCalendar.get(Calendar.DAY_OF_WEEK) == userInput.get(Calendar.DAY_OF_WEEK)) {
                        isSameDayOfTheWeek = true;
                        break;
                    }
                }
                if (!isSameDayOfTheWeek) {
                    return false;
                }
        }
        return true;
    }

    private boolean checkCost(Route r){
//      cost Check
        if (RouteFilter.this.minPricePerPassenger != defaultMinPrice || RouteFilter.this.maxPricePerPassenger != defaultMaxPrice){
            try {
                float costPerRider = Float.parseFloat(r.getCostPerRider());
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

    private boolean checkTime(Route r){
//      time check
        if (minTime != defaultMinTime || maxTime != defaultMaxTime){

            float hourDifference = (
                    getCombineDate(r.getRouteDateTime().getStartDateUnix(),
                            r.getRouteDateTime().getStartTimeUnix()) - RouteFilter.this.timeUnix) /(60.f*60.f*1000);
            if (RouteFilter.this.minTime > hourDifference || RouteFilter.this.maxTime < hourDifference) {
                return false;
            }
        }
        return true;
    }
}
