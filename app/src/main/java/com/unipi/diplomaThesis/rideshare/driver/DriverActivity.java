package com.unipi.diplomaThesis.rideshare.driver;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverRouteFragment;
import com.unipi.diplomaThesis.rideshare.driver.fragments.Route.DriverSaveRouteFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DriverRouteFragment driverRouteFragment;
    private DriverSaveRouteFragment driverSaveRouteFragment;
    private Driver driver;
    private TextView userNameNavigationHeader,emailNavigationHeader;
    private CircleImageView imageNavigationHeader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);
        driver = (Driver) User.loadUserInstance(PreferenceManager.getDefaultSharedPreferences(this));
        if (driver == null) {
            finish();
        }
        driverSaveRouteFragment = new DriverSaveRouteFragment();
        topAppBar = findViewById(R.id.topAppBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.NavigationViewUser);
        View headerView = navigationView.getHeaderView(0);
        userNameNavigationHeader = headerView.findViewById(R.id.textViewNavigationUserName);
        emailNavigationHeader = headerView.findViewById(R.id.textViewNavigationUserEmail);
        imageNavigationHeader = headerView.findViewById(R.id.CircleImageDriverImage);
        loadUserData();
//        handle back key with fragment
        View fragment = findViewById(R.id.riderFragment);
        fragment.setFocusableInTouchMode(true);
        fragment.requestFocus();
        fragment.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_BACK)
            {
                onBackPressed();
                return true;
            }
            return false;
        });
//        load User data
        driverRouteFragment = new DriverRouteFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,driverRouteFragment).commit();
        topAppBar.setNavigationOnClickListener(view -> drawerLayout.open());
//        ToolBar Items
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.requests:
                        Toast.makeText(DriverActivity.this, "Requests", Toast.LENGTH_SHORT).show();
//                        TODO: make requests dropDown
                        break;
                    case R.id.messages:
                        Toast.makeText(DriverActivity.this, "Messages", Toast.LENGTH_SHORT).show();
//                        TODO: open Apps Messenger
                        break;

                }
                return false;
            }
        });
//        Navigation Items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.personalData:
//                        TODO: edit Personal Data
                        break;
                    case R.id.availableCars:
//                        TODO: Edit Available Cars
                        break;
                    case R.id.yourRoutes:
                        getSupportFragmentManager().beginTransaction().replace(R.id.riderFragment,driverRouteFragment).commit();
                        break;
                    case R.id.logOut:
                        driver.logOut(DriverActivity.this);
                        finish();
                        break;
                }
                topAppBar.setTitle(item.getTitle());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    private void loadUserData(){
        userNameNavigationHeader.setText(driver.getFullName());
        emailNavigationHeader.setText(driver.getEmail());
        driver.loadUserImage(image -> {
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
    }
}