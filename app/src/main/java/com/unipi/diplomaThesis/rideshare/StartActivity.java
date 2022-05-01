package com.unipi.diplomaThesis.rideshare;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    private static String REQ_LAST_LOCATIONS = "lastLocation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
//        PreferenceManager.getDefaultSharedPreferences(this).edit().remove(REQ_LAST_LOCATIONS).apply();
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

}