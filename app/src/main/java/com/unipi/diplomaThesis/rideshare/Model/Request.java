package com.unipi.diplomaThesis.rideshare.Model;

public class Request {
    public static String REQ_REQUEST_CODE="223";
    public static String REQ_ACCEPT_CODE="333";
    public static String REQ_DECLINE_CODE="000";

    private String routeId;
    private String riderId;
    private String description;
    private long timestamp;
    private String status;

    public Request(String routeId, String riderId, String description, long timestamp, String status) {
        this.routeId = routeId;
        this.riderId = riderId;
        this.description = description;
        this.timestamp = timestamp;
        this.status = status;
    }

    public Request() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
