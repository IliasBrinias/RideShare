package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.unipi.diplomaThesis.rideshare.CarFragment;
import com.unipi.diplomaThesis.rideshare.Interface.OnCompleteRoutesLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver extends User{
    private Car ownedCar = new Car();
    public Driver() {
    }

    public Driver(String userId, String email, String fullName, String description, Map<String,String> lastRoutes) {
        super(userId, email, fullName, description, lastRoutes);
    }
    public Car getOwnedCar() {
        return ownedCar;
    }
    public void setOwnedCar(Car ownedCar) {
        this.ownedCar = ownedCar;
    }

    public static void createDriver(String driverId, OnUserLoadComplete onUserLoadComplete){
//        update User mode {Rider or Driver with a boolean Variable}
        Map<String,Object> updateUserMode = new HashMap<>();
        updateUserMode.put(User.REQ_IS_DRIVER_TAG,true);
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(driverId)
                .updateChildren(updateUserMode).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                load Driver Object From Firebase
                loadDriver(driverId,onUserLoadComplete);
            }
        });

    }
    public static void loadDriver(String driverId, OnUserLoadComplete onUserLoadComplete){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try{
                            if (snapshot.child(User.REQ_IS_DRIVER_TAG).getValue(Boolean.class) == true) {
                                onUserLoadComplete.returnedUser(snapshot.getValue(Driver.class));
                            } else {
                                onUserLoadComplete.returnedUser(null);
                            }
                        }catch (NullPointerException ignore){
                            ignore.printStackTrace();
                            onUserLoadComplete.returnedUser(null);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    public void loadDriverRoutes(OnCompleteRoutesLoad onCompleteRoutesLoad){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child(Route.class.getSimpleName())
                .child(getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Route> routeList = new ArrayList<>();
                        for (DataSnapshot routes:snapshot.getChildren()){
                            routeList.add(routes.getValue(Route.class));
                        }
                        onCompleteRoutesLoad.returnedRoutes(routeList);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    public void saveRoute(Route r, Context c, OnCompleteListener<Void> onCompleteListener){
//      save route
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName());
        if (r.getRouteId()==null) r.setRouteId(database.push().getKey());
        database.child(r.getRouteId()).setValue(r).addOnCompleteListener(onCompleteListener);
//      save route id to drivers data
        this.getLastRoutes().put(r.getRouteId(),"active");
//        Update the driver Object
        updateUserInstance(c);
    }
    public void loadDriverRoutes(OnRouteResponse onRouteResponse){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName());
//        return all the driver routes
        if (this.getLastRoutes().isEmpty()) return;
        for (Map.Entry<String,String> route:this.getLastRoutes().entrySet()){
            database.child(route.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Route route = snapshot.getValue(Route.class);
                    onRouteResponse.returnedRoute(route);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
    public void deleteRoute(Route deletedRoute, Context c){
//        delete all the route data
        FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .child(deletedRoute.getRouteId()).removeValue();
//        delete route id from the driver route list
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(Driver.class.getSimpleName())
                .child(this.getUserId())
                .child(Route.class.getSimpleName())
                .child(deletedRoute.getRouteId())
                .removeValue();
        this.getLastRoutes().remove(deletedRoute.getRouteId());
        this.updateUserInstance(c);
    }
    public void saveCar(Car c, OnCompleteListener<Void> onCompleteListener){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId())
                .child("ownedCar")
                .setValue(c).addOnCompleteListener(onCompleteListener);
    }
}
