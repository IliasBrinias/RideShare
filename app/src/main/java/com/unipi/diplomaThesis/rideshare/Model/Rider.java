package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.Interface.OnDataReturn;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteSearchResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Rider extends User{

    public Rider(){
        super();
    }

    public Rider(String userId, String email, String name) {
        super(userId, email, name, Rider.class.getSimpleName());
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
    private static int routeCount=0;
    public void routeSearch(Context c, RouteFilter routeFilter, OnRouteSearchResponse onRouteSearchResponse){
        FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        routeCount = Math.toIntExact(snapshot.getChildrenCount()-1);
                        for (DataSnapshot route:snapshot.getChildren()) {
                            Route r = route.getValue(Route.class);
//                            check if route exists
                            if (r==null) {
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0);
                                continue;
                            }
//                            check if the route has at least one seat free
                            if (r.isFull()) {
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0);
                                continue;
                            }
//                            check if is already rider
                            boolean isRider = false;
                            for (String p:r.getPassengersId()){
                                if (FirebaseAuth.getInstance().getUid().equals(p)){
                                    isRider = true;
                                }
                            }
                            if (isRider) {
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0);
                                continue;
                            }
//                            check if the route is acceptable
                            routeFilter.filterCheck(c, r, (success, distanceDeviation) ->
                            {
                                if (!success) {
                                    if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0);
                                    return;
                                }
                                FirebaseDatabase.getInstance().getReference()
                                        .child(User.class.getSimpleName())
                                        .child(r.getDriverId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot user) {
                                                onRouteSearchResponse.returnedData(r, user.getValue(User.class),distanceDeviation);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0);
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private boolean checkIfIsTheLastRoute(){
        if (routeCount==0){
            return true;
        }else {
            routeCount--;
            return false;
        }
    }
    public void makeRequest(Request request, OnCompleteListener<Void> onCompleteListener){
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .child(request.getRouteId())
                .child(request.getRiderId())
                .setValue(request).addOnCompleteListener(onCompleteListener);
    }
    public static void loadUser(String userId, OnUserLoadComplete onUserLoadComplete){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        onUserLoadComplete.returnedUser(snapshot.getValue(Rider.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void saveReview(String participantId, double rating, String description) {
        Review review = new Review(participantId,rating,description,new Date().getTime(),this.getUserId());
        FirebaseDatabase.getInstance().getReference()
                .child(Review.class.getSimpleName())
                .child(participantId)
                .child(this.getUserId())
                .setValue(review);
    }
}
