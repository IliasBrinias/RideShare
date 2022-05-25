package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.unipi.diplomaThesis.rideshare.Interface.OnPlacesApiResponse;
import com.unipi.diplomaThesis.rideshare.Model.ApiCalls;
import com.unipi.diplomaThesis.rideshare.Model.MapDrawer;
import com.unipi.diplomaThesis.rideshare.Model.RouteLatLng;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.DriverSaveRouteActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DriverSaveRouteDirectionsFragment extends Fragment implements AdapterView.OnItemClickListener, TextWatcher, OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the mapFragmentView initialization parameters, e.g. ARG_ITEM_NUMBER
    private String apiKey;
    LatLng startingPoint, endPoint;
    private Button nextStep;
    DriverSaveRouteActivity parentActivity;
    ListView listViewLocationSearch;
    EditText originTextView, destinationTextView;
    private JSONArray locations;
    private static JSONObject originLocation, destinationLocation;
    MapView mapView;
    MapDrawer m;
    GoogleMap googleMap;

    public DriverSaveRouteDirectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this mapFragmentView
        View v = inflater.inflate(R.layout.driver_route_directions_fragment, container, false);
        listViewLocationSearch = v.findViewById(R.id.listViewLocationSearch);
        originTextView = v.findViewById(R.id.autoCompleteOriginPoint);
        destinationTextView = v.findViewById(R.id.textInputDestinationLocation);
        mapView = v.findViewById(R.id.mapView);
        originTextView.addTextChangedListener(this);
        destinationTextView.addTextChangedListener(this);
        listViewLocationSearch.setOnItemClickListener(this);
        apiKey = getActivity().getString(R.string.android_api_key);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(apiKey);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        nextStep = v.findViewById(R.id.buttonNextStepDirections);
        nextStep.setOnClickListener(view -> parentActivity.nextStep());
        nextStep.setVisibility(View.GONE);
        parentActivity = (DriverSaveRouteActivity) getActivity();
        return v;
    }
    public RouteLatLng getPoints(){
        return new RouteLatLng(
                jsonToPlaceId(originLocation),
                startingPoint.latitude,
                startingPoint.longitude,
                jsonToPlaceId(destinationLocation),
                endPoint.latitude,
                endPoint.longitude,
                m.getDistance());
    }
    Toast t;
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        try {
            if (originTextView.isFocused()){
                originLocation = locations.getJSONObject(position);
                originTextView.setText(originLocation.getString("formatted_address"));
                originTextView.clearFocus();
            }else if (destinationTextView.isFocused()){
                destinationLocation = locations.getJSONObject(position);
                destinationTextView.setText(destinationLocation.getString("formatted_address"));
                if (destinationTextView.getText().toString().equals(originTextView.getText().toString())){
                    if (t!=null) t.cancel();
                    t.makeText(getActivity(), getResources().getString(R.string.same_start_finish_point_error), Toast.LENGTH_SHORT).show();
                    destinationTextView.setText("");
                    destinationLocation = null;
                    return;
                }
                destinationTextView.clearFocus();
            }
            listViewLocationSearch.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            if (originLocation!=null && destinationLocation!=null){
                nextStep.setVisibility(View.VISIBLE);
            }else {
                nextStep.setVisibility(View.GONE);
            }
            drawMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }


    @Override
    public void afterTextChanged(Editable editable) {
//        Call to PlacesApi and handle the returned Data
        try {
            if (originLocation.getString("formatted_address").equals(editable.toString())){
                mapView.setVisibility(View.VISIBLE);
                return;
            }else if (destinationLocation.getString("formatted_address").equals(editable.toString())){
                listViewLocationSearch.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                return;
            }
        }catch (Exception ignored){}
        ApiCalls.getLocationPlaces(getActivity(), editable.toString(), new OnPlacesApiResponse() {
            @Override
            public void results(JSONArray locations) {
                DriverSaveRouteDirectionsFragment.this.locations = locations;
                List<String> streetNames = new ArrayList<>();
                for (int i=0; i<locations.length(); i++){
                    try {
                        streetNames.add(locations.getJSONObject(i).getString("formatted_address"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (editable.toString().length()>0){
                    listViewLocationSearch.setVisibility(View.VISIBLE);
                    mapView.setVisibility(View.GONE);
                }
                listViewLocationSearch.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(),
                        R.layout.list_item,
                        streetNames.toArray(new String[streetNames.size()])));
            }
        });

    }

    private LatLng jsonToLatLng(JSONObject jsonObject){
        try {
            JSONObject latLngJson = jsonObject.getJSONObject("geometry").getJSONObject("location");
            return new LatLng(latLngJson.getDouble("lat"),latLngJson.getDouble("lng"));
        }catch (JSONException jsonException){
            jsonException.printStackTrace();
            return null;
        }
    }
    private String jsonToPlaceId(JSONObject jsonObject){
        try {
            return jsonObject.getString("place_id");
        }catch (JSONException jsonException){
            jsonException.printStackTrace();
            return null;
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        m = new MapDrawer(getActivity(),googleMap);
        m.setEdgesOffsetFromTheMap(0.1);
    }

    private void drawMap(){
        if (m == null) return;
        if (destinationLocation == null){
            if (originLocation != null) {
                startingPoint =jsonToLatLng(originLocation);
                m.setOriginSpot(startingPoint);
                return;
            }
            return;
        }
        closeKeyboard(getActivity(),destinationTextView.getWindowToken());
        startingPoint =jsonToLatLng(originLocation);
        LatLng destinationPoint = jsonToLatLng(destinationLocation);
        m.directions(apiKey,startingPoint,destinationPoint,mapView);
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    public void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }
}