package com.unipi.diplomaThesis.rideshare.Interface;

import org.json.JSONObject;

public interface OnDistanceResponse {
    void returnedData(JSONObject response, Double distance);
}
