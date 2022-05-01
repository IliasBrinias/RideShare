package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.unipi.diplomaThesis.rideshare.MapsFragment;
import com.unipi.diplomaThesis.rideshare.Model.RouteLatLng;
import com.unipi.diplomaThesis.rideshare.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

public class DriverSaveRouteDirectionsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the mapFragmentView initialization parameters, e.g. ARG_ITEM_NUMBER
    private final String apiKey = getString(R.string.android_api_key);
    String startPlaceID, endPlaceId;
    LatLng startingPoint, endPoint;
    private TableRow finishRow;
    private Button nextStep;
    private TextView title;
    private String TAG = "AUTOCOMPLETE";
    DriverSaveRouteFragment parentFragment;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    DriverSaveRouteDirectionsFragment driverSaveRouteDirectionsFragment;
    public DriverSaveRouteDirectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    View mapFragmentView;
    MapsFragment mapsFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this mapFragmentView
        View v = inflater.inflate(R.layout.driver_route_directions_fragment, container, false);
        finishRow = v.findViewById(R.id.tableRowFinishPoint);
        finishRow.setVisibility(View.GONE);
        title = v.findViewById(R.id.textViewSaveRouteTitle);
        nextStep = v.findViewById(R.id.buttonNextStepDirections);
        mapFragmentView = v.findViewById(R.id.fragmentContainerViewMap);
        nextStep.setOnClickListener(view -> parentFragment.addStep());
        parentFragment = (DriverSaveRouteFragment) DriverSaveRouteDirectionsFragment.this.getParentFragment();

        AutocompleteSupportFragment autocompleteFragmentStarting = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragmentStarting);
        AutocompleteSupportFragment autocompleteFragmentFinish = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragmentFinish);
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), apiKey);
        }
        mapsFragment = new MapsFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragmentContainerViewMap,mapsFragment).commit();
//        hints for the fragments
        autocompleteFragmentStarting.setHint(getString(R.string.starting_route));
        autocompleteFragmentFinish.setHint(getString(R.string.finish_route));
//        returned fields
        List<Place.Field> returnedFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG);
        autocompleteFragmentStarting.setPlaceFields(returnedFields);
        autocompleteFragmentFinish.setPlaceFields(returnedFields);
//        listeners for places
        autocompleteFragmentStarting.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                startingPoint = place.getLatLng();
                startPlaceID = place.getId();
                mapsFragment.addSpot(place);
                if (endPoint!=null){
                    drawDirections();
                }else {
                    title.setText(R.string.set_your_finish_point);
                    finishRow.setVisibility(View.VISIBLE);
                    parentFragment.addStep();
                }
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        autocompleteFragmentFinish.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                endPlaceId = place.getId();
                endPoint = place.getLatLng();
                mapsFragment.addSpot(place);
                drawDirections();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        return v;
    }
    Double distance;
    public void drawDirections(){
        GoogleDirection.withServerKey(apiKey)
                .from(startingPoint)
                .to(endPoint)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(@Nullable Direction direction) {
                        if (startingPoint!=null&&endPoint!=null){
                            mapsFragment.drawDirection(startingPoint,endPoint, mapFragmentView.getWidth(), mapFragmentView.getHeight(),direction);
                            nextStep.setVisibility(View.VISIBLE);
                            title.setText(getResources().getString(R.string.next_step_title));
                            distance = direction.getRouteList().get(0).getLegList().get(0).getDistance().getValue()/1000.;
                        }
                    }
                    @Override
                    public void onDirectionFailure(@NonNull Throwable t) {
                    }
                });
    }
    public RouteLatLng getPoints(){
        return new RouteLatLng(startPlaceID,startingPoint.latitude,
                startingPoint.longitude,
                endPlaceId,
                endPoint.latitude,
                endPoint.longitude,
                distance);
    }
}