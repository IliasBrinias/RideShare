package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import com.unipi.diplomaThesis.rideshare.Interface.OnDistanceResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnFilterResult;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class RouteFilter implements Serializable {
    private double maximumDistance = 0.1;
    private String originRiderPlaceId = "";
    private String destinationRiderPlaceId = "";
    private int defaultTimetable = -1;
    private int timetable;
    private long timeUnix = -1;
    private float defaultMinPrice = 0.f, defaultMaxPrice = 100.f;
    private float minPricePerPassenger, maxPricePerPassenger;
    private float defaultMinTime = -12, defaultMaxTime = 12;
    private float minTime, maxTime;
    private float defaultMinRating = 0.f, defaultMaxRating = 5.f;
    private float minRating, maxRating;

    public RouteFilter() {
        minPricePerPassenger = defaultMinPrice;
        maxPricePerPassenger = defaultMaxPrice;
        minTime = defaultMinTime;
        maxTime = defaultMaxTime;
        minRating = defaultMinRating;
        maxRating = defaultMaxRating;
        timetable = defaultTimetable;
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
        if (timetable != -1){
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
                        if (RouteFilter.this.maximumDistance<=0||RouteFilter.this.maximumDistance>=1) RouteFilter.this.maximumDistance = 0.2;
//                         if the route with the rider is maximum 20% bigger than before is acceptable
                        if (distance - r.getRouteLatLng().getDistance()>=RouteFilter.this.maximumDistance*r.getRouteLatLng().getDistance()) {
                            onFilterResult.result(false);
                            return;
                        }
//                         cost Check
                        if (RouteFilter.this.minPricePerPassenger != defaultMinPrice || RouteFilter.this.maxPricePerPassenger != defaultMaxPrice){
                            try {
                                float costPerRider = Float.parseFloat(r.getCostPerRider());
                                if (RouteFilter.this.minPricePerPassenger > costPerRider || RouteFilter.this.maxPricePerPassenger < costPerRider) {
                                    onFilterResult.result(false);
                                    return;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                onFilterResult.result(false);
                                return;
                            }
                        }
//                         timetable check
                        if (RouteFilter.this.timetable != r.getRouteDateTime().getTimetable() && RouteFilter.this.timetable !=-1){
                            onFilterResult.result(false);
                            return;
                        }
//                         time
                        float hourDifference = getCombineDate(r.getRouteDateTime().getStartDateUnix(),
                                r.getRouteDateTime().getStartTimeUnix())
                                - RouteFilter.this.timeUnix/(60.f*60.f);
                        if (minTime != defaultMinTime || maxTime != defaultMaxTime){
                            if (RouteFilter.this.minTime > hourDifference || RouteFilter.this.maxTime < hourDifference) {
                                onFilterResult.result(false);
                                return;
                            }
                        }
//                        Rating
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
}
