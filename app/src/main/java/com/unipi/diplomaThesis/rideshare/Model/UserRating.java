package com.unipi.diplomaThesis.rideshare.Model;

import java.io.Serializable;

public class UserRating implements Serializable {
    private Double kindness;
    private Double drivingSkills;
    private Double consistent;
    private String description;
    private Double finalRate;

    public UserRating(Double kindness, Double drivingSkills, Double consistent, String description, Double finalRate) {
        this.kindness = kindness;
        this.drivingSkills = drivingSkills;
        this.consistent = consistent;
        this.description = description;
        this.finalRate = finalRate;
    }

    public UserRating() {
    }

    public Double getKindness() {
        return kindness;
    }

    public void setKindness(Double kindness) {
        this.kindness = kindness;
    }

    public Double getDrivingSkills() {
        return drivingSkills;
    }

    public void setDrivingSkills(Double drivingSkills) {
        this.drivingSkills = drivingSkills;
    }

    public Double getConsistent() {
        return consistent;
    }

    public void setConsistent(Double consistent) {
        this.consistent = consistent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getFinalRate() {
        return finalRate;
    }

    public void setFinalRate(Double finalRate) {
        this.finalRate = finalRate;
    }
}
