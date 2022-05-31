package com.unipi.diplomaThesis.rideshare.Model;

import com.google.firebase.database.FirebaseDatabase;

public class Request {
    private String routeId;
    private String riderId;
    private String description;
    private long timestamp;
    private boolean seen=false;
    private double distanceDeviation;

    public Request(String routeId, String riderId, String description, long timestamp, double distanceDeviation, boolean seen) {
        this.routeId = routeId;
        this.riderId = riderId;
        this.description = description;
        this.timestamp = timestamp;
        this.distanceDeviation = distanceDeviation;
        this.seen = seen;
    }

    public Request() {
    }

    public double getDistanceDeviation() {
        return distanceDeviation;
    }

    public void setDistanceDeviation(double distanceDeviation) {
        this.distanceDeviation = distanceDeviation;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void makeSeen() {
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .child(this.getRouteId())
                .child(this.riderId)
                .child("seen").setValue(true);
    }
}
