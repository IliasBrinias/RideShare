package com.unipi.diplomaThesis.rideshare.driver;

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
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.PersonalDataFragment;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.rider.RouteSearchFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener, NavigationView.OnNavigationItemSelectedListener {
    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RouteSearchFragment routeSearchFragment;
    private PersonalDataFragment personalDataFragment;
    private Driver userDriver;
    private TextView userNameNavigationHeader,emailNavigationHeader;
    private CircleImageView imageNavigationHeader;

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

//        load User data
        loadUserData();
//        ToolBar Items
        topAppBar.setOnMenuItemClickListener(this);
//        Navigation Items
        navigationView.setNavigationItemSelectedListener(this);
    }
    private void loadUserData(){
        userNameNavigationHeader.setText(userDriver.getFullName());
        emailNavigationHeader.setText(userDriver.getEmail());
        userDriver.loadUserImage(image -> {
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.requests:
                Toast.makeText(DriverActivity.this, "Requests", Toast.LENGTH_SHORT).show();
                break;
//                        TODO: open Apps Requests
            case R.id.messages:
                Toast.makeText(DriverActivity.this, "Messages", Toast.LENGTH_SHORT).show();
//                        TODO: open Apps Messenger
                break;
        }
        return false;
    }

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
            case R.id.logOut:
                DriverActivity.this.userDriver.logOut(DriverActivity.this);
                Intent i = new Intent();
                i.putExtra("LogOut","true");
                setResult(Activity.RESULT_OK, i);
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
