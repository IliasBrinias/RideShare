package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.Interface.OnDataReturn;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteSearchResponse;

import java.util.HashMap;
import java.util.Map;

public class Rider extends User{

    public Rider() {
    }

    public Rider(String userId, String email, String name, String description, Map<String,String> lastRoutes) {
        super(userId, email, name, description, lastRoutes);
    }
    public void findMinMaxPrice(OnDataReturn onDataReturn){
        FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .orderByChild("costPerRider")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String,Object> returnData = new HashMap<>();
                        for (DataSnapshot route:snapshot.getChildren()) {
                            Route r = route.getValue(Route.class);
                            returnData.put("max",Float.parseFloat(r.getCostPerRider()));
                        }
                        onDataReturn.returnData(returnData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .orderByChild("costPerRider")
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String,Object> returnData = new HashMap<>();
                        for (DataSnapshot route:snapshot.getChildren()) {
                            Route r = route.getValue(Route.class);
                            returnData.put("min",Float.parseFloat(r.getCostPerRider()));
                        }
                        onDataReturn.returnData(returnData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void routeSearch(Context c, RouteFilter routeFilter, OnRouteSearchResponse onRouteSearchResponse){
        FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot route:snapshot.getChildren()) {
//                            calculate the distance
                            Route r = route.getValue(Route.class);
                            if (r==null) continue;
//                            check if the route is acceptable
                            routeFilter.filterCheck(c, r, success ->
                            {
                                if (!success) return;
                                FirebaseDatabase.getInstance().getReference()
                                        .child(User.class.getSimpleName())
                                        .child(r.getDriverId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot user) {
                                                onRouteSearchResponse.returnedData(r, user.getValue(User.class));
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void makeRequest(Request request, OnCompleteListener<Void> onCompleteListener){
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .child(request.getRouteId())
                .child(request.getRiderId())
                .setValue(request).addOnCompleteListener(onCompleteListener);
    }
}
