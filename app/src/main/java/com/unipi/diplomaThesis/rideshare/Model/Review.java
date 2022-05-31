package com.unipi.diplomaThesis.rideshare.Model;

public class Review {
    private String driverId;
    private double review;
    private String description;
    private long timestamp;
    private String userId;

    public Review() {
    }

    public Review(String driverId, double review, String description, long timestamp, String userId) {
        this.driverId = driverId;
        this.review = review;
        this.description = description;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public double getReview() {
        return review;
    }

    public void setReview(double review) {
        this.review = review;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
