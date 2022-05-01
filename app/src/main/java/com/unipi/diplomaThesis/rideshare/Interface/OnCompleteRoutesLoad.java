package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Route;

import java.util.List;

public interface OnCompleteRoutesLoad {
    void returnedRoutes(List<Route> routes);
}
