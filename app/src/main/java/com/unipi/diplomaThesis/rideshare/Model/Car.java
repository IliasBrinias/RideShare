package com.unipi.diplomaThesis.rideshare.Model;

import java.io.Serializable;

public class Car implements Serializable {
    private String year;
    private String make;
    private String model;
    private String carPlates;

    public Car() {
    }

    public Car(String model,String make,  String year, String carPlates) {
        this.year = year;
        this.make = make;
        this.model = model;
        this.carPlates = carPlates;
    }

    public String getCarPlates() {
        return carPlates;
    }

    public void setCarPlates(String carPlates) {
        this.carPlates = carPlates;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

}
