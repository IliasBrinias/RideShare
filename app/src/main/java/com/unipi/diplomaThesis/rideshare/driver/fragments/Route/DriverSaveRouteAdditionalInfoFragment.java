package com.unipi.diplomaThesis.rideshare.driver.fragments.Route;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.unipi.diplomaThesis.rideshare.R;

import java.util.HashMap;
import java.util.Map;

public class DriverSaveRouteAdditionalInfoFragment extends Fragment {

    EditText passengers, description, costPerPassenger;
    AutoCompleteTextView car;
    Button save;
    DriverSaveRouteFragment parentFragment;
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
        passengers = v.findViewById(R.id.textInputPassengers);
        description = v.findViewById(R.id.textInputDescription);
        car = v.findViewById(R.id.availableCars);
        costPerPassenger = v.findViewById(R.id.textInputPassengersCost);
//        TODO: get Drivers cars
        String[] a1 = new String[3];
        a1[0]="Fiesta";
        a1[1]="Octavia";
        a1[2]="C3";

        ArrayAdapter a = new ArrayAdapter(getActivity(),R.layout.list_item,a1);
        car.setAdapter(a);
        parentFragment = (DriverSaveRouteFragment) DriverSaveRouteAdditionalInfoFragment.this.getParentFragment();
        save = v.findViewById(R.id.buttonNextStepDateTime);
        save.setOnClickListener(view -> parentFragment.saveRoute());
        return v;
    }

    public Map<String,String> getAdditionalInfo(){
        Map<String,String> additionalInfo = new HashMap<>();
        additionalInfo.put("maxPassengers",passengers.getText().toString());
        additionalInfo.put("description",description.getText().toString());
        additionalInfo.put("car",car.getText().toString());
        additionalInfo.put("price",costPerPassenger.getText().toString());
        return additionalInfo;
    }
}