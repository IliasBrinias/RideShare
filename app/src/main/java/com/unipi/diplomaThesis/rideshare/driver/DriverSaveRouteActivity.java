package com.unipi.diplomaThesis.rideshare.driver;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
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

import java.util.Map;

public class DriverSaveRouteActivity extends AppCompatActivity{
    int progress = 0;
    public int COUNT_STEPS=3;
    ProgressBar progressBar;
    DriverSaveRouteDirectionsFragment driverSaveRouteDirectionsFragment;
    DriverSaveRouteTimeTableFragment driverSaveRouteTimeTableFragment;
    DriverSaveRouteAdditionalInfoFragment driverSaveRouteAdditionalInfoFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_save_route);
        progressBar = findViewById(R.id.progressBar);
        handleStep(0);
    }
    public void nextStep(){
        progress++;
        handleStep(progress);
    }
    public void stepBack(){
        progress--;
        handleStep(progress);
    }
    private void handleStep(int progress){
        changeProgressBar(progress+1);
        switch (progress){
            case 0: // Directions
                driverSaveRouteDirectionsFragment = new DriverSaveRouteDirectionsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,driverSaveRouteDirectionsFragment).commit();
                break;
            case 1: //timeTable
                driverSaveRouteTimeTableFragment = new DriverSaveRouteTimeTableFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, driverSaveRouteTimeTableFragment).commit();
                break;
            case 2:
                driverSaveRouteAdditionalInfoFragment = new DriverSaveRouteAdditionalInfoFragment();
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
        Route r = new Route(
                additionalInfo.get("name").toString(),
                null,
                driver.getUserId(),
                points,
                null,
                routeDateTime,
                additionalInfo.get("costPreRider").toString(),
                (int) additionalInfo.get("rideCapacity")
        );
        driver.saveRoute(r, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });
    }
}