package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;

public interface OnPreviousRouteResponse {
    void returnRoute(Route r, User u);
}
