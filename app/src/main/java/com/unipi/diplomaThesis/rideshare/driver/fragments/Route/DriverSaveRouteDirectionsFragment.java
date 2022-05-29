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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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

public class DriverSaveRouteDirectionsFragment extends Fragment implements AdapterView.OnItemClickListener, TextWatcher, OnMapReadyCallback, View.OnFocusChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the mapFragmentView initialization parameters, e.g. ARG_ITEM_NUMBER
    private String apiKey;
    LatLng startingPoint, destinationPoint;
    private Button nextStep;
    DriverSaveRouteActivity parentActivity;
    ListView listViewLocationSearch;
    EditText originTextView, destinationTextView;
    private JSONArray locations;
    private static JSONObject originLocation, destinationLocation;
    MapView mapView;
    MapDrawer m;
    GoogleMap googleMap;
    ConstraintLayout constraintLayout;

    public DriverSaveRouteDirectionsFragment() {}
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
        originTextView = v.findViewById(R.id.autoCompleteOrigin);
        destinationTextView = v.findViewById(R.id.autoCompleteDestination);
        mapView = v.findViewById(R.id.mapView);
        nextStep = v.findViewById(R.id.buttonNextStepDirections);
        apiKey = getActivity().getString(R.string.android_api_key);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(apiKey);
        }
        mapView.onCreate(mapViewBundle);
        nextStep.setOnClickListener(view -> parentActivity.nextStep());
        nextStep.setVisibility(View.GONE);
        parentActivity = (DriverSaveRouteActivity) getActivity();
        constraintLayout = v.findViewById(R.id.constraintLayout);


        originTextView.addTextChangedListener(this);
        originTextView.requestFocus();
        destinationTextView.addTextChangedListener(this);
        listViewLocationSearch.setOnItemClickListener(this);
        originTextView.setOnFocusChangeListener(this);
        destinationTextView.setOnFocusChangeListener(this);
        mapView.getMapAsync(this);

        return v;
    }
    public RouteLatLng getPoints(){
        return new RouteLatLng(
                jsonToPlaceId(originLocation),
                startingPoint.latitude,
                startingPoint.longitude,
                jsonToPlaceId(destinationLocation),
                destinationPoint.latitude,
                destinationPoint.longitude,
                m.getDistance());
    }
    Toast t;
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        try {
            if (originTextView.isFocused()){
                originLocation = locations.getJSONObject(position);
                originTextView.setText(originLocation.getString("formatted_address"));
            }else if (destinationTextView.isFocused()){
                destinationLocation = locations.getJSONObject(position);
                destinationTextView.setText(destinationLocation.getString("formatted_address"));
            }
            listViewLocationSearch.setVisibility(View.GONE);
            if (destinationTextView.getText().toString().equals(originTextView.getText().toString())){
                if (t!=null) t.cancel();
                t.makeText(getActivity(), getResources().getString(R.string.same_start_finish_point_error), Toast.LENGTH_SHORT).show();
                if (originTextView.isFocused()){
                    originTextView.setText("");
                    originLocation = null;
                }else if (destinationTextView.isFocused()){
                    destinationTextView.setText("");
                    destinationLocation = null;
                }
                return;
            }

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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void afterTextChanged(Editable editable) {
//        Call to PlacesApi and handle the returned Data
        try {
            if (originLocation.getString("formatted_address").equals(editable.toString())){
                return;
            }else if (destinationLocation.getString("formatted_address").equals(editable.toString())){
                listViewLocationSearch.setVisibility(View.GONE);
                return;
            }
        }catch (Exception ignored){}
        if (editable.toString().length()==0){
            listViewLocationSearch.setVisibility(View.GONE);
            return;
        }
        listViewLocationSearch.setVisibility(View.VISIBLE);

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
        if (originTextView.isFocused()){
            startingPoint =jsonToLatLng(originLocation);
            m.setOriginSpot(startingPoint);
            closeKeyboard(getActivity(),originTextView.getWindowToken());
        }else if (destinationTextView.isFocused()){
            destinationPoint =jsonToLatLng(destinationLocation);
            m.setDestinationSpot(destinationPoint);
            closeKeyboard(getActivity(),destinationTextView.getWindowToken());
        }
        if (originLocation==null||destinationLocation==null) return;
        m.directions(apiKey,startingPoint,destinationPoint,mapView);
        m.zoomToDirection(mapView);
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

    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) return;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        if (view.getId()==originTextView.getId()){
            constraintSet.connect(R.id.listViewLocationSearch,ConstraintSet.TOP,R.id.textInputOriginLayout,ConstraintSet.BOTTOM,0);
            constraintSet.applyTo(constraintLayout);
        }else if (view.getId()==destinationTextView.getId()){
            constraintSet.connect(R.id.listViewLocationSearch, ConstraintSet.TOP, R.id.textInputDestinationLayout, ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(constraintLayout);
        }
    }
}