package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteSearchResponse;

import java.util.Map;

public class Rider extends User{

    public Rider() {
    }

    public Rider(String userId, String email, String name, String description, Map<String,String> lastRoutes) {
        super(userId, email, name, description, lastRoutes);
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
