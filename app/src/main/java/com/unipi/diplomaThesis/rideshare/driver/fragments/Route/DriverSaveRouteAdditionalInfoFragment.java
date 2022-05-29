package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.DriverSaveRouteActivity;

import java.util.HashMap;
import java.util.Map;

public class DriverSaveRouteAdditionalInfoFragment extends Fragment {

    TextInputLayout routeNameLayout, costLayout, capacityLayout, maximumDeviationLayout;
    EditText routeName, cost, capacity, maximumDeviation;
    Button saveRoute;
    public DriverSaveRouteAdditionalInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.driver_save_route_additional_info_fragment, container, false);
        routeName = v.findViewById(R.id.textInputRouteName);
        routeNameLayout = v.findViewById(R.id.textInputRouteNameLayout);

        cost = v.findViewById(R.id.textInputPassengersCost);
        costLayout = v.findViewById(R.id.textInputPassengersCostLayout);

        capacity = v.findViewById(R.id.textInputCapacityDeviation);
        capacityLayout = v.findViewById(R.id.textInputCapacityLayout);

        maximumDeviation = v.findViewById(R.id.textInputMaximumDeviation);
        maximumDeviationLayout = v.findViewById(R.id.textInputMaximumDeviationLayout);

        saveRoute = v.findViewById(R.id.buttonSaveRoute);
        saveRoute.setOnClickListener(view-> ((DriverSaveRouteActivity) getActivity()).saveRoute());
        return v;
    }
    public Map<String,Object> getAdditionalInfo(){
        Map<String,Object> additionalInfo = new HashMap<>();
        additionalInfo.put("maximumDeviation",Double.parseDouble(maximumDeviation.getText().toString()));
        additionalInfo.put("rideCapacity",Integer.parseInt(capacity.getText().toString()));
        additionalInfo.put("costPreRider", cost.getText().toString());
        additionalInfo.put("name",routeName.getText().toString());
        return additionalInfo;
    }
}