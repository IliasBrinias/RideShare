package com.unipi.diplomaThesis.rideshare.driver;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.RouteDateTime;
import com.unipi.diplomaThesis.rideshare.Model.RouteLatLng;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteAdditionalInfoFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteDirectionsFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteTimeTableFragment;

import java.util.ArrayList;
import java.util.Map;

public class DriverSaveRouteActivity extends AppCompatActivity{
    int progress = 0;
    public int COUNT_STEPS=3;
    ProgressBar progressBar, progressBarSave;
    ImageButton buttonBack;
    DriverSaveRouteDirectionsFragment driverSaveRouteDirectionsFragment;
    DriverSaveRouteTimeTableFragment driverSaveRouteTimeTableFragment;
    DriverSaveRouteAdditionalInfoFragment driverSaveRouteAdditionalInfoFragment;
    Route route;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_save_route);
        progressBar = findViewById(R.id.progressBar);
        driverSaveRouteDirectionsFragment = new DriverSaveRouteDirectionsFragment();
        driverSaveRouteTimeTableFragment = new DriverSaveRouteTimeTableFragment();
        driverSaveRouteAdditionalInfoFragment = new DriverSaveRouteAdditionalInfoFragment();
        buttonBack = findViewById(R.id.buttonBack);
        progressBarSave = findViewById(R.id.progressBarSave);
        buttonBack.setOnClickListener(v->finish());
        if (getIntent().hasExtra(Route.class.getSimpleName())){
            route =(Route) getIntent().getSerializableExtra(Route.class.getSimpleName());
            loadRoute(route);
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
    public void saveRoute(){
        Driver driver = null;
        try {
            driver = (Driver) User.loadUserInstance(this);
        }catch (ClassCastException e){
            e.printStackTrace();
            FirebaseAuth.getInstance().signOut();
            finish();
        }
//        collect all the data from the fragments and create the route Object
        RouteLatLng points = driverSaveRouteDirectionsFragment.getPoints();
        RouteDateTime routeDateTime = driverSaveRouteTimeTableFragment.getRouteDateTime();
        Map<String,Object> additionalInfo = driverSaveRouteAdditionalInfoFragment.getAdditionalInfo();
        points.setMaximumDeviation((double) additionalInfo.get("maximumDeviation"));

        if (route!=null){
            ArrayList<String> deletedPassengers = (ArrayList<String>) additionalInfo.get("deletedPassengers");
            route.setName(additionalInfo.get("name").toString());
            route.setRouteLatLng(points);
            route.setRouteDateTime(routeDateTime);
            route.setCostPerRider(additionalInfo.get("costPreRider").toString());
            route.setRideCapacity((int) additionalInfo.get("rideCapacity"));
            route.setPassengersId((ArrayList<String>) additionalInfo.get("passengersId"));
            for (String id:deletedPassengers){
                driver.deletePassenger(route.getRouteId(),id, null);
            }
        }else {
            route = new Route(
                    additionalInfo.get("name").toString(),
                    null,
                    driver.getUserId(),
                    points,
                    null,
                    routeDateTime,
                    additionalInfo.get("costPreRider").toString(),
                    (int) additionalInfo.get("rideCapacity")
            );
        }
        progressBarSave.setIndeterminate(true);
        progressBarSave.setVisibility(View.VISIBLE);
        driver.saveRoute(route, new OnCompleteListener<Void>() {
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
    public void loadRoute(Route route){
        driverSaveRouteDirectionsFragment.setPoints(route.getRouteLatLng());
        driverSaveRouteTimeTableFragment.setRouteDateTime(route.getRouteDateTime());
        driverSaveRouteAdditionalInfoFragment.setAdditionalInfo(route.getRouteLatLng().getMaximumDeviation(),
                route.getRideCapacity(),
                route.getCostPerRider(),
                route.getName(),
                route.getPassengersId());
    }
}