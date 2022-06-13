package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Routes;

import java.util.List;

public interface OnCompleteRoutesLoad {
    void returnedRoutes(List<Routes> routes);
}
