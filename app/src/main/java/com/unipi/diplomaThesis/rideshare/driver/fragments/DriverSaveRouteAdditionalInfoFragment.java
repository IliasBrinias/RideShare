package com.unipi.diplomaThesis.rideshare.driver.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.DriverSaveRouteActivity;
import com.unipi.diplomaThesis.rideshare.driver.adapter.EditPassengersAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverSaveRouteAdditionalInfoFragment extends Fragment implements TextWatcher {

    TextInputLayout routeNameLayout, costLayout, capacityLayout, maximumDeviationLayout;
    TableRow tableRowPassengers;
    EditText routeName, cost, capacity, maximumDeviation;
    Button saveRoute, buttonSubmitEditedRoute;
    TableRow tableRowEditRoute;
    ImageButton previous;
    RecyclerView recyclerView;
    EditPassengersAdapter editPassengersAdapter;
    List<String> passengersId;
    List<String> deletedPassengers=new ArrayList<>();
    TextView titleDeletePassengers;
    Driver driver;
    double routeMaxDeviation=0;
    int routeCapacity = 0;
    Double routeCost=0.;
    String name="";

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
        try {
            driver = (Driver) User.loadUserInstance(getActivity());
        }catch (ClassCastException classCastException){
            getActivity().finish();
        }
        tableRowPassengers = v.findViewById(R.id.tableRowPassengers);
        routeName = v.findViewById(R.id.textInputUserName);
        routeNameLayout = v.findViewById(R.id.textInputUserNameLayout);
        routeName.addTextChangedListener(this);
        cost = v.findViewById(R.id.textInputPassengersCost);
        costLayout = v.findViewById(R.id.textInputPassengersCostLayout);
        cost.addTextChangedListener(this);

        capacity = v.findViewById(R.id.textInputRideCapacity);
        capacityLayout = v.findViewById(R.id.textInputCapacityLayout);
        capacity.addTextChangedListener(this);

        maximumDeviation = v.findViewById(R.id.textInputMaximumDeviation);
        maximumDeviationLayout = v.findViewById(R.id.textInputMaximumDeviationLayout);
        maximumDeviation.addTextChangedListener(this);

        recyclerView = v.findViewById(R.id.recyclerViewPassengers);
        previous = v.findViewById(R.id.imageViewPreviousStep);
        saveRoute = v.findViewById(R.id.buttonSaveRoute);
        buttonSubmitEditedRoute = v.findViewById(R.id.buttonSubmitEditedRoute);
        tableRowEditRoute = v.findViewById(R.id.tableRowEditRoute);
        titleDeletePassengers = v.findViewById(R.id.titleDeletePassengers);
        previous.setOnClickListener(view->((DriverSaveRouteActivity)getActivity()).previousStep());
        saveRoute.setOnClickListener(view-> {
            List<EditText> editTexts = new ArrayList<>();
            editTexts.add(routeName); editTexts.add(cost);
            editTexts.add(capacity); editTexts.add(maximumDeviation);

            if (!User.checkIfEditTextIsNull(getActivity(),editTexts)){
                ((DriverSaveRouteActivity) getActivity()).saveRoute();
            }
        });
        buttonSubmitEditedRoute.setOnClickListener(view -> {
            List<EditText> editTexts = new ArrayList<>();
            editTexts.add(routeName); editTexts.add(cost);
            editTexts.add(capacity); editTexts.add(maximumDeviation);

            if (!User.checkIfEditTextIsNull(getActivity(),editTexts)){
                ((DriverSaveRouteActivity) getActivity()).editRoute();
            }
        });
        if (routeMaxDeviation!=0 || routeCapacity!=0 || routeCost!=0 || !name.equals("")){
            tableRowEditRoute.setVisibility(View.VISIBLE);
            saveRoute.setVisibility(View.GONE);
            maximumDeviation.setText(String.valueOf(routeMaxDeviation));
            capacity.setText(String.valueOf(routeCapacity));
            cost.setText(String.valueOf(routeCost));
            routeName.setText(name);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
            recyclerView.setLayoutManager(linearLayoutManager);
            editPassengersAdapter = new EditPassengersAdapter(getActivity(),passengersId, ((view, position) -> deletePassengerDialog(passengersId.get(position))));
            recyclerView.setAdapter(editPassengersAdapter);
            if (passengersId.size() == 0) {
                tableRowPassengers.setVisibility(View.GONE);
            }else {
                tableRowPassengers.setVisibility(View.VISIBLE);
            }
        }
        return v;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deletePassengerDialog(String passengerId){
        final View dialogView = View.inflate(getActivity(), R.layout.delete_passenger_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.tablerow_background));
        View passengerInfo = dialogView.findViewById(R.id.passengerView);
        ImageView userImage = passengerInfo.findViewById(R.id.imageViewProfile);
        TextView userName = passengerInfo.findViewById(R.id.textViewUserName);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        passengerInfo.findViewById(R.id.imageButtonDelete).setVisibility(View.GONE);
        User.loadUser(passengerId, u -> {
            userName.setText(u.getFullName());
            u.loadUserImage(image -> {
                userImage.setImageBitmap(null);
                userImage.setBackgroundResource(0);
                if (image!=null){
                    userImage.setImageBitmap(image);
                }else {
                    userImage.setBackgroundResource(R.drawable.ic_default_profile);
                }
            });
        });
        dialogView.findViewById(R.id.deleteAccount).setOnClickListener(view -> {
           alertDialog.setCancelable(false);
           progressBar.setIndeterminate(true);
           progressBar.setVisibility(View.VISIBLE);
            driver.deletePassenger(
                   ((DriverSaveRouteActivity) getActivity()).getRoute().getRouteId(),
                   passengerId, task -> {
                       if (task.isSuccessful()){
                           Toast.makeText(getActivity(), getActivity().getString(R.string.passenger_deleted_success), Toast.LENGTH_SHORT).show();
                       }else {
                           Toast.makeText(getActivity(), getActivity().getString(R.string.something_happened), Toast.LENGTH_SHORT).show();

                       }
                        progressBar.setIndeterminate(true);
                        progressBar.setVisibility(View.GONE);
                       alertDialog.dismiss();
                   }
           );
            passengersId.remove(passengerId);
            if (passengersId.size() == 0) titleDeletePassengers.setVisibility(View.GONE);
            editPassengersAdapter.notifyDataSetChanged();
            alertDialog.dismiss();
        });
        dialogView.findViewById(R.id.textViewCancel).setOnClickListener(view -> alertDialog.dismiss());

        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    public Map<String,Object> getAdditionalInfo(){
        try{
            Map<String, Object> additionalInfo = new HashMap<>();
            additionalInfo.put("maximumDeviation", Double.parseDouble(maximumDeviation.getText().toString()));
            additionalInfo.put("rideCapacity", Integer.parseInt(capacity.getText().toString()));
            additionalInfo.put("costPreRider", Double.parseDouble(cost.getText().toString()));
            additionalInfo.put("name", routeName.getText().toString());
            if (passengersId != null) additionalInfo.put("passengersId", passengersId);
            if (passengersId != null) additionalInfo.put("deletedPassengers", deletedPassengers);
            return additionalInfo;
        }catch (NullPointerException nullPointerException){
            return null;
        }
    }
    public void setAdditionalInfo(double routeMaxDeviation, int routeCapacity, double routeCost, String name, ArrayList<String> passengersId){
        this.routeMaxDeviation = routeMaxDeviation;
        this.routeCapacity = routeCapacity;
        this.routeCost = routeCost;
        this.name = name;
        this.passengersId = passengersId;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {
        if (!maximumDeviation.getText().toString().equals("") &&
                !capacity.getText().toString().equals("") &&
                !cost.getText().toString().equals("") &&
                !routeName.getText().toString().equals("")){

            displaySubmit(View.VISIBLE);
        }else {
            displaySubmit(View.GONE);

        }
    }
    private void displaySubmit(int visibility){
        if (routeMaxDeviation!=0 && routeCapacity!=0 && !routeCost.equals("") && !name.equals("")){
            tableRowEditRoute.setVisibility(visibility);
        }else {
            saveRoute.setVisibility(visibility);
        }
    }
}