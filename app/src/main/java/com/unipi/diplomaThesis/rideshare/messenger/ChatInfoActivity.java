package com.unipi.diplomaThesis.rideshare.messenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.diplomaThesis.rideshare.Interface.OnClickDriverRoute;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.Rider;
import com.unipi.diplomaThesis.rideshare.Model.Route;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.Route.RouteActivity;
import com.unipi.diplomaThesis.rideshare.driver.adapter.DriverRouteListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatInfoActivity extends AppCompatActivity {
    String participantId;
    ImageView userImage, imageCar, imageLeave, imageExit;
    TextView userName,
            carName,
            carYear,
            carPlate,
            averageRating,
            countReviews;
    Switch muteMessage;
    RatingBar ratingBar;
    RecyclerView recyclerView;
    TableRow tableRowCar;
    DriverRouteListAdapter riderRouteAdapter;
    Rider rider=new Rider();
    Driver driver = new Driver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);
        if (!getIntent().hasExtra(User.class.getSimpleName())) finish();
        userImage = findViewById(R.id.imageViewUser);
        imageCar = findViewById(R.id.imageViewCarImage);
        imageExit = findViewById(R.id.exitChatInfo);
        imageLeave = findViewById(R.id.leaveChat);
        userName = findViewById(R.id.textViewUserName);
        carName = findViewById(R.id.textViewCarName);
        carPlate = findViewById(R.id.textViewCarPlate);
        carYear = findViewById(R.id.textViewCarYear);
        ratingBar = findViewById(R.id.ratingBar);
        averageRating = findViewById(R.id.textViewRatingAverage);
        countReviews = findViewById(R.id.textViewRatingsCount);
        muteMessage = findViewById(R.id.switchMute);
        recyclerView = findViewById(R.id.recyclerView);
        tableRowCar = findViewById(R.id.tableRowCar);
        imageExit.setOnClickListener(view -> finish());
        imageLeave.setOnClickListener(view -> finish());
        tableRowCar.setVisibility(View.GONE);
        participantId = getIntent().getStringExtra(User.class.getSimpleName());
        loadParticipantData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        List<User> drivers = new ArrayList<>();
        drivers.add(driver);
        riderRouteAdapter = new DriverRouteListAdapter(this, routeList, driver, new OnClickDriverRoute() {
            @Override
            public void editDriversRoute(View v, int position) {}
            @Override
            public void deleteDriversRoute(View v, int position) {}
            @Override
            public void itemClick(View v, int position) {
                Intent i =new Intent(ChatInfoActivity.this, RouteActivity.class);
                i.putExtra(Route.class.getSimpleName(),routeList.get(position).getRouteId());
                i.putExtra(Driver.class.getSimpleName(),routeList.get(position).getDriverId());
                startActivity(i);
            }
        });
        recyclerView.setAdapter(riderRouteAdapter);
        routeList.clear();
    }
    private void loadParticipantData() {
        User u = User.loadUserInstance(this);
        if (u.getType().equals(Rider.class.getSimpleName())){
            Driver.loadDriver(participantId,d->searchMutualRoutes((Driver) d, (Rider) u));
        }else {
            Rider.loadUser(participantId, r->searchMutualRoutes((Driver) u, (Rider) r));
        }
    }
    private void searchMutualRoutes(Driver driver, Rider rider){
        if (FirebaseAuth.getInstance().getUid().equals(rider.getUserId())){
            userName.setText(driver.getFullName());
            driver.loadCarImage(car -> {
                imageCar.setImageBitmap(null);
                imageCar.setBackgroundResource(0);
                if (car != null){
                    imageCar.setImageBitmap(car);
                }else {
                    imageCar.setBackgroundResource(R.drawable.ic_car);
                }
                carName.setText(driver.getOwnedCar().getCarName());
                carPlate.setText(driver.getOwnedCar().getCarPlates());
                carYear.setText(driver.getOwnedCar().getYear());
                imageCar.setVisibility(View.VISIBLE);
                tableRowCar.setVisibility(View.VISIBLE);
            });
        }else {
            userName.setText(rider.getFullName());
        }
        this.rider = rider;
        this.driver = driver;
        User.mutualRoutes(driver,rider,this::refreshData);
    }
    List<Route> routeList = new ArrayList<>();
    @SuppressLint("NotifyDataSetChanged")
    private void refreshData(Route r){
        routeList.add(r);
//        check if the driver exists
        riderRouteAdapter.notifyDataSetChanged();
    }


}