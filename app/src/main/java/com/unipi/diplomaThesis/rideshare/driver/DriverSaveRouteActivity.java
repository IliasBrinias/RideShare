package com.unipi.diplomaThesis.rideshare.driver;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Routes;
import com.unipi.diplomaThesis.rideshare.Model.RouteDateTime;
import com.unipi.diplomaThesis.rideshare.Model.RouteLatLng;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.fragments.DriverSaveRouteAdditionalInfoFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.DriverSaveRouteDirectionsFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.DriverSaveRouteTimeTableFragment;

import java.util.ArrayList;
import java.util.Map;

public class DriverSaveRouteActivity extends AppCompatActivity{
    int progress = 0;
    public int COUNT_STEPS=3;
    ProgressBar progressBar, progressBarSave;
    ImageButton buttonBack;
    TextView title;
    DriverSaveRouteDirectionsFragment driverSaveRouteDirectionsFragment;
    DriverSaveRouteTimeTableFragment driverSaveRouteTimeTableFragment;
    DriverSaveRouteAdditionalInfoFragment driverSaveRouteAdditionalInfoFragment;
    Routes routes;
    Driver driver = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_save_route);
        try {
            driver = (Driver) User.loadUserInstance(this);
        }catch (ClassCastException e){
            e.printStackTrace();
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        progressBar = findViewById(R.id.progressBar);
        title = findViewById(R.id.textViewWelcomeDriverTitle);
        driverSaveRouteDirectionsFragment = new DriverSaveRouteDirectionsFragment();
        driverSaveRouteTimeTableFragment = new DriverSaveRouteTimeTableFragment();
        driverSaveRouteAdditionalInfoFragment = new DriverSaveRouteAdditionalInfoFragment();
        buttonBack = findViewById(R.id.buttonBack);
        progressBarSave = findViewById(R.id.progressBarSave);
        buttonBack.setOnClickListener(v->finish());
        if (getIntent().hasExtra(Routes.class.getSimpleName())){
            routes =(Routes) getIntent().getSerializableExtra(Routes.class.getSimpleName());
            title.setText(getString(R.string.edit_route));
            loadRoute(routes);
        }

        handleStep(0);
    }
    public void nextStep(){
        progress++;
        handleStep(progress);
    }
    public void previousStep(){
        progress--;
        handleStep(progress);
    }
    private void handleStep(int progress){
        changeProgressBar(progress+1);
        switch (progress){
            case 0: // Directions
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,driverSaveRouteDirectionsFragment).commit();
                break;
            case 1: //timeTable
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, driverSaveRouteTimeTableFragment).commit();
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,driverSaveRouteAdditionalInfoFragment).commit();
                break;
            case 4:
                saveRoute();
        }
    }
    private void changeProgressBar(int progress){
        progressBar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                progressBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (progress == COUNT_STEPS){
                    changeWidthWithAnim(progressBar.getProgress(), 100);
                }else {
                    changeWidthWithAnim(progressBar.getProgress(), (100/(COUNT_STEPS))*progress);
                }
            }
        });
    }
    private void changeWidthWithAnim(int startValueWidth, int endValueWidth){
        ValueAnimator anim = ValueAnimator.ofInt(
                startValueWidth, //start
                endValueWidth //end
        );
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                progressBar.setProgress(val);
            }
        });
        anim.setDuration(1000);
        anim.start();
    }
    public void editRoute(){
//        collect all the data from the fragments and create the routes Object
        RouteLatLng points = driverSaveRouteDirectionsFragment.getPoints();
        RouteDateTime routeDateTime = driverSaveRouteTimeTableFragment.getRouteDateTime();
        Map<String,Object> additionalInfo = driverSaveRouteAdditionalInfoFragment.getAdditionalInfo();

        if (points!=null) {
            points.setMaximumDeviation(routes.getRouteLatLng().getMaximumDeviation());
            routes.setRouteLatLng(points);
        }
        if (additionalInfo !=null){
            routes.getRouteLatLng().setMaximumDeviation((double) additionalInfo.get("maximumDeviation"));
            ArrayList<String> deletedPassengers = (ArrayList<String>) additionalInfo.get("deletedPassengers");
            routes.setRideCapacity((int) additionalInfo.get("rideCapacity"));
            routes.setPassengersId((ArrayList<String>) additionalInfo.get("passengersId"));
            routes.setCostPerRider((Double) additionalInfo.get("costPreRider"));
            routes.setName(additionalInfo.get("name").toString());
            for (String id:deletedPassengers){
                driver.deletePassenger(routes.getRouteId(),id, null);
            }
        }
        if (routeDateTime!=null) routes.setRouteDateTime(routeDateTime);

        saveRouteToDatabase(routes);
    }
    public void saveRoute(){
//        collect all the data from the fragments and create the routes Object
        RouteLatLng points = driverSaveRouteDirectionsFragment.getPoints();
        RouteDateTime routeDateTime = driverSaveRouteTimeTableFragment.getRouteDateTime();
        Map<String,Object> additionalInfo = driverSaveRouteAdditionalInfoFragment.getAdditionalInfo();
        points.setMaximumDeviation((double) additionalInfo.get("maximumDeviation"));

        if (routes !=null){
            ArrayList<String> deletedPassengers = (ArrayList<String>) additionalInfo.get("deletedPassengers");
            routes.setName(additionalInfo.get("name").toString());
            routes.setRouteLatLng(points);
            routes.setRouteDateTime(routeDateTime);
            routes.setCostPerRider((Double) additionalInfo.get("costPreRider"));
            routes.setRideCapacity((int) additionalInfo.get("rideCapacity"));
            routes.setPassengersId((ArrayList<String>) additionalInfo.get("passengersId"));
            for (String id:deletedPassengers){
                driver.deletePassenger(routes.getRouteId(),id, null);
            }
        }else {
            routes = new Routes(
                    additionalInfo.get("name").toString(),
                    null,
                    driver.getUserId(),
                    points,
                    null,
                    routeDateTime,
                    (Double) additionalInfo.get("costPreRider"),
                    (int) additionalInfo.get("rideCapacity")
            );
        }
        saveRouteToDatabase(routes);
    }
    private void saveRouteToDatabase(Routes r){
        progressBarSave.setIndeterminate(true);
        progressBarSave.setVisibility(View.VISIBLE);
        driver.saveRoute(routes, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBarSave.setIndeterminate(false);
                progressBarSave.setVisibility(View.GONE);
                if (task.isSuccessful()){
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

    }
    public void loadRoute(Routes routes){
        driverSaveRouteDirectionsFragment.setPoints(routes.getRouteLatLng());
        driverSaveRouteTimeTableFragment.setRouteDateTime(routes.getRouteDateTime());
        driverSaveRouteAdditionalInfoFragment.setAdditionalInfo(
                routes.getRouteLatLng().getMaximumDeviation(),
                routes.getRideCapacity(),
                routes.getCostPerRider(),
                routes.getName(),
                routes.getPassengersId());
    }
    public Routes getRoute(){
        return routes;
    }
}