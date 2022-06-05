package com.unipi.diplomaThesis.rideshare.driver;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.CarFragment;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Message;
import com.unipi.diplomaThesis.rideshare.Model.MyApplication;
import com.unipi.diplomaThesis.rideshare.Model.Request;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.PersonalDataFragment;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.RiderLastRoutesFragment;
import com.unipi.diplomaThesis.rideshare.messenger.MessengerActivity;

import java.util.ArrayList;

public class DriverActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, NavigationView.OnNavigationItemSelectedListener {
    protected MyApplication mMyApp;

    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private PersonalDataFragment personalDataFragment;
    RiderLastRoutesFragment riderLastRoutesFragment;
    CarFragment carFragment;

    DriverRouteListFragment driverRouteListFragment;
    private Driver userDriver;
    private TextView userNameNavigationHeader,emailNavigationHeader;
    private ImageView imageNavigationHeader;
    ArrayList<String> newRequests = new ArrayList<>();
    ArrayList<String> newMessages = new ArrayList<>();
    BadgeDrawable badgeDrawableRequests;
    BadgeDrawable badgeDrawableMessages;

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        try {
            userDriver = (Driver) User.loadUserInstance(this);
            if (userDriver == null) {
                finish();
            }
        }catch (ClassCastException classCastException){
            classCastException.printStackTrace();
            finish();
        }
        topAppBar = findViewById(R.id.topAppBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.NavigationViewUser);
        View headerView = navigationView.getHeaderView(0);
        userNameNavigationHeader = headerView.findViewById(R.id.textViewNavigationUserName);
        emailNavigationHeader = headerView.findViewById(R.id.textViewNavigationUserEmail);
        imageNavigationHeader = headerView.findViewById(R.id.CircleImageDriverImage);
//        set Menu
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.navigation_menu_driver);
        topAppBar.getMenu().clear();
        topAppBar.inflateMenu(R.menu.toolbar_menu_driver);
        topAppBar.setNavigationOnClickListener(view -> drawerLayout.open());
        driverRouteListFragment = new DriverRouteListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,driverRouteListFragment).commit();

//        load User data
        loadUserData(userDriver);
//        ToolBar Items
        topAppBar.setOnMenuItemClickListener(this);
//        Navigation Items
        navigationView.setNavigationItemSelectedListener(this);
        mMyApp = (MyApplication) this.getApplicationContext();
        badgeDrawableRequests = BadgeDrawable.create(this);
        badgeDrawableMessages = BadgeDrawable.create(this);
        startChecking();
    }
    public void loadUserData(User u){
        userNameNavigationHeader.setText(User.reformatLengthString(u.getFullName(),20));
        emailNavigationHeader.setText(User.reformatLengthString(u.getEmail(),30));
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.requests:
                startActivity(new Intent(this, RequestsActivity.class));
                break;
            case R.id.messages:
                startActivity(new Intent(this, MessengerActivity.class));
                break;
        }
        return false;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
                driverRouteListFragment = new DriverRouteListFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,driverRouteListFragment).commit();
                break;
            case R.id.personalData:
                personalDataFragment = new PersonalDataFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,personalDataFragment).commit();
                break;
            case R.id.yourRoutes:
                riderLastRoutesFragment = new RiderLastRoutesFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,riderLastRoutesFragment).commit();
                break;
            case R.id.carDriver:
                carFragment = new CarFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,carFragment).commit();
                break;
            case R.id.logOut:
                DriverActivity.this.userDriver.logOut(DriverActivity.this);
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
        newRequests.clear();
        newMessages.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void startChecking(){
        userDriver.loadDriversRouteId(ids -> {
            FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (String id:ids) {
                            if (!snapshot.hasChild(id)) continue;
                            for (DataSnapshot requests : snapshot.child(id).getChildren()) {
                                String userId = requests.getKey();
                                if (!newRequests.contains(userId)) {
                                    newRequests.add(userId);
                                }
                            }
                            if (newRequests == null) return;
                        }
                        if (newRequests.size() == 0) {
                            BadgeUtils.detachBadgeDrawable(badgeDrawableRequests, topAppBar, R.id.requests);
                            return;
                        }
                        badgeDrawableRequests.setNumber(newRequests.size());
                        BadgeUtils.attachBadgeDrawable(badgeDrawableRequests, topAppBar, R.id.requests);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        });
        userDriver.loadUserMessageSession(messageSession -> {
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

            if (m.getUserSenderId().equals(userDriver.getUserId())){
                if (newMessages.contains(messageSession.getMessageSessionId())){
                    newMessages.remove(messageSession.getMessageSessionId());
                }
            }else{
                if (m.isSeen()){
                    if (newMessages.contains(messageSession.getMessageSessionId())){
                        newMessages.remove(messageSession.getMessageSessionId());
                    }
                }else{
                    if (!newMessages.contains(messageSession.getMessageSessionId())){
                        newMessages.add(messageSession.getMessageSessionId());
                    }
                }

            }
            if (newMessages == null) return;
            if (newMessages.size() == 0){
                BadgeUtils.detachBadgeDrawable(badgeDrawableMessages,topAppBar,R.id.messages);
                return;
            }
            badgeDrawableMessages.setNumber(newMessages.size());
            BadgeUtils.attachBadgeDrawable(badgeDrawableMessages,topAppBar,R.id.messages);
        });
    }
}
