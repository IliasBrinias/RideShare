package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.unipi.diplomaThesis.rideshare.Interface.OnCompleteRoutesLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Driver extends Rider{
    private List<Car> ownedCars;
    public Driver() {
    }

    public Driver(String userId, String email, String firstName, String lastName, String description, Map<String,String> lastRoutes, List<Car> ownedCars) {
        super(userId, email, firstName, description, lastRoutes);
        this.ownedCars = ownedCars;
    }
    public List<Car> getOwnedCars() {
        return ownedCars;
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
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
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
                public void onCancelled(@NonNull DatabaseError error) {

                }
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
}
