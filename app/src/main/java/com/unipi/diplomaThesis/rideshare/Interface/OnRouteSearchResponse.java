package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;

public interface OnRouteSearchResponse {
    void returnedData(Route route, User driver, double distanceDeviation, float reviewTotalScore, int reviewCount);
}
