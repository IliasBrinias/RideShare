package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.unipi.diplomaThesis.rideshare.Interface.OnDistanceResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnPlacesApiResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ApiCalls {
    private static String PLACES_API_KEY = "AIzaSyCO-ylUowiKV6IECv1L6eVw9Bl8CBzhaFk";

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
    public static void getLocationPlaceId(Context c, LatLng latLng, OnPlacesApiResponse onPlacesApiResponse) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?" +
                "key=" + PLACES_API_KEY +
                "&latlng=" + latLng.latitude +","+latLng.longitude+
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
        System.out.println(url);
        RequestQueue queue = Volley.newRequestQueue(c);
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest
                (Request.Method.GET,
                        url,
                        null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            double distance = 0;
                            JSONArray jsonArray = response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                            for (int i=0; i<jsonArray.length();i++){
                                distance += jsonArray.getJSONObject(i)
                                        .getJSONObject("distance")
                                        .getInt("value");

                            }
                            onDistanceResponse.returnedData(response,distance);
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
