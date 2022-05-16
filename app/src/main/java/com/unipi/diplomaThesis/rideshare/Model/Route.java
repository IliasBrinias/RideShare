package com.unipi.diplomaThesis.rideshare.Model;


import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.Interface.OnCompleteRouteLoad;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class Route implements Serializable {
    private String routeId;
    private String driverId;
    private RouteLatLng routeLatLng;
    private HashMap<String, String> passengersId = new HashMap<>();
    private RouteDateTime routeDateTime;
    private String costPerRider;
    private int maxRiders;
    private String description;

    public Route(String routeId, String driverId, RouteLatLng routeLatLng, HashMap<String, String> passengersId, RouteDateTime routeDateTime, String costPerRider, int maxRiders, String description) {
        this.routeId = routeId;
        this.driverId = driverId;
        this.routeLatLng = routeLatLng;
        this.passengersId = passengersId;
        this.routeDateTime = routeDateTime;
        this.costPerRider = costPerRider;
        this.maxRiders = maxRiders;
        this.description = description;
    }

    public Route() {
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RouteLatLng getRouteLatLng() {
        return routeLatLng;
    }

    public void setRouteLatLng(RouteLatLng routeLatLng) {
        this.routeLatLng = routeLatLng;
    }

    public RouteDateTime getRouteDateTime() {
        return routeDateTime;
    }

    public void setRouteDateTime(RouteDateTime routeDateTime) {
        this.routeDateTime = routeDateTime;
    }

    public String getCostPerRider() {
        return costPerRider;
    }

    public void setCostPerRider(String costPerRider) {
        this.costPerRider = costPerRider;
    }

    public int getMaxRiders() {
        return maxRiders;
    }

    public void setMaxRiders(int maxRiders) {
        this.maxRiders = maxRiders;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public HashMap<String, String> getPassengersId() {
        return passengersId;
    }

    public void setPassengersId(HashMap<String, String> passengersId) {
        this.passengersId = passengersId;
    }
    public static void loadRoute(String routeId, OnCompleteRouteLoad onCompleteRoutesLoad){
        FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .child(routeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        onCompleteRoutesLoad.returnedRoute(snapshot.getValue(Route.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public long getMinDiff(long userTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar route = new GregorianCalendar();
        Calendar rider = new GregorianCalendar();
        Calendar newRoute = new GregorianCalendar();
        Calendar newRider = new GregorianCalendar();

        route.setTimeInMillis(this.getRouteDateTime().getStartTimeUnix());
        rider.setTimeInMillis(userTime);

        int routeHour = route.get(Calendar.HOUR_OF_DAY);
        int routeMinute = route.get(Calendar.MINUTE);

        int riderHour = rider.get(Calendar.HOUR_OF_DAY);
        int riderMinute = rider.get(Calendar.MINUTE);

        newRoute.set(Calendar.HOUR, routeHour);
        newRoute.set(Calendar.MINUTE, routeMinute);

        newRider.set(Calendar.HOUR, riderHour);
        newRider.set(Calendar.MINUTE, riderMinute);

        Duration duration = Duration.between(newRoute.toInstant(),newRider.toInstant());
        System.out.println(duration.toMinutes());
        return duration.toMinutes();
    }
    public CharSequence getTextForTimeDif(Context c, long userDateTime){
        float hourDifference = getMinDiff(userDateTime)/60.f;
        if (Math.abs((int) hourDifference)>=0.5){
            if (hourDifference == 1) {
                return (1 + " " + c.getString(R.string.hour) + " " + c.getString(R.string.later));
            } else if (hourDifference < 0){
                return (((int)-hourDifference )+ " " + c.getString(R.string.hours) + " " + c.getString(R.string.later));
            }else
            if (hourDifference == -1) {
                return (1 + " " + c.getString(R.string.hour) + " " + c.getString(R.string.sooner));
            } else if (hourDifference > 0){
                return (((int) hourDifference) + " " + c.getString(R.string.hours) + " " + c.getString(R.string.sooner));
            }
        }else {
            return (c.getString(R.string.same_time));
        }
        return "";
    }
    public int getColorForRideCapacitySlider(){
        int maxRiders = this.maxRiders;
        int halfRiders = (int) Math.ceil((double)maxRiders / 2);
        if (this.passengersId.size() == maxRiders){
            return R.color.sliderRed;
        }else if (this.passengersId.size() > halfRiders){
            return R.color.sliderOrange;
        }else if (this.passengersId.size() < halfRiders){
            return R.color.sliderGreen;
        }else {
            return R.color.sliderYellow;
        }
    }
    public boolean isFull(){
        return this.maxRiders == this.passengersId.size();
    }
}
