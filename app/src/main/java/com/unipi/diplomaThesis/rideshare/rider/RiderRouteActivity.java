package com.unipi.diplomaThesis.rideshare.rider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnPlacesApiResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnRiderRouteClickListener;
import com.unipi.diplomaThesis.rideshare.Model.ApiCalls;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.RouteFilter;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.RouteActivity;
import com.unipi.diplomaThesis.rideshare.RouteFilterActivity;
import com.unipi.diplomaThesis.rideshare.rider.adapter.RiderRouteAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class RiderRouteActivity extends AppCompatActivity implements TextWatcher {
    private RecyclerView recyclerView;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
    private TextView  routeCountTitle;
    private static List<Route> routeList = new ArrayList<>();
    private static List<User> routeDrivers = new ArrayList<>();
    private RiderRouteAdapter riderRouteAdapter;
    private Rider r;
    private JSONArray locations;
    private final int REQ_ROUTE_FILTER = 23;
    private static JSONObject originLocation, destinationLocation;
    private static String originPlaceId,destinationPlaceId;
    private static long dateTimeUnix;
    ImageView buttonFilter;
    TextView textViewFilterCount;
    AutoCompleteTextView autoCompleteOriginPoint,autoCompleteDestinationPoint,autoCompleteDate;
    TableRow tableRowFilter, tableRowLocationSearch, tableRowReturnedData;
    ListView listViewLocationSearch;
    RouteFilter routeFilter = new RouteFilter();
    Toast t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_route);
//        load the elements
        recyclerView = findViewById(R.id.recyclerView);
        autoCompleteDestinationPoint = findViewById(R.id.autoCompleteDestinationPoint);
        autoCompleteOriginPoint = findViewById(R.id.autoCompleteOriginPoint);
        autoCompleteDate = findViewById(R.id.autoCompleteDate);
        buttonFilter = findViewById(R.id.buttonFilter);
        textViewFilterCount = findViewById(R.id.textViewFilter);
        routeCountTitle = findViewById(R.id.textViewCountRoutes);
        tableRowFilter = findViewById(R.id.tableRowFilter);
        tableRowLocationSearch = findViewById(R.id.tableRowLocationSearch);
        tableRowReturnedData = findViewById(R.id.tableRowReturnData);
        listViewLocationSearch = findViewById(R.id.listViewLocationSearch);
//        reset the routeList and RouteDrivers
        routeList.clear();
        routeDrivers.clear();
        if(!getIntent().hasExtra("originLocation")
                ||!getIntent().hasExtra("destinationLocation")
                ||!getIntent().hasExtra("date")) {
            finish();
        }
        try {
            r = (Rider) User.loadUserInstance(PreferenceManager.getDefaultSharedPreferences(this));
//            getIntent Data
            originLocation = new JSONObject(getIntent().getStringExtra("originLocation"));
            destinationLocation = new JSONObject(getIntent().getStringExtra("destinationLocation"));
            dateTimeUnix = getIntent().getLongExtra("date",-1);
//            restrict the Place Id
            originPlaceId = originLocation.getString("place_id");
            destinationPlaceId = destinationLocation.getString("place_id");
//            complete AutoComplete Data
            autoCompleteOriginPoint.setText(originLocation.getString("formatted_address"));
            autoCompleteDestinationPoint.setText(destinationLocation.getString("formatted_address"));
            autoCompleteDate.setText(simpleDateFormat.format(dateTimeUnix));
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }
        //        initialize recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        routeFilter = new RouteFilter();
        routeFilter.setOriginRiderPlaceId(originPlaceId);
        routeFilter.setDestinationRiderPlaceId(destinationPlaceId);
        routeFilter.setTimeUnix(dateTimeUnix);
        r.routeSearch(this ,routeFilter, this::refreshData);
        riderRouteAdapter = new RiderRouteAdapter(this,routeList, routeDrivers, dateTimeUnix, new OnRiderRouteClickListener() {
            @Override
            public void onRouteClick(View view, int position) {
                Intent i =new Intent(RiderRouteActivity.this, RouteActivity.class);
                i.putExtra(Route.class.getSimpleName(),routeList.get(position).getRouteId());
                i.putExtra(Driver.class.getSimpleName(),routeList.get(position).getDriverId());
                i.putExtra("userDateTime",dateTimeUnix);
                startActivity(i);
            }
        });
        recyclerView.setAdapter(riderRouteAdapter);
        @SuppressLint("NotifyDataSetChanged") AdapterView.OnItemClickListener clickedLocationListener =
                (adapterView, view, position, l) -> {
            try {
                if (autoCompleteOriginPoint.isFocused()){
                    originLocation = locations.getJSONObject(position);
                    autoCompleteOriginPoint.setText(originLocation.getString("formatted_address"));
                    routeFilter.setOriginRiderPlaceId(originLocation.getString("place_id"));
                    autoCompleteOriginPoint.clearFocus();
                }else if (autoCompleteDestinationPoint.isFocused()){
                    destinationLocation = locations.getJSONObject(position);
                    autoCompleteDestinationPoint.setText(destinationLocation.getString("formatted_address"));
                    if (autoCompleteDestinationPoint.getText().toString().equals(autoCompleteOriginPoint.getText().toString())){
                        if (t!=null) t.cancel();
                        t.makeText(this, getResources().getString(R.string.same_start_finish_point_error),Toast.LENGTH_SHORT).show();
                        autoCompleteDestinationPoint.setText("");
                        destinationLocation = null;
                        return;
                    }
                    routeFilter.setOriginRiderPlaceId(destinationLocation.getString("place_id"));
                    autoCompleteDestinationPoint.clearFocus();
                }
                tableRowLocationSearch.setVisibility(View.GONE);
                tableRowReturnedData.setVisibility(View.VISIBLE);
                routeDrivers.clear();
                routeList.clear();
                riderRouteAdapter.notifyDataSetChanged();
                r.routeSearch(this ,routeFilter, this::refreshData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };
        listViewLocationSearch.setOnItemClickListener(clickedLocationListener);
        autoCompleteOriginPoint.addTextChangedListener(this);
        autoCompleteDestinationPoint.addTextChangedListener(this);
        tableRowLocationSearch.setVisibility(View.GONE);
        tableRowReturnedData.setVisibility(View.VISIBLE);
        autoCompleteDate.setOnClickListener(this::setDateTime);
//        TODO: get the destination from the previous activity
//              return the Routes and the drivers
    }
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void refreshData(Route r, User driver){
        routeList.add(r);
//        check if the driver exists
        boolean exists = false;
        for (User u:routeDrivers) {
            if (driver.getUserId().equals(u.getUserId())) {
                exists = true;
                break;
            }
        }
        if (!exists){
            routeDrivers.add(driver);
        }
        riderRouteAdapter.notifyDataSetChanged();
//        update the count if the returnData is null
        if (routeList.size()==0){
            routeCountTitle.setText(getString(R.string.we_found)+" "+1+" "+getString(R.string.route));
        }else{
            routeCountTitle.setText(getString(R.string.we_found)+" "+routeList.size()+" "+getString(R.string.routes));
        }

    }
    public void openFilter(View view){
        Intent i = new Intent(new Intent(this, RouteFilterActivity.class));
        i.putExtra(RouteFilter.class.getSimpleName(),routeFilter);
        startActivityForResult(i,REQ_ROUTE_FILTER);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQ_ROUTE_FILTER:
                if (resultCode == Activity.RESULT_OK && data != null){
                    routeFilter = (RouteFilter) data.getSerializableExtra(RouteFilter.class.getSimpleName());
                    routeList.clear();
                    routeDrivers.clear();
                    r.routeSearch(this, routeFilter, this::refreshData);
                    riderRouteAdapter.notifyDataSetChanged();
                    int countFilters = routeFilter.getFilterCount();
                    if (countFilters == 0){
                        buttonFilter.setVisibility(View.VISIBLE);
                        textViewFilterCount.setVisibility(View.GONE);
                    }else {
                        buttonFilter.setVisibility(View.GONE);
                        textViewFilterCount.setVisibility(View.VISIBLE);
                        textViewFilterCount.setText(String.valueOf(countFilters));
                    }
                }
                break;
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
                tableRowLocationSearch.setVisibility(View.GONE);
                tableRowReturnedData.setVisibility(View.VISIBLE);
                return;
            }else if (destinationLocation.getString("formatted_address").equals(editable.toString())){
                tableRowLocationSearch.setVisibility(View.GONE);
                tableRowReturnedData.setVisibility(View.VISIBLE);
                return;
            }
        }catch (Exception ignored){
            ignored.printStackTrace();
        }

        ApiCalls.getLocationPlaces(this, editable.toString(), new OnPlacesApiResponse() {
            @Override
            public void results(JSONArray locations) {
                RiderRouteActivity.this.locations = locations;
                List<String> streetNames = new ArrayList<>();
                for (int i=0; i<locations.length(); i++){
                    try {
                        streetNames.add(locations.getJSONObject(i).getString("formatted_address"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (editable.toString().length()>0){
                    tableRowLocationSearch.setVisibility(View.VISIBLE);
                    tableRowReturnedData.setVisibility(View.GONE);
                }
                listViewLocationSearch.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                        R.layout.list_item,
                        streetNames.toArray(new String[streetNames.size()])));
            }
        });

    }

    public void setDateTime(View view){
        //load the custom date Picker to the alertDialog
        final View dialogView = View.inflate(this, R.layout.date_time_picker, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).create();
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
        timePicker.setVisibility(View.GONE);
        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
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
                    dateTimeUnix = calendar.getTimeInMillis();
                    autoCompleteDate.setText(simpleDateFormat.format(dateTimeUnix));
                    alertDialog.dismiss();
                    routeDrivers.clear();
                    routeList.clear();
                    riderRouteAdapter.notifyDataSetChanged();
                    riderRouteAdapter.setDateTime(dateTimeUnix);
                    r.routeSearch(RiderRouteActivity.this ,routeFilter, RiderRouteActivity.this::refreshData);
                }
            }
        });
        dialogView.findViewById(R.id.date_time_cancel).setOnClickListener(view1 -> alertDialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

}