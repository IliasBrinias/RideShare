package com.unipi.diplomaThesis.rideshare.rider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.PersonalDataFragment;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverRouteFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiderActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DriverRouteFragment driverRouteFragment;
    private DriverSaveRouteFragment driverSaveRouteFragment;
    private CarFragment carFragment;
    private RouteSearchFragment routeSearchFragment;
    private PersonalDataFragment personalDataFragment;
    private User u;
    private TextView userNameNavigationHeader,emailNavigationHeader;
    private CircleImageView imageNavigationHeader;

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
        loadUserData();
        driverSaveRouteFragment = new DriverSaveRouteFragment();
        driverRouteFragment = new DriverRouteFragment();
        topAppBar.setNavigationOnClickListener(view -> drawerLayout.open());
//        ToolBar Items
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.messages:
                        Toast.makeText(RiderActivity.this, "Messages", Toast.LENGTH_SHORT).show();
//                        TODO: open Apps Messenger
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
//                        getSupportFragmentManager().beginTransaction().replace(R.id.driverFragment,driverRouteFragment).commit();
                        break;
                    case R.id.becameDriver:
                        carFragment = new CarFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,carFragment).commit();
                        break;
                    case R.id.logOut:
                        RiderActivity.this.u.logOut(RiderActivity.this);
                        Intent i = new Intent();
                        i.putExtra("LogOut","true");
                        setResult(Activity.RESULT_OK, i);
                        finish();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    private void loadUserData(){
        userNameNavigationHeader.setText(u.getFullName());
        emailNavigationHeader.setText(u.getEmail());
        u.loadUserImage(image -> {
            if (image!=null){
                imageNavigationHeader.setImageBitmap(image);
            }else{
                imageNavigationHeader.setBackgroundResource(R.drawable.ic_default_profile);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}