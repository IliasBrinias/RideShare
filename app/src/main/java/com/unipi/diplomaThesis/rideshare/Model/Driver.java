package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unipi.diplomaThesis.rideshare.Interface.OnImageLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Driver extends User{
    public static final int REQ_CREATE_DRIVER_ACCOUNT=234;
    public static final int REQ_DISABLE_DRIVER_ACCOUNT=457;
    private Car ownedCar = new Car();
    public Driver() {
    }

    public Driver(String userId, String email, String fullName, String description, ArrayList<String> lastRoutes) {
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
        updateUserMode.put(User.REQ_TYPE_TAG,true);
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
                            if (snapshot.child(User.REQ_TYPE_TAG).getValue(String.class).equals(Driver.class.getSimpleName())){
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
    public void saveRoute(Route r, Context c, OnCompleteListener<Void> onCompleteListener){
//      save route
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName());
        if (r.getRouteId()==null) r.setRouteId(database.push().getKey());
        database.child(r.getRouteId()).setValue(r).addOnCompleteListener(onCompleteListener);
//      save route id to drivers data
        this.getLastRoutes().add(r.getRouteId());
//        Update the driver Object
        updateUserInstance(c);
    }
    public void loadDriverRoutes(OnRouteResponse onRouteResponse){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName());
//        return all the driver routes
        if (this.getLastRoutes().isEmpty()) return;
        for (String routeId:this.getLastRoutes()){
            database.child(routeId).addListenerForSingleValueEvent(new ValueEventListener() {
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
    public void saveCar(Car c, byte[] carImage, OnCompleteListener<Void> onCompleteListener, OnFailureListener onFailureListener){
        saveCarImage(carImage, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference()
                            .child(User.class.getSimpleName())
                            .child(Driver.this.getUserId())
                            .child("ownedCar")
                            .setValue(c).addOnCompleteListener(onCompleteListener);
                }else {
                    onFailureListener.onFailure(task.getException());
                }
            }
        });

    }
    public void loadRouteRiderIcon(String riderId, OnImageLoad onImageLoad){
        try {
            File f = File.createTempFile(riderId, ".jpg");
            StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(riderId)
                    .child(riderId+".jpg");
            imageRef.getFile(f).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        onImageLoad.loadImageSuccess(BitmapFactory.decodeFile(f.getAbsolutePath()));
                    }else {
                        onImageLoad.loadImageSuccess(null);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void acceptRequest(Request request){
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .child(request.getRouteId())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("acceptRequest", "RequestDeleted");
                            createMessageSession(request.getRiderId());
                        }
                    }
                });

    }
    private void createMessageSession(String riderId){
        ArrayList<String> participants = new ArrayList<>();
        participants.add(this.getUserId());
        participants.add(riderId);
        Log.d("createMessageSession", "participant Map:"+participants);

        MessageSession messageSession = new MessageSession(null, participants,(new Date()).getTime(),null);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName());
        String messageSessionId = databaseReference.push().getKey();
        messageSession.setMessageSessionId(messageSessionId);
        databaseReference.child(messageSessionId)
                .setValue(messageSession).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("createMessageSession", "MessageSession Created with id:"+messageSessionId);
                        makeMessageSessionVisibleToParticipants(participants,messageSessionId);
                    }
                });
    }
    private void makeMessageSessionVisibleToParticipants(ArrayList<String> participants, String sessionId){
        for (String p:participants){
            Log.d("messageSessionId", "Current User "+p);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(p);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String,String> messageSessionId = new HashMap<>();
                    if (snapshot.hasChild("messageSessionId")){
                        messageSessionId = (Map<String, String>) snapshot.child("messageSessionId");
                        for (Map.Entry<String,String> messageSession:messageSessionId.entrySet())
                        {
                            if (messageSession.getValue().equals(sessionId)){
                                return;
                            }
                        }
                    }
                    messageSessionId.put(String.valueOf(messageSessionId.size()),sessionId);
                    Log.d("UserMessageSession", p+" updated messageSession "+messageSessionId);
                    ref.child("messageSessionId").setValue(messageSessionId).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.d("messageSessionId", "saved Successfully for the "+p);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    public void loadCarImage(OnImageLoad onImageLoad){
        try {
            File f = File.createTempFile("car", ".jpg");
            StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(this.getUserId())
                    .child("car.jpg");
            imageRef.getFile(f).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        onImageLoad.loadImageSuccess(BitmapFactory.decodeFile(f.getAbsolutePath()));
                    }else {
                        onImageLoad.loadImageSuccess(null);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveCarImage(byte[] carImage, OnCompleteListener<UploadTask.TaskSnapshot> onCompleteListener){
        FirebaseStorage.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId())
                .child("car.jpg")
                .putBytes(carImage)
                .addOnCompleteListener(onCompleteListener);
    }

}
