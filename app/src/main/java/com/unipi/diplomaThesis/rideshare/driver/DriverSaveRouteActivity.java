package com.unipi.diplomaThesis.rideshare.driver;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteAdditionalInfoFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteTimeTableFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteDirectionsFragment;

public class DriverSaveRouteActivity extends AppCompatActivity{
    int progress = 0;
    public int COUNT_STEPS=4;
    TableRow tableRowStages,tableRowProgress;
    DriverSaveRouteDirectionsFragment driverSaveRouteDirectionsFragment;
    DriverSaveRouteTimeTableFragment driverSaveRouteTimeTableFragment;
    DriverSaveRouteAdditionalInfoFragment driverSaveRouteAdditionalInfoFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_save_route);
        tableRowProgress = findViewById(R.id.tableRowProgress);
        tableRowStages = findViewById(R.id.tableRowStages);
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
        changeProgressBar(progress);
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
        }
    }
    private void changeProgressBar(int progress){
        if (progress == COUNT_STEPS-1){
            tableRowProgress.setVisibility(View.GONE);
            tableRowStages.setBackgroundTintList(tableRowProgress.getBackgroundTintList());
        }else {
            tableRowProgress.setVisibility(View.VISIBLE);
            changeWidthWithAnim(tableRowProgress.getLayoutParams().width, //start
                    (tableRowStages.getLayoutParams().width/COUNT_STEPS)*(progress+1) + 10 //end
            );
        }
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
                ViewGroup.LayoutParams layoutParams = tableRowProgress.getLayoutParams();
                layoutParams.width = val;
                tableRowProgress.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(1000);
        anim.start();

    }
}