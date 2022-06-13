package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Routes;
import com.unipi.diplomaThesis.rideshare.Model.User;

public interface OnRouteSearchResponse {
    void returnedData(Routes routes, User driver, double distanceDeviation, float reviewTotalScore, int reviewCount);
}
