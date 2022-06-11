package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.driver.DriverActivity;
import com.unipi.diplomaThesis.rideshare.rider.RiderActivity;

public class StartActivity extends AppCompatActivity {
    private static String REQ_LAST_LOCATIONS = "lastLocation";
    public static final int REQ_DRIVER_ACTIVITY = 213;
    public static final int REQ_RIDER_ACTIVITY = 678;
    View fragment;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        progressBar = findViewById(R.id.progressBar);
        fragment = findViewById(R.id.fragmentLoginRegister);
        stopProgressBarAnimation();
//        check if the user is already sign in
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            User u = User.loadUserInstance(this);
            if (u!=null){
                if (u.getType().equals(Driver.class.getSimpleName())){
                    this.startActivityForResult(new Intent(this, DriverActivity.class),REQ_DRIVER_ACTIVITY);
                }else{
                    this.startActivityForResult(new Intent(this, RiderActivity.class), REQ_RIDER_ACTIVITY);
                }
                return;
            }
//          load the user instance if is still authorized
            User.loadSignInUser(new OnUserLoadComplete() {
                @Override
                public void returnedUser(User u) {
                    // if the user didn't found sign out
                    if (u == null){
                        FirebaseAuth.getInstance().signOut();
                        return;
                    }
                    PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit()
                            .putString(User.REQ_TYPE_TAG,u.getType()).apply();
                    Gson gson = new Gson();
                    String json = gson.toJson(u);
                    PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit()
                            .putString(User.class.getSimpleName(),json).apply();
                    if (u.getType().equals(Driver.class.getSimpleName())){
                        StartActivity.this.startActivityForResult(new Intent(StartActivity.this, DriverActivity.class),REQ_DRIVER_ACTIVITY);
                    }else {
                        StartActivity.this.startActivityForResult(new Intent(StartActivity.this, RiderActivity.class), REQ_RIDER_ACTIVITY);
                    }
                }
            });
        }
    }
    public void register(View view){
        transaction(new RegisterFragment());
    }
    public void login(View view){
       transaction(new LoginFragment());
    }
    private void transaction(Fragment fragmentToShow){
        TranslateAnimation animate0 = new TranslateAnimation(0, 0, 0, fragment.getHeight());
        animate0.setDuration(500);
        animate0.setFillAfter(true);
        animate0.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentLoginRegister,fragmentToShow)
                        .runOnCommit(() -> {
                            TranslateAnimation animate01 = new TranslateAnimation(0, 0,fragment.getHeight(), 0);
                            animate01.setDuration(500);
                            animate01.setFillAfter(true);
                            fragment.startAnimation(animate01);
                        }).commit();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        fragment.startAnimation(animate0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_RIDER_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                User.loadSignInUser(new OnUserLoadComplete() {
                    @Override
                    public void returnedUser(User u) {
                        // if the user didn't found sign out
                        if (u == null) {
                            FirebaseAuth.getInstance().signOut();
                            return;
                        }
                        PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit()
                                .putString(User.REQ_TYPE_TAG, u.getType()).apply();
                        Gson gson = new Gson();
                        String json = gson.toJson(u);
                        PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit()
                                .putString(User.class.getSimpleName(), json).apply();
                        startActivity(new Intent(StartActivity.this, DriverActivity.class));
                    }
                });
                Toast.makeText(this, getString(R.string.you_are_driver_now), Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void startProgressBarAnimation(){
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }
    public void stopProgressBarAnimation(){
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }

}