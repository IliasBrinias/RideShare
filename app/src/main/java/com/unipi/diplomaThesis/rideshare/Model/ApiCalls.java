package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.unipi.diplomaThesis.rideshare.Interface.CarApiResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnDistanceResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnPlacesApiResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ApiCalls {
    private static String PLACES_API_KEY = "AIzaSyCO-ylUowiKV6IECv1L6eVw9Bl8CBzhaFk";

    public static void getCar(Context c, Car searchedCar, CarApiResponse carApiResponse){
        RequestQueue queue = Volley.newRequestQueue(c);
        String url = "https://car-data.p.rapidapi.com/cars";
        String CAR_API_KEY = "d8455cfac5mshb2e12524fc60827p13bf2fjsna477236d177e";
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Car> suggestedCars = new ArrayList<>();
                            for (int i =0; i<response.length(); i++){
                                JSONObject carResponse = response.getJSONObject(i);
                                Car c = new Car(
                                        carResponse.getString("model"),
                                        carResponse.getString("make"),
                                        carResponse.getString("year"),
                                        null);
                                suggestedCars.add(c);
                            }
                            carApiResponse.returnRoutes(suggestedCars);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                },
                new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(this.getClass().getSimpleName()+".CarCall",error.toString());
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("year",searchedCar.getYear());
                params.put("make",searchedCar.getMake());
                params.put("model",searchedCar.getModel());
                return params;
            }
            @Override
            public Map<String, String> getHeaders(){
                Map<String,String> params = new HashMap<>();
                params.put("x-rapidapi-host","car-data.p.rapidapi.com");
                params.put("x-rapidapi-key", CAR_API_KEY);
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }
    public static void getLocationPlaces(Context c, String address, OnPlacesApiResponse onPlacesApiResponse) {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?" +
                "key=" + PLACES_API_KEY +
                "&query=" + address +
                "&language="+ Locale.getDefault().toLanguageTag();
        RequestQueue queue = Volley.newRequestQueue(c);

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url,
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            onPlacesApiResponse.results((JSONArray) response.get("results"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                })
        ;
        queue.add(jsonArrayRequest);
    }
    public static void getDistanceWithWaypoints(Context c, String originPlaceId, String destinationPlaceId, String startWaypointPlaceId, String destinationWaypointPlaceId, OnDistanceResponse onDistanceResponse){
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "key=" + PLACES_API_KEY +
                "&origin=place_id:"+originPlaceId+
                "&destination=place_id:"+destinationPlaceId+
                "&waypoints=via:place_id:"+startWaypointPlaceId+"|place_id:"+destinationWaypointPlaceId;
        RequestQueue queue = Volley.newRequestQueue(c);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url,
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int distance = 0;
                            for (int i=0; i<response.getJSONArray("routes").length();i++){
                                distance += response.getJSONArray("routes")
                                        .getJSONObject(i)
                                        .getJSONArray("legs")
                                        .getJSONObject(0)
                                        .getJSONObject("distance")
                                        .getInt("value");

                            }
                            onDistanceResponse.returnedData(response,distance/1000.);
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }
}
