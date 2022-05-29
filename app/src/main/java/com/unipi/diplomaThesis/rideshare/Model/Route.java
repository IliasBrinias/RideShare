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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Route implements Serializable {
    private String name;
    private String routeId;
    private String driverId;
    private RouteLatLng routeLatLng;
    private ArrayList<String> passengersId = new ArrayList<>();
    private RouteDateTime routeDateTime;
    private String costPerRider;
    private int rideCapacity;

    public Route(String name, String routeId, String driverId, RouteLatLng routeLatLng, ArrayList<String> passengersId, RouteDateTime routeDateTime, String costPerRider, int rideCapacity) {
        this.name = name;
        this.routeId = routeId;
        this.driverId = driverId;
        this.routeLatLng = routeLatLng;
        this.passengersId = passengersId;
        this.routeDateTime = routeDateTime;
        this.costPerRider = costPerRider;
        this.rideCapacity = rideCapacity;
    }

    public Route() {
    }

    public int getRideCapacity() {
        return rideCapacity;
    }

    public void setRideCapacity(int rideCapacity) {
        this.rideCapacity = rideCapacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ArrayList<String> getPassengersId() {
        return passengersId;
    }

    public void setPassengersId(ArrayList<String> passengersId) {
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
        Calendar route = new GregorianCalendar();
        Calendar rider = new GregorianCalendar();
        Calendar newRoute = new GregorianCalendar();
        Calendar newRider = new GregorianCalendar();

        route.setTimeInMillis(this.getRouteDateTime().getStartDateUnix());
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
        if (passengersId == null) return 0;
        int halfRiders = (int) Math.ceil((double)this.rideCapacity / 2);
        if (this.passengersId.size() == this.rideCapacity){
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
        if (passengersId == null) return false;
        return this.rideCapacity == this.passengersId.size();
    }
}
