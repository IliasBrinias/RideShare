package com.unipi.diplomaThesis.rideshare.driver.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private Button submitStep,buttonSubmitEditedRoute;
    private ImageButton nextStep;
    DriverSaveRouteActivity parentActivity;
    ListView listViewLocationSearch;
    EditText originTextView, destinationTextView;
    private JSONArray locations;
    private static JSONObject originLocation, destinationLocation;
    MapView mapView;
    MapDrawer m;
    GoogleMap googleMap;
    TableRow tableRowEditRoute;
    LinearLayout linearLayout;
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
        linearLayout = v.findViewById(R.id.linearLayout);
        listViewLocationSearch = v.findViewById(R.id.listViewLocationSearch);
        originTextView = v.findViewById(R.id.autoCompleteOrigin);
        buttonSubmitEditedRoute = v.findViewById(R.id.buttonSubmitEditedRoute);
        destinationTextView = v.findViewById(R.id.autoCompleteDestination);
        mapView = v.findViewById(R.id.mapView);
        submitStep = v.findViewById(R.id.buttonSubmitStepDirections);
        nextStep = v.findViewById(R.id.imageViewNextStep);
        tableRowEditRoute = v.findViewById(R.id.tableRowEditRoute);
        tableRowEditRoute.setVisibility(View.GONE);
        apiKey = getActivity().getString(R.string.android_api_key);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(apiKey);
        }
        mapView.onCreate(mapViewBundle);
        buttonSubmitEditedRoute.setOnClickListener(view -> parentActivity.editRoute());
        submitStep.setOnClickListener(view -> parentActivity.nextStep());
        nextStep.setOnClickListener(view -> parentActivity.nextStep());
        nextStep.setVisibility(View.GONE);
        submitStep.setVisibility(View.GONE);
        parentActivity = (DriverSaveRouteActivity) getActivity();


        originTextView.addTextChangedListener(this);
        originTextView.requestFocus();
        destinationTextView.addTextChangedListener(this);
        listViewLocationSearch.setOnItemClickListener(this);
        originTextView.setOnFocusChangeListener(this);
        destinationTextView.setOnFocusChangeListener(this);
        mapView.getMapAsync(this);
        return v;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        m = new MapDrawer(getActivity(),googleMap);
        m.setEdgesOffsetFromTheMap(0.2);
        if (originLocation==null & destinationLocation==null & routeLatLng!=null) {
            loadRoute();
        }
    }

    private void loadRoute(){
        if (routeLatLng!=null){
            submitStep.setVisibility(View.GONE);
            tableRowEditRoute.setVisibility(View.VISIBLE);
            nextStep.setVisibility(View.VISIBLE);
            if (startingPoint==null || destinationPoint == null) return;
            loadPlaceId();

            mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            m.directions(apiKey,startingPoint,destinationPoint,mapView);
                            m.zoomToDirection(mapView,startingPoint,destinationPoint);
                        }
                    });
        }
    }
    private void loadPlaceId(){
        ApiCalls.getLocationPlaceId(getActivity(), new LatLng(routeLatLng.getStartLat(),routeLatLng.getStartLng()) , new OnPlacesApiResponse() {
            @Override
            public void results(JSONArray locations) {
                try {
                    originLocation = locations.getJSONObject(0);
                    originTextView.setText(originLocation.getString("formatted_address"));
                    listViewLocationSearch.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        ApiCalls.getLocationPlaceId(getActivity(), new LatLng(routeLatLng.getEndLat(),routeLatLng.getEndLng()) , new OnPlacesApiResponse() {
            @Override
            public void results(JSONArray locations) {
                try {
                    destinationLocation = locations.getJSONObject(0);
                    destinationTextView.setText(destinationLocation.getString("formatted_address"));
                    listViewLocationSearch.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

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
            if (routeLatLng==null) {
                if (originLocation != null && destinationLocation != null) {
                    submitStep.setVisibility(View.VISIBLE);
                } else {
                    submitStep.setVisibility(View.GONE);
                }
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
        try {
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
                    listViewLocationSearch.setAdapter(new ArrayAdapter<String>(getActivity(),
                            R.layout.list_item,
                            streetNames.toArray(new String[streetNames.size()])){
                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View v = super.getView(position, convertView, parent);
                            TextView text = v.findViewById(R.id.textView);
                            TypedValue typedValue = new TypedValue();
                            Resources.Theme theme = getActivity().getTheme();
                            theme.resolveAttribute(R.attr.listViewTextColor, typedValue, true);
                            @ColorInt int color = typedValue. data;
                            text.setTextColor(color);
                            return v;
                        }
                    });
                }
            });
        }catch (Exception ignore){}

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
        if (startingPoint==null || destinationPoint == null) return;
        m.directions(apiKey,jsonToLatLng(originLocation),jsonToLatLng(destinationLocation),mapView);
        m.zoomToDirection(mapView,startingPoint,destinationPoint);
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        if (routeLatLng!=null & originLocation!=null & destinationLocation!=null){
            loadRoute();
        }
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
        if (view.getId()==originTextView.getId()){
            linearLayout.removeView(listViewLocationSearch);
            linearLayout.addView(listViewLocationSearch,1);
        }else if (view.getId()==destinationTextView.getId()){
            linearLayout.removeView(listViewLocationSearch);
            linearLayout.addView(listViewLocationSearch,2);
        }
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
    RouteLatLng routeLatLng;
    public void setPoints(RouteLatLng routeLatLng){
        this.routeLatLng = routeLatLng;
        startingPoint = new LatLng(routeLatLng.getStartLat(),routeLatLng.getStartLng());
        destinationPoint = new LatLng(routeLatLng.getEndLat(),routeLatLng.getEndLng());

    }

}