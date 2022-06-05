package com.unipi.diplomaThesis.rideshare.rider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.CarFragment;
import com.unipi.diplomaThesis.rideshare.Model.Message;
import com.unipi.diplomaThesis.rideshare.Model.MyApplication;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.PersonalDataFragment;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.RiderLastRoutesFragment;
import com.unipi.diplomaThesis.rideshare.messenger.MessengerActivity;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends AppCompatActivity {
    protected MyApplication mMyApp;


    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    RiderLastRoutesFragment riderLastRoutesFragment;

    private CarFragment carFragment;
    private RouteSearchFragment routeSearchFragment;
    private PersonalDataFragment personalDataFragment;
    private User u;
    private TextView userNameNavigationHeader,emailNavigationHeader;
    private ImageView imageNavigationHeader;
    List<String> newMessages=new ArrayList<>();
    BadgeDrawable badgeDrawableMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        u = User.loadUserInstance(this);
        if (u == null) {
            finish();
        }
        topAppBar = findViewById(R.id.topAppBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.NavigationViewUser);
        View headerView = navigationView.getHeaderView(0);
        userNameNavigationHeader = headerView.findViewById(R.id.textViewNavigationUserName);
        emailNavigationHeader = headerView.findViewById(R.id.textViewNavigationUserEmail);
        imageNavigationHeader = headerView.findViewById(R.id.CircleImageDriverImage);
//        load User data
        loadUserData(u);
        topAppBar.setNavigationOnClickListener(view -> drawerLayout.open());
//        ToolBar Items
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.messages:
                        startActivity(new Intent(RiderActivity.this, MessengerActivity.class));
                        break;

                }
                return false;
            }
        });
//        Navigation Items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        routeSearchFragment = new RouteSearchFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,routeSearchFragment).commit();
                        break;
                    case R.id.personalData:
                        personalDataFragment = new PersonalDataFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,personalDataFragment).commit();
                        break;
                    case R.id.yourRoutes:
                        riderLastRoutesFragment = new RiderLastRoutesFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,riderLastRoutesFragment).commit();
                        break;
                    case R.id.becameDriver:
                        carFragment = new CarFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,carFragment).commit();
                        break;
                    case R.id.logOut:
                        Intent i = new Intent();
                        i.putExtra("LogOut", FirebaseAuth.getInstance().getCurrentUser().getProviderId());
                        RiderActivity.this.u.logOut(RiderActivity.this);
                        setResult(Activity.RESULT_OK, i);
                        finish();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        mMyApp = (MyApplication) this.getApplicationContext();
        badgeDrawableMessages = BadgeDrawable.create(this);
        startChecking();
    }
    public void loadUserData(User u){
        userNameNavigationHeader.setText(User.reformatLengthString(u.getFullName(),15));
        emailNavigationHeader.setText(User.reformatLengthString(u.getEmail(),20));
        u.loadUserImage(image -> {
            imageNavigationHeader.setImageBitmap(null);
            imageNavigationHeader.setBackgroundResource(0);

            if (image!=null){
                imageNavigationHeader.setImageBitmap(image);
            }else{
                imageNavigationHeader.setBackgroundResource(R.drawable.ic_default_profile);
            }
        });
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.msg_back_again_exit), Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        startChecking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @SuppressLint("UnsafeOptInUsageError")
    private void startChecking() {

        u.loadUserMessageSession(messageSession -> {
            if (messageSession == null) return;
            if (messageSession.getMessages().isEmpty()){
                if (!newMessages.contains(messageSession.getMessageSessionId())) {
                    newMessages.add(messageSession.getMessageSessionId());
                }
                badgeDrawableMessages.setNumber(newMessages.size());
                BadgeUtils.attachBadgeDrawable(badgeDrawableMessages, topAppBar, R.id.messages);
                return;
            }
            Message m = messageSession.getMessages().entrySet().iterator().next().getValue();

            if (m.getUserSenderId().equals(u.getUserId())) {
                if (newMessages.contains(messageSession.getMessageSessionId())) {
                    newMessages.remove(messageSession.getMessageSessionId());
                }
            } else {
                if (m.isSeen()) {
                    if (newMessages.contains(messageSession.getMessageSessionId())) {
                        newMessages.remove(messageSession.getMessageSessionId());
                    }
                } else {
                    if (!newMessages.contains(messageSession.getMessageSessionId())) {
                        newMessages.add(messageSession.getMessageSessionId());
                    }
                }

            }
            if (newMessages == null) return;
            if (newMessages.size() == 0) {
                BadgeUtils.detachBadgeDrawable(badgeDrawableMessages, topAppBar, R.id.messages);
                return;
            }
            badgeDrawableMessages.setNumber(newMessages.size());
            BadgeUtils.attachBadgeDrawable(badgeDrawableMessages, topAppBar, R.id.messages);
        });

    }
}