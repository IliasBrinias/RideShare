package com.unipi.diplomaThesis.rideshare.rider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.unipi.diplomaThesis.rideshare.Interface.OnPlacesApiResponse;
import com.unipi.diplomaThesis.rideshare.Model.ApiCalls;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RouteSearchFragment extends Fragment implements TextWatcher {


    public RouteSearchFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private static final String REQ_LAST_LOCATIONS = "lastLocation";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
    private Toast t;
    private TextView welcomeTitle;
    private AutoCompleteTextView autoCompleteStartPoint,autoCompleteFinishPoint, autoCompleteDate;
    private TableRow tableRowRideHistory,tableRowStartPoint,tableRowFinishPoint,tableRowDate;
    private JSONArray locations;
    private static JSONObject startLocation,finishLocation;
    private long routeDateUnix=-1;
    private ListView listViewSearchedLocation, listViewLastLocations;
    private JSONArray lastLocations = new JSONArray();
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_route_search, container, false);
        User u = User.loadUserInstance(getActivity());
        if (u==null) getActivity().finish();
        welcomeTitle = v.findViewById(R.id.textViewWelcomeTitle);
        String[] name =u.getFullName().split(" ");
        if (name.length!=2){
            welcomeTitle.setText("Hello "+u.getFullName()+"!");
        }else {
            welcomeTitle.setText("Hello "+name[0]+"!");
        }
//        TableRows
        tableRowStartPoint = v.findViewById(R.id.tableRowStartPoint);
        tableRowFinishPoint = v.findViewById(R.id.tableRowFinishPoint);
        tableRowDate = v.findViewById(R.id.tableRowDate);
        tableRowRideHistory = v.findViewById(R.id.tableRowRideHistory);
//        autoCompletes
        autoCompleteStartPoint = v.findViewById(R.id.autoCompleteEmail);
        autoCompleteFinishPoint = v.findViewById(R.id.autoCompleteFinishPoint);
        autoCompleteDate = v.findViewById(R.id.autoCompleteDateRoute);
//        listViews
        listViewSearchedLocation = v.findViewById(R.id.listView);
        listViewLastLocations = v.findViewById(R.id.listViewRideHistory);
//        GONE some tableRows
        tableRowFinishPoint.setVisibility(View.GONE);
        tableRowDate.setVisibility(View.GONE);
        autoCompleteStartPoint.setText("");
        autoCompleteFinishPoint.setText("");
        autoCompleteDate.setText("");
        startLocation = null;
        finishLocation = null;
        routeDateUnix = 0;
        listViewSearchedLocation.setVisibility(View.GONE);
        listViewLastLocations.setVisibility(View.VISIBLE);
//        Prepare rest Elements
        autoCompleteDate.setOnClickListener(this::setDateTime);
        autoCompleteStartPoint.requestFocus();
        autoCompleteStartPoint.addTextChangedListener(this);
        autoCompleteFinishPoint.addTextChangedListener(this);
        AdapterView.OnItemClickListener clickedLocationListener = (adapterView, view, position, l) -> {
            try {
                if (autoCompleteStartPoint.isFocused()){
                    startLocation = completeTextView(adapterView,position);
                    autoCompleteStartPoint.setText(startLocation.getString("formatted_address"));
                    tableRowFinishPoint.setVisibility(View.VISIBLE);
                    listViewSearchedLocation.setVisibility(View.GONE);
                    autoCompleteFinishPoint.requestFocus();
                }else if (autoCompleteFinishPoint.isFocused()){
                    finishLocation = completeTextView(adapterView, position);
                    autoCompleteFinishPoint.setText(finishLocation.getString("formatted_address"));
                    if (autoCompleteFinishPoint.getText().toString().equals(autoCompleteStartPoint.getText().toString())){
                        if (t!=null) t.cancel();
                        t.makeText(getActivity(), getActivity().getResources().getString(R.string.same_start_finish_point_error),Toast.LENGTH_SHORT).show();
                        autoCompleteFinishPoint.setText("");
                        finishLocation = null;
                        return;
                    }
                    tableRowDate.setVisibility(View.VISIBLE);
                    listViewSearchedLocation.setVisibility(View.GONE);
                    autoCompleteDate.requestFocus();
                    autoCompleteDate.performClick();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };
        listViewSearchedLocation.setOnItemClickListener(clickedLocationListener);
        listViewLastLocations.setOnItemClickListener(clickedLocationListener);
        loadLastLocation();
        return v;
    }
    private JSONObject completeTextView(AdapterView<?> adapterView, int position) throws JSONException {
        JSONObject currentJsonObject;
        if (adapterView.getId() == R.id.listView){
            currentJsonObject = locations.getJSONObject(position);
//            check if the current location exists
            if (checkIfLocationExists(lastLocations,currentJsonObject)) return currentJsonObject;
//            Set the values
            lastLocations.put(currentJsonObject);
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(REQ_LAST_LOCATIONS, lastLocations.toString()).apply();
            loadLastLocation();
        }else if (adapterView.getId() == R.id.listViewRideHistory){
            currentJsonObject = lastLocations.getJSONObject(position);
        }else {
            return null;
        }
        return currentJsonObject;
    }
    private void openRiderRouteActivity(){
        Intent i = new Intent(getActivity(),RiderRouteActivity.class);
        i.putExtra("originLocation", startLocation.toString());
        i.putExtra("destinationLocation", finishLocation.toString());
        i.putExtra("date", routeDateUnix);
        startActivity(i);
    }
    private boolean checkIfLocationExists(JSONArray savedLocations,JSONObject newLocation){
        for (int i=0; i<savedLocations.length(); i++){
            try {
                if (savedLocations.getJSONObject(i).getString("place_id").equals(newLocation.getString("place_id")))
                {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void afterTextChanged(Editable editable) {
//        Call to PlacesApi and handle the returned Data
        try {
            if (startLocation.getString("formatted_address").equals(editable.toString())){
                listViewSearchedLocation.setVisibility(View.GONE);
                return;
            }else if (finishLocation.getString("formatted_address").equals(editable.toString())){
                listViewSearchedLocation.setVisibility(View.GONE);
                return;
            }
        }catch (Exception ignored){}
        ApiCalls.getLocationPlaces(getActivity(), editable.toString(), new OnPlacesApiResponse() {
            @Override
            public void results(JSONArray locations) {
                RouteSearchFragment.this.locations = locations;
                List<String> streetNames = new ArrayList<>();
                for (int i=0; i<locations.length(); i++){
                    try {
                        streetNames.add(locations.getJSONObject(i).getString("formatted_address"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (editable.toString().length()>0) {
                    listViewSearchedLocation.setVisibility(View.VISIBLE);
                    tableRowRideHistory.setVisibility(View.GONE);
                }else {
                    listViewSearchedLocation.setVisibility(View.GONE);
                    if (lastLocations.length()>0){
                        tableRowRideHistory.setVisibility(View.VISIBLE);
                    }
                }
                try {
                    listViewSearchedLocation.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(),
                            R.layout.list_item,
                            streetNames.toArray(new String[streetNames.size()])));
                }catch (Exception ignore){
                    listViewSearchedLocation.setVisibility(View.GONE);
                    tableRowRideHistory.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void loadLastLocation(){
        String serializedObject =  PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(REQ_LAST_LOCATIONS, "");
        if (serializedObject.equals("")){
            tableRowRideHistory.setVisibility(View.GONE);
            return;
        }
        if (tableRowRideHistory.getVisibility()==View.GONE){
            tableRowRideHistory.setVisibility(View.VISIBLE);
        }
        try {
            lastLocations = new JSONArray(serializedObject);
            List<String> streetNames = new ArrayList<>();
            for (int i = 0; i< lastLocations.length(); i++){
                try {
                    streetNames.add(lastLocations.getJSONObject(i).getString("formatted_address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            listViewLastLocations.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(),
                    R.layout.list_item,
                    streetNames.toArray(new String[streetNames.size()])));
        }catch (Exception e){
            e.printStackTrace();
            tableRowRideHistory.setVisibility(View.GONE);
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove(REQ_LAST_LOCATIONS).apply();
        }
    }

    public void setDateTime(View view){
        //load the custom date Picker to the alertDialog
        final View dialogView = View.inflate(getActivity(), R.layout.date_time_picker, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.alert_dialog_background));
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(new Date().getTime());
        datePicker.setMinDate(calendar.getTimeInMillis());
        calendar.add(Calendar.YEAR,1);
        datePicker.setMaxDate(calendar.getTimeInMillis());
        timePicker.setVisibility(View.GONE);
        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (datePicker.getVisibility()==View.VISIBLE){
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.VISIBLE);
                }else if (timePicker.getVisibility()==View.VISIBLE){
                    Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth(),
                            timePicker.getHour(),
                            timePicker.getMinute());
                    routeDateUnix = calendar.getTimeInMillis();
                    autoCompleteDate.setText(simpleDateFormat.format(routeDateUnix));
                    openRiderRouteActivity();
                    alertDialog.dismiss();
                }
            }
        });
        dialogView.findViewById(R.id.date_time_cancel).setOnClickListener(view1 -> alertDialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
    @Override
    public void onResume() {
        super.onResume();
        autoCompleteStartPoint.setText("");
        autoCompleteFinishPoint.setText("");
        autoCompleteDate.setText("");
        startLocation = null;
        finishLocation = null;
        routeDateUnix = 0;
        listViewSearchedLocation.setVisibility(View.GONE);
        listViewLastLocations.setVisibility(View.VISIBLE);
        tableRowFinishPoint.setVisibility(View.GONE);
        tableRowDate.setVisibility(View.GONE);
        autoCompleteStartPoint.requestFocus();
    }
}