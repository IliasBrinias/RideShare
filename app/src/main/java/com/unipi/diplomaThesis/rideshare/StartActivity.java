package com.unipi.diplomaThesis.rideshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        progressBar = findViewById(R.id.progressBar);
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
        RegisterFragment registerFragment = new RegisterFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top)
                .replace(R.id.fragmentLoginRegister,registerFragment)
                .commit();
    }
    public void login(View view){
        LoginFragment loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top)
                .replace(R.id.fragmentLoginRegister,loginFragment)
                .commit();
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