package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import com.unipi.diplomaThesis.rideshare.Interface.OnDistanceResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnFilterResult;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class RouteFilter implements Serializable {
    private double maximumDistance = 0.3;
    private String originRiderPlaceId = "";
    private String destinationRiderPlaceId = "";
    private int repeatability = -1;
    private long timeUnix = -1;
    private float minPricePerPassenger = -1.f, maxPricePerPassenger = -1.f;
    private float minTime = -12, maxTime = 12;
    private float minRating = 0.f, maxRating = 5.f;

    public RouteFilter() {
    }

    public long getTimeUnix() {
        return timeUnix;
    }

    public void setTimeUnix(long timeUnix) {
        this.timeUnix = timeUnix;
    }

    public int getFilterCount() {
        int countFilters = 0;
        if (repeatability != -1){
            countFilters++;
        }
        if (minPricePerPassenger != -1.f || maxPricePerPassenger != -1.f){
            countFilters++;
        }
        if (minTime != -12.f || maxTime != 12.f){
            countFilters++;
        }
        if (minRating != 0.f || maxRating != 5.f){
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

    public int getRepeatability() {
        return repeatability;
    }

    public void setRepeatability(int repeatability) {
        this.repeatability = repeatability;
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
                        if (distance - r.getRouteLatLng().getDistance()>RouteFilter.this.maximumDistance*r.getRouteLatLng().getDistance()) {
                            onFilterResult.result(false);
                            return;
                        }
//                         cost Check
                        if (RouteFilter.this.minPricePerPassenger != -1.f && RouteFilter.this.maxPricePerPassenger != -1.f){
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
//                         repeatability check
                        if (RouteFilter.this.repeatability != r.getRouteDateTime().getRepeatness() && RouteFilter.this.repeatability!=-1){
                            onFilterResult.result(false);
                            return;
                        }
//                         time
                        float hourDifference = getCombineDate(r.getRouteDateTime().getStartDateUnix(),
                                r.getRouteDateTime().getStartTimeUnix())
                                - RouteFilter.this.timeUnix/(60.f*60.f);
                        if (RouteFilter.this.minTime != -12f || RouteFilter.this.maxTime != 12f){
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
