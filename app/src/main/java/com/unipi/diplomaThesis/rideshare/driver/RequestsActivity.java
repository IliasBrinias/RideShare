package com.unipi.diplomaThesis.rideshare.driver;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.diplomaThesis.rideshare.Interface.RequestOnClickListener;
import com.unipi.diplomaThesis.rideshare.Model.Driver;
import com.unipi.diplomaThesis.rideshare.Model.MyApplication;
import com.unipi.diplomaThesis.rideshare.Model.Request;
import com.unipi.diplomaThesis.rideshare.Model.User;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.adapter.RequestAdapter;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {
    protected MyApplication mMyApp;

    ImageView imageViewBack;
    RecyclerView recyclerView;
    RequestAdapter requestAdapter;
    List<Request> requestList = new ArrayList<>();
    Driver driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        try {
            driver = (Driver) User.loadUserInstance(this);
            if (driver == null) throw new ClassCastException();
        }catch (ClassCastException classCastException){
            finish();
        }
        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        requestAdapter = new RequestAdapter(requestList, new RequestOnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void accept(View view, int position) {
                driver.acceptRequest(requestList.get(position));
                requestList.remove(position);
                requestAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void decline(View view, int position) {
                driver.declineRequest(requestList.get(position));
                requestList.remove(position);
                requestAdapter.notifyDataSetChanged();
                if (requestList.isEmpty()) finish();
            }
        });
        recyclerView.setAdapter(requestAdapter);
        requestSearch();
        mMyApp = (MyApplication) this.getApplicationContext();

    }
    private void requestSearch(){
        driver.loadRequests(this::refreshData);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void refreshData(Request request){
        for (Request r:requestList){
            if (r.getRouteId().equals(request.getRouteId()) && r.getRiderId().equals(request.getRiderId())){
                requestList.remove(r);
                break;
            }
        }
        requestList.add(request);
        requestAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}