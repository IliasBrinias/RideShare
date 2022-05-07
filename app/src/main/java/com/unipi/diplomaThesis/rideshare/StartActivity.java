package com.unipi.diplomaThesis.rideshare;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Model.User;

public class StartActivity extends AppCompatActivity {
    private static String REQ_LAST_LOCATIONS = "lastLocation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

//        check if the user is already sign in
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            if (User.loadUserInstance(PreferenceManager.getDefaultSharedPreferences(this))!=null){
                startActivityForResult(new Intent(this, UserActivity.class),123);
                return;
            }
            User.loadSignInUser(new OnUserLoadComplete() {
                @Override
                public void returnedUser(User u) {
                    // if the user didn't found sign out
                    if (u == null){
                        FirebaseAuth.getInstance().signOut();
                        return;
                    }
                    PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit()
                            .putString(User.REQ_IS_DRIVER_TAG,u.getIsDriver().toString()).apply();
                    Gson gson = new Gson();
                    String json = gson.toJson(u);
                    PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit()
                            .putString(User.class.getSimpleName(),json).apply();
                    startActivityForResult(new Intent(StartActivity.this, UserActivity.class),123);
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
}