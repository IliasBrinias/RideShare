package com.unipi.diplomaThesis.rideshare.passenger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.OnPlacesApiResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnPassengerRouteClickListener;
import com.unipi.diplomaThesis.rideshare.Model.ApiCalls;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Passenger;
import com.unipi.diplomaThesis.rideshare.Model.Routes;
import com.unipi.diplomaThesis.rideshare.Model.RouteFilter;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.Route.RouteActivity;
import com.unipi.diplomaThesis.rideshare.Route.RouteFilterActivity;
import com.unipi.diplomaThesis.rideshare.passenger.adapter.PassengerRouteAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassengerRouteActivity extends AppCompatActivity implements TextWatcher, AdapterView.OnItemClickListener {
    private RecyclerView recyclerView;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
    private TextView  routeCountTitle;
    private static List<Routes> routesList = new ArrayList<>();
    private static List<User> routeDrivers = new ArrayList<>();
    private static Map<String,Object> driverReview = new HashMap<>();
    private PassengerRouteAdapter passengerRouteAdapter;
    private Passenger r;
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
    private static RouteFilter routeFilter = new RouteFilter();
    ProgressBar progressBar;
    Toast t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_route);
//        load the elements
        recyclerView = findViewById(R.id.recyclerView);
        autoCompleteDestinationPoint = findViewById(R.id.autoCompleteDestinationPoint);
        autoCompleteOriginPoint = findViewById(R.id.autoCompleteEmail);
        autoCompleteDate = findViewById(R.id.autoCompleteDate);
        buttonFilter = findViewById(R.id.buttonFilter);
        textViewFilterCount = findViewById(R.id.textViewFilter);
        routeCountTitle = findViewById(R.id.textViewCountRoutes);
        tableRowFilter = findViewById(R.id.tableRowFilter);
        tableRowLocationSearch = findViewById(R.id.tableRowLocationSearch);
        tableRowReturnedData = findViewById(R.id.tableRowReturnData);
        listViewLocationSearch = findViewById(R.id.listViewLocationSearch);
        progressBar = findViewById(R.id.progressBar);
        tableRowLocationSearch.setVisibility(View.GONE);
        tableRowFilter.setVisibility(View.GONE);
        tableRowReturnedData.setVisibility(View.VISIBLE);

        startProgressBarAnimation();
        autoCompleteDate.setOnClickListener(this::setDateTime);
        autoCompleteOriginPoint.addTextChangedListener(this);
        autoCompleteDestinationPoint.addTextChangedListener(this);
//        reset the routesList and RouteDrivers
        routesList.clear();
        routeDrivers.clear();
        if(!getIntent().hasExtra("originLocation")
                ||!getIntent().hasExtra("destinationLocation")
                ||!getIntent().hasExtra("date")) {
            finish();
        }
        try {
            r = (Passenger) User.loadUserInstance(this);
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
        passengerRouteAdapter = new PassengerRouteAdapter(this, routesList, routeDrivers, dateTimeUnix,
                driverReview, new OnPassengerRouteClickListener() {
            @Override
            public void onRouteClick(View view, int position) {
                Intent i =new Intent(PassengerRouteActivity.this, RouteActivity.class);
                i.putExtra(Routes.class.getSimpleName(), routesList.get(position).getRouteId());
                i.putExtra(Driver.class.getSimpleName(), routesList.get(position).getDriverId());
                i.putExtra("userDateTime",dateTimeUnix);
                i.putExtra("distanceDeviation",distanceDeviationMap.get(routesList.get(position).getRouteId()));
                startActivity(i);
            }
        });
        recyclerView.setAdapter(passengerRouteAdapter);

        listViewLocationSearch.setOnItemClickListener(this);
        RouteSearch();
    }
    Map<String,Double> distanceDeviationMap = new HashMap<>();
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void refreshData(Routes r, User driver, double distanceDeviation, float reviewTotalScore, int reviewCount){
        stopProgressBarAnimation();
        if (r==null) {
            if (routesList.isEmpty()){
                routeCountTitle.setText(getString(R.string.we_found)+" "+0+" "+getString(R.string.route));
            }
            return;
        }
        if (routeFilter.getMinRating()>reviewTotalScore || routeFilter.getMaxRating()< reviewTotalScore) {
            return;
        }

        Map<String,Object> reviewData = new HashMap<>();
        reviewData.put("total",reviewTotalScore);
        reviewData.put("count",reviewCount);
        driverReview.put(r.getRouteId(),reviewData);
        routesList.add(r);
        distanceDeviationMap.put(r.getRouteId(),distanceDeviation);
        if (routeFilter.getClassification()!=routeFilter.getDefaultClassification()){
            routesList = classificationBasedRouteFilter(routesList,routeFilter.getClassification());
        }
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
        passengerRouteAdapter.notifyDataSetChanged();
//        update the count if the returnData is null
        if (routesList.size()==0){
            routeCountTitle.setText(getString(R.string.we_found)+" "+1+" "+getString(R.string.route));
        }else{
            routeCountTitle.setText(getString(R.string.we_found)+" "+ routesList.size()+" "+getString(R.string.routes));
        }

    }
    private List<Routes> classificationBasedRouteFilter(List<Routes> routesList, int typeOfClassification){
        switch (typeOfClassification){
            case 0: // asc Price
                routesList.sort(new Comparator<Routes>() {
                    @Override
                    public int compare(Routes r1, Routes r2) {
                        return Double.compare(r1.getCostPerRider(),r2.getCostPerRider());
                    }
                });
                break;
            case 1: // Dec Price
                routesList.sort(new Comparator<Routes>() {
                    @Override
                    public int compare(Routes r1, Routes r2) {
                        return Double.compare(r2.getCostPerRider(),r1.getCostPerRider());
                    }
                });
                break;
            case 2: // Based on Time
                routesList.sort(new Comparator<Routes>() {
                    @Override
                    public int compare(Routes r1, Routes r2) {
                        return Float.compare(Math.abs(r1.getMinDiff(dateTimeUnix)),Math.abs(r2.getMinDiff(dateTimeUnix)));
                    }
                });
                break;
            case 3: // Based on Reviews
                routesList.sort(new Comparator<Routes>() {
                    @Override
                    public int compare(Routes routes, Routes routes1) {
                        double routeTotalReview = (float) ((Map<String,Object>) driverReview.get(routesList.get(routesList.indexOf(routes)).getRouteId())).get("total");
                        double routeTotalReview1 = (float) ((Map<String,Object>) driverReview.get(routesList.get(routesList.indexOf(routes1)).getRouteId())).get("total");
                        return Double.compare(routeTotalReview1,routeTotalReview);
                    }
                });
                break;

        }
        return routesList;
    }
    public void openFilter(View view){
        Intent i = new Intent(new Intent(this, RouteFilterActivity.class));
        i.putExtra(RouteFilter.class.getSimpleName(),routeFilter);
        startActivityForResult(i,REQ_ROUTE_FILTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQ_ROUTE_FILTER:
                if (resultCode == Activity.RESULT_OK && data != null){
                    routeFilter = (RouteFilter) data.getSerializableExtra(RouteFilter.class.getSimpleName());
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
                RouteSearch();
                break;
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
                PassengerRouteActivity.this.locations = locations;
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
                listViewLocationSearch.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.list_item,
                        streetNames.toArray(new String[streetNames.size()])){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        TextView text = v.findViewById(R.id.textView);
                        TypedValue typedValue = new TypedValue();
                        Resources.Theme theme = getTheme();
                        theme.resolveAttribute(R.attr.listViewTextColor, typedValue, true);
                        @ColorInt int color = typedValue. data;
                        text.setTextColor(color);
                        return v;
                    }
                });
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setDateTime(View view){
        //load the custom date Picker to the alertDialog
        final View dialogView = View.inflate(this, R.layout.date_time_picker, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.alert_dialog_background));
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
        datePicker.setMinDate(new Date().getTime());
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
                    dateTimeUnix = calendar.getTimeInMillis();
                    autoCompleteDate.setText(simpleDateFormat.format(dateTimeUnix));
                    alertDialog.dismiss();
                    passengerRouteAdapter.setDateTime(dateTimeUnix);
                    routeFilter.setTimeUnix(dateTimeUnix);
                    RouteSearch();

                }
            }
        });
        dialogView.findViewById(R.id.date_time_cancel).setOnClickListener(view1 -> alertDialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.show();
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

    }
    Double min;
    Double max;

    @SuppressLint("NotifyDataSetChanged")
    private void RouteSearch() {
        startProgressBarAnimation();
        tableRowFilter.setVisibility(View.GONE);
        passengerRouteAdapter.notifyDataSetChanged();
        routeDrivers.clear();
        routesList.clear();
        r.routeSearch(PassengerRouteActivity.this ,routeFilter, PassengerRouteActivity.this::refreshData);
        tableRowFilter.setVisibility(View.GONE);
        min = -1.;
        max = -1.;

        r.findMinMaxPrice(returnData -> {
//            find Price min max
            if (returnData.get("min")!=null){
                min = (double) returnData.get("min");
            }else if (returnData.get("max")!=null){
                max = (double) returnData.get("max");
            }
            if (min==-1f || max == -1f) return;
            routeFilter.setDefaultMaxPrice(max);
            routeFilter.setDefaultMinPrice(min);
            tableRowFilter.setVisibility(View.VISIBLE);
        });

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
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
            RouteSearch();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void startProgressBarAnimation(){
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }
    private void stopProgressBarAnimation(){
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.GONE);
    }

}