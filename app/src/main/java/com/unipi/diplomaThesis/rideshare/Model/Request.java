package com.unipi.diplomaThesis.rideshare.Model;

public class Request {
    public static final String REQ_REQUEST_CODE="223";
    public static final String REQ_ACCEPT_CODE="333";
    public static final String REQ_DECLINE_CODE="000";
    public static final String REQ_UNSEEN="755";
    public static final String REQ_SEEN="946";

    private String routeId;
    private String riderId;
    private String description;
    private long timestamp;
    private String status;
    private String seen = REQ_UNSEEN;

    public Request(String routeId, String riderId, String description, long timestamp, String status) {
        this.routeId = routeId;
        this.riderId = riderId;
        this.description = description;
        this.timestamp = timestamp;
        this.status = status;
    }

    public Request() {
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
