package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Car;

import java.util.List;

public interface CarApiResponse {
    void returnRoutes(List<Car> cars);
}
