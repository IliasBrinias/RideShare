package com.unipi.diplomaThesis.rideshare.Interface;

import com.unipi.diplomaThesis.rideshare.Model.Routes;
import com.unipi.diplomaThesis.rideshare.Model.User;

public interface OnPreviousRouteResponse {
    void returnRoute(Routes r, User u);
}
