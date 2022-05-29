package com.unipi.diplomaThesis.rideshare.Model;

import java.io.Serializable;

public class RouteLatLng implements Serializable {
    private String startPlaceId;
    private String endPlaceId;
    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;
    private double distance;
    private double maximumDeviation;
    public RouteLatLng() {}


    public RouteLatLng(String startPlaceId, double startLat, double startLng, String endPlaceId, double endLat, double endLng, double distance) {
        this.startPlaceId = startPlaceId;
        this.endPlaceId = endPlaceId;
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
        this.distance = distance;
    }

    public double getMaximumDeviation() {
        return maximumDeviation;
    }

    public void setMaximumDeviation(double maximumDeviation) {
        this.maximumDeviation = maximumDeviation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getStartPlaceId() {
        return startPlaceId;
    }

    public void setStartPlaceId(String startPlaceId) {
        this.startPlaceId = startPlaceId;
    }

    public String getEndPlaceId() {
        return endPlaceId;
    }

    public void setEndPlaceId(String endPlaceId) {
        this.endPlaceId = endPlaceId;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLng() {
        return startLng;
    }

    public void setStartLng(double startLng) {
        this.startLng = startLng;
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getEndLng() {
        return endLng;
    }

    public void setEndLng(double endLng) {
        this.endLng = endLng;
    }
}
