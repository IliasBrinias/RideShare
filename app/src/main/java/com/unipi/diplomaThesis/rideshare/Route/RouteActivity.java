package com.unipi.diplomaThesis.rideshare.Route;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.Slider;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Request;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String apiKey;
    private double EDGES_OFFSET_FOR_MAP = 0.05;
    private BottomSheetBehavior mBottomSheetBehavior;
    private MarkerOptions startMarker;
    private MarkerOptions destinationMarker;
    private PolylineOptions directions;
    ImageView circleImageDriver, imageViewClose;
    TableRow tableRowShowRepeat;
    TextView driverName,
            originRoute,
            destinationRoute,
            timeRoute,
            timeDifference,
            carName,
            carPlate,
            carYear,
            ratingAverage,
            ratingCount,
            sliderRiderCapacityFrom,
            sliderRiderCapacityTo;
    Slider sliderRiderCapacity;
    RatingBar ratingBar;
    Button contactDriver;
    MapView mapView;
    Route r;
    Driver driverUser;
    View boundsOfTheMap, bottomSheet;
    String routeId, driverId;
    long userDateTime=0;
    Rider rider;
    GoogleMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        try {
            rider = (Rider) User.loadUserInstance(this);
        }catch (ClassCastException c){
            finish();
        }
        apiKey = getString(R.string.android_api_key);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(apiKey);
        }
        if (!getIntent().hasExtra(Route.class.getSimpleName()) ||
                !getIntent().hasExtra(Driver.class.getSimpleName()) ||
                !getIntent().hasExtra("userDateTime")){
            finish();
        }

        routeId = getIntent().getStringExtra(Route.class.getSimpleName());
        driverId = getIntent().getStringExtra(Driver.class.getSimpleName());
        userDateTime = getIntent().getLongExtra("userDateTime",0);

        mapView = findViewById(R.id.mapView);
        boundsOfTheMap = findViewById(R.id.view7);
        imageViewClose = findViewById(R.id.imageViewClose);
        bottomSheet = findViewById(R.id.include_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        imageViewClose.setOnClickListener(view->finish());
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
                int bottomSheetShiftDown = bottomSheet.getHeight() - bottomSheet.getTop();
                map.setPadding(0,0,0,bottomSheetShiftDown);
            }
        });
        loadBottomSheetElements(bottomSheet);
        mapView.getMapAsync(this);
        mapView.onCreate(mapViewBundle);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(apiKey);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(apiKey, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        Route.loadRoute(routeId, route -> {
            r = route;
            LatLng startingPoint = new LatLng(
                    r.getRouteLatLng().getStartLat(), r.getRouteLatLng().getStartLng()
            );
            LatLng destinationPoint = new LatLng(
                    r.getRouteLatLng().getEndLat(), r.getRouteLatLng().getEndLng()
            );
            GoogleDirection.withServerKey(apiKey)
                    .from(startingPoint)
                    .to(destinationPoint)
                    .transportMode(TransportMode.DRIVING)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(@Nullable Direction direction) {
                            drawDirection(startingPoint,destinationPoint, boundsOfTheMap.getWidth(), boundsOfTheMap.getHeight(),direction);
                        }
                        @Override
                        public void onDirectionFailure(@NonNull Throwable t) {
                        }
                    });
            initializeSliderRiderCapacity(r);
            try {
                Geocoder g = new Geocoder(this);
                Address a = g.getFromLocation(r.getRouteLatLng().getStartLat(), r.getRouteLatLng().getStartLng(), 1).get(0);
                String start = a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
                a = g.getFromLocation(r.getRouteLatLng().getEndLat(),r.getRouteLatLng().getEndLng(),1).get(0);
                String end = a.getThoroughfare() +" "+a.getFeatureName()+", "+a.getLocality()+", "+a.getCountryName();
                originRoute.setText(getString(R.string.from)+" "+start);
                destinationRoute.setText(getString(R.string.to)+" "+end);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("hh.mm aa");
                timeRoute.setText(timeFormat.format(r.getRouteDateTime().getStartTimeUnix()));

            } catch (IOException e) {
                e.printStackTrace();
            }
            timeDifference.setText(route.getTextForTimeDif(this,userDateTime));
        });
    }
    private void initializeSliderRiderCapacity(Route r){
        sliderRiderCapacity.setValueFrom(0.f);
        sliderRiderCapacity.setValueTo(r.getMaxRiders());
        sliderRiderCapacity.setValue(r.getPassengersId().size());
        sliderRiderCapacityTo.setText(String.valueOf(r.getMaxRiders()) );
        int selectedColor = r.getColorForRideCapacitySlider();
        sliderRiderCapacity.setTrackActiveTintList(ColorStateList.valueOf(getColor(selectedColor)));
        sliderRiderCapacity.setThumbTintList(ColorStateList.valueOf(getColor(selectedColor)));
        sliderRiderCapacityFrom.setVisibility(View.VISIBLE);
        sliderRiderCapacityTo.setVisibility(View.VISIBLE);
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondaryVariant, typedValue, true);
        int color = typedValue.data;
        sliderRiderCapacity.setTickTintList(ColorStateList.valueOf(color));
        if (r.getPassengersId().size()==r.getMaxRiders()) {
            contactDriver.setOnClickListener(view -> {
                Toast.makeText(this, getString(R.string.request_send_full_capacity), Toast.LENGTH_SHORT).show();
                this.finish();
            });
        }
    }
    public void drawDirection(LatLng start, LatLng end,int width,int height,Direction direction){
        if (directions!=null){
            map.clear();
            directions = null;
        }
//        origin point
        startMarker = new MarkerOptions();
        startMarker.position(start);
        startMarker.icon(bitmapDescriptorFromVector(this,R.drawable.ic_origin_point));
        map.addMarker(startMarker);

//        destination point
        destinationMarker = new MarkerOptions();
        destinationMarker.position(end);
        destinationMarker.icon(bitmapDescriptorFromVector(this,R.drawable.ic_finish_route));
        map.addMarker(destinationMarker);
//        draw the route
        com.akexorcist.googledirection.model.Route r = direction.getRouteList().get(0);
        List<LatLng> paths = r.getLegList().get(0).getDirectionPoint();
        directions = new PolylineOptions();
        for (LatLng path:paths) {
            directions.add(path);
        }
        directions.color(Color.BLACK);
        directions.width(5);
        map.addPolyline(directions);

        int padding = (int) (width * EDGES_OFFSET_FOR_MAP);

//        // zoom to the directions
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(start)
                .include(end)
                .build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        map.animateCamera(cu, 1, new GoogleMap.CancelableCallback() {
            @Override
            public void onCancel() {

            }
            @Override
            public void onFinish() {
                CameraPosition cameraPosition = map.getCameraPosition();
                GlobalCoordinates startCoordinates = new GlobalCoordinates(bounds.getCenter().latitude, bounds.getCenter().longitude);
                GeodeticCalculator geoCalc = new GeodeticCalculator();
                GlobalCoordinates target = geoCalc.calculateEndingGlobalCoordinates(
                        Ellipsoid.WGS84, startCoordinates, cameraPosition.bearing, 150f);
                LatLng mapTarget = new LatLng(target.getLatitude(), target.getLongitude());
                CameraPosition cap = new CameraPosition(mapTarget,
                        cameraPosition.zoom, cameraPosition.tilt, cameraPosition.bearing);
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cap));
            }
        });
        map.setPadding(0,0,0,bottomSheet.getHeight());

    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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

    private void loadBottomSheetElements(View v){
        circleImageDriver = v.findViewById(R.id.circleImageDriver);
        tableRowShowRepeat = v.findViewById(R.id.tableRowShowRepeat);
        driverName = v.findViewById(R.id.textViewDriverName);
        originRoute = v.findViewById(R.id.textViewOriginRoutePoint);
        destinationRoute = v.findViewById(R.id.textViewDestinationRoutePoint);
        timeRoute = v.findViewById(R.id.textViewRouteTime);
        timeDifference = v.findViewById(R.id.textViewRouteTimeDifference);
        carName = v.findViewById(R.id.textViewCarName);
        carPlate = v.findViewById(R.id.textViewCarPlate);
        carYear = v.findViewById(R.id.textViewCarYear);
        ratingAverage = v.findViewById(R.id.textViewRatingAverage);
        ratingCount = v.findViewById(R.id.textViewRatingsCount);
        ratingBar = v.findViewById(R.id.ratingBarDriver);
        sliderRiderCapacity = v.findViewById(R.id.sliderRideCapacity);
        sliderRiderCapacityFrom = v.findViewById(R.id.sliderRideCapacityFrom);
        sliderRiderCapacityTo = v.findViewById(R.id.sliderRideCapacityTo);
        contactDriver = v.findViewById(R.id.buttonContactDriver);
        sliderRiderCapacity.setOnDragListener(null);
        tableRowShowRepeat.setOnClickListener(this::showRepeat);
        contactDriver.setOnClickListener(this::routeRequest);
        loadDriverData();
    }
    private void loadDriverData(){
        Driver.loadDriver(driverId, new OnUserLoadComplete() {
            @Override
            public void returnedUser(User u) {
                driverUser = (Driver) u;
                driverUser.loadUserImage(image -> {
                    if (image!=null){
                        circleImageDriver.setImageBitmap(image);
                    }else{
                        circleImageDriver.setBackgroundResource(R.drawable.ic_default_profile);
                    }
                });
                driverName.setText(driverUser.getFullName());
            }
        });
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void routeRequest(View view){
        //load the custom date Picker to the alertDialog
        final View dialogView = View.inflate(this, R.layout.request_route_alert_dialog, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.alert_dialog_background));
        dialogView.findViewById(R.id.buttonSendRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                make the request
                EditText description= dialogView.findViewById(R.id.editTextDesription);
                Request request = new Request(r.getRouteId(),rider.getUserId(),description.getText().toString(),new Date().getTime(),Request.REQ_REQUEST_CODE);
                rider.makeRequest(request, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(RouteActivity.this, getString(R.string.route_request_success),Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            System.out.println(task.getException().getLocalizedMessage());
                        }
                        alertDialog.dismiss();
                    }
                });
            }
        });
        dialogView.findViewById(R.id.textViewCancelRequest).setOnClickListener(view1 -> alertDialog.dismiss());
        alertDialog.setCancelable(true);
        alertDialog.show();
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
    private long findDateBasedSpecificDay(long startDateUnixTime, int daySelected){
//       check if the start date day is the same with the selected day
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startDateUnixTime);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (daySelected!=dayOfWeek - 1) {
            if (daySelected > dayOfWeek - 1) {
                calendar.add(Calendar.DAY_OF_YEAR, dayOfWeek - 1 - daySelected);
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, daySelected - (dayOfWeek - 1));
            }
            return calendar.getTimeInMillis();
        }
        return calendar.getTimeInMillis();
    }
    private void showRepeat(View view) {
        //load the custom date Picker to the alertDialog
        final View dialogView = View.inflate(this, R.layout.calendar_for_marked_days, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog).setView(dialogView).create();
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alertDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.alert_dialog_background));
        alertDialog.setCancelable(true);
        MaterialCalendarView materialCalendarView = dialogView.findViewById(R.id.calendarView);
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
        materialCalendarView.setClickable(false);
        Date date = new Date();
        date.setTime(r.getRouteDateTime().getStartDateUnix());
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        switch (r.getRouteDateTime().getTimetable()) {
            case 0: //one time
                materialCalendarView.setDateSelected(
                        CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)), true);
                break;
            case 1: // daily
                dailyRepeat(materialCalendarView, c);
                break;
            case 2: //weekly
                weeklyRepeat(materialCalendarView, c);
                break;
            case 3: // monthly
                monthlyRepeat(materialCalendarView, c);
                break;
            case 4: //yearly
                yearlyRepeat(materialCalendarView, c);
                break;
            case 5: //custom
                customRepeat(materialCalendarView, c);
                break;
        }
        alertDialog.show();
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void customRepeat(MaterialCalendarView materialCalendarView, @NonNull Calendar c) {
//      create a single calendar for each selected day
        List<Calendar> calendars = new ArrayList<>();
        List<String> selectedDays = new ArrayList<>(r.getRouteDateTime().getSelectedDays().values());
        Collections.sort(selectedDays);
        for (String days: selectedDays){
            Calendar currentCalendar = new GregorianCalendar();
            currentCalendar.setTimeInMillis(c.getTimeInMillis());
            currentCalendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(days));
            calendars.add(currentCalendar);
        }
        while (r.getRouteDateTime().getEndDateUnix() > calendars.get(0).getTimeInMillis()) {
            for (Calendar currentCalendar:calendars) {
                weeklyRepeat(materialCalendarView,currentCalendar);
            }
        }
    }

    private void yearlyRepeat(MaterialCalendarView materialCalendarView, @NonNull Calendar c) {
        int dayOfMonth;
//      TODO: remove Yearly
        dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        while (r.getRouteDateTime().getEndDateUnix() > c.getTimeInMillis()) {
            materialCalendarView.setDateSelected(
                    CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)), true);
            c.add(Calendar.YEAR,1);
            if (YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1).lengthOfMonth() <= c.get(Calendar.DAY_OF_MONTH)){
                c.set(Calendar.DAY_OF_MONTH,YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1).lengthOfMonth());
            }else {
                c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            }
        }
    }

    private void monthlyRepeat(MaterialCalendarView materialCalendarView, @NonNull Calendar c) {
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        while (r.getRouteDateTime().getEndDateUnix() > c.getTimeInMillis()) {
            materialCalendarView.setDateSelected(
                    CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)), true);

//                    change year if the month is december
            if (c.get(Calendar.MONTH) == Calendar.DECEMBER) {
                c.add(Calendar.YEAR, 1);
                c.set(Calendar.MONTH, Calendar.JANUARY);
            } else {
                c.add(Calendar.MONTH,1);
//                  check if the selected day_of_month is bigger than the last day of month
                if (YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1).lengthOfMonth() <= c.get(Calendar.DAY_OF_MONTH)) {
//                  check the last day of the month
                    c.set(Calendar.DAY_OF_MONTH, YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1).lengthOfMonth());
                }else {
                    c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                }
            }
        }
    }

    private void weeklyRepeat(MaterialCalendarView materialCalendarView, @NonNull Calendar c) {
        while (r.getRouteDateTime().getEndDateUnix() > c.getTimeInMillis()) {
            materialCalendarView.setDateSelected(
                    CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)), true);
            if (YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1).lengthOfMonth() <= (c.get(Calendar.DAY_OF_MONTH) - 1) + 7) {
                c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH)+7 - YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1).lengthOfMonth());
                if (c.get(Calendar.MONTH) == Calendar.DECEMBER) {
                    c.add(Calendar.YEAR, 1);
                    c.set(Calendar.MONTH, Calendar.JANUARY);
                } else {
                    c.add(Calendar.MONTH, 1);
                }
            } else {
                c.add(Calendar.DAY_OF_MONTH, 7);
            }
        }
    }

    private void dailyRepeat(MaterialCalendarView materialCalendarView, @NonNull Calendar c) {
        while (r.getRouteDateTime().getEndDateUnix() > c.getTimeInMillis()) {
            materialCalendarView.setDateSelected(
                    CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)), true);
            if (YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1).lengthOfMonth() == c.get(Calendar.DAY_OF_MONTH) - 1) {

                if (c.get(Calendar.MONTH) == Calendar.DECEMBER) {
                    c.add(Calendar.YEAR, 1);
                    c.set(Calendar.MONTH, Calendar.JANUARY);

                } else {
                    c.add(Calendar.MONTH, 1);
                }
                c.set(Calendar.DAY_OF_MONTH, 1);
            } else {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }
}