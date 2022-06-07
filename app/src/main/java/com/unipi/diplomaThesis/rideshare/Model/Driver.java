package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.unipi.diplomaThesis.rideshare.Interface.OnProcedureComplete;
import com.unipi.diplomaThesis.rideshare.Interface.OnRequestLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnReturnedIds;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.Interface.hasAllreadyMessageSession;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver extends User{
    public static final int REQ_CREATE_DRIVER_ACCOUNT=234;
    public static final int REQ_DISABLE_DRIVER_ACCOUNT=457;
    private Car ownedCar = new Car();

    public Driver() {
    }

    public Driver(String userId, String email, String fullName) {
        super(userId, email, fullName, Driver.class.getSimpleName());
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
        updateUserMode.put(User.REQ_TYPE_TAG,Driver.class.getSimpleName());
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
    public void saveRoute(Route r, OnCompleteListener<Void> onCompleteListener){
//      save route
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName());
        if (r.getRouteId()==null) r.setRouteId(database.push().getKey());
        database.child(r.getRouteId())
                .setValue(r).addOnCompleteListener(onCompleteListener);
//      save route id to drivers data
        DatabaseReference ref =
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(FirebaseAuth.getInstance().getUid())
                .child("lastRoutes");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> lastRoutes;
                        if (snapshot.getChildrenCount()==0){
                            lastRoutes = new ArrayList<>();
                        }else {
                            lastRoutes = (ArrayList<String>) snapshot.getValue();
                        }
                        if (!lastRoutes.contains(r.getRouteId())){
                            lastRoutes.add(r.getRouteId());
                            ref.setValue(lastRoutes);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//        Update the driver Object
    }
    public void loadDriverRoutes(OnRouteResponse onRouteResponse){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName());
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId())
                .child("lastRoutes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null) {
                            onRouteResponse.returnedRoute(null);
                            return;
                        };
                        ArrayList<String> routeIds = (ArrayList<String>) snapshot.getValue();
                        for (String id:routeIds) {
                            database.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
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
                    .child(riderId);
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
    private void createMessageSession(Activity activity, Driver d, String riderId){
        ArrayList<String> participants = new ArrayList<>();
        participants.add(this.getUserId());
        participants.add(riderId);
        MessageSession messageSession = new MessageSession(null, participants,(new Date()).getTime(),null);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName());
        String messageSessionId = databaseReference.push().getKey();
        messageSession.setMessageSessionId(messageSessionId);
        databaseReference.child(messageSessionId)
                .setValue(messageSession).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        makeMessageSessionVisibleToParticipants(participants,messageSessionId);
                        User.loadUser(riderId,u -> {
                            FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(
                                    u.getToken_FCM(),
                                    Request.class.getSimpleName(),
                                    messageSessionId,
                                    d.getFullName(),
                                    activity.getString(R.string.first_message_chat),
                                    activity
                                    );
                            fcmNotificationsSender.SendNotifications();
                        });
                    }
                });
    }
    private void makeMessageSessionVisibleToParticipants(ArrayList<String> participants, String sessionId){
        for (String p:participants){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(p);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    get participant sessionIds and check add the new one
                    ArrayList<String> messageSessionId;
                    if (!snapshot.hasChild("messageSessionId")) {
                        messageSessionId = new ArrayList<>();
                    }else {
                        messageSessionId = (ArrayList<String>) snapshot.child("messageSessionId").getValue();
                    }
                    for (String id:messageSessionId)
                    {
                        if (id.equals(sessionId)){
                            return;
                        }
                    }
                    messageSessionId.add(sessionId);
                    ref.child("messageSessionId").setValue(messageSessionId);
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
            FirebaseStorage.getInstance().getReference()
                    .child(Car.class.getSimpleName())
                    .child(this.getUserId()).getFile(f)
                    .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
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
                .child(Car.class.getSimpleName())
                .child(this.getUserId())
                .putBytes(carImage)
                .addOnCompleteListener(onCompleteListener);
    }

    public void loadRequests(OnRequestLoad onRequestLoad){
        loadDriverRoutes(route->{
            if (route == null) onRequestLoad.returnedRequest(null);
            FirebaseDatabase.getInstance().getReference()
                    .child(Request.class.getSimpleName())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(route.getRouteId())){
                                for (DataSnapshot request:snapshot.child(route.getRouteId()).getChildren()){
                                    onRequestLoad.returnedRequest(request.getValue(Request.class));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });
    }
    public void loadDriversRouteId(OnReturnedIds onReturnedIds){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId())
                .child("lastRoutes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> driverRoutesId = (ArrayList<String>) snapshot.getValue();
                        if (driverRoutesId == null) return;
                        onReturnedIds.returnIds(driverRoutesId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    public void acceptRequest(Activity activity,Request request){
//        create Messages Session
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .child(request.getRouteId())
                .child(request.getRiderId())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
//                            check if the messageSessionExists
                            checkIfRiderHasMessageSessionWithDriver(request.getRiderId(),Driver.this.getUserId(),exists->{
//                                if is not exist create a new one
                                if (!exists){
                                    createMessageSession(activity,Driver.this,request.getRiderId());
                                }
//                                add the rider to the Route and Route to riders lastRoutes
                                addRiderToRoute(request.getRouteId(),request.getRiderId());
                                addRouteToRiders(request.getRouteId(),request.getRiderId());
                            });
                        }
                    }
                });

    }

    private void addRouteToRiders(String routeId, String riderId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(riderId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> lastRoutes;
                        if (snapshot.hasChild("lastRoutes")){
                            lastRoutes = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
                        }else {
                            lastRoutes = new ArrayList<>();
                        }
                        lastRoutes.add(routeId);
                        ref.child("lastRoutes").setValue(lastRoutes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIfRiderHasMessageSessionWithDriver(String riderId, String driverId, hasAllreadyMessageSession hasAllreadyMessageSession){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
                ref.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot driver) {
//                        check if driver has MessageSession and get his arrayList
                        if (driver.hasChild("messageSessionId")){
                            ArrayList<String> driverMessageSessionId = (ArrayList<String>) driver.child("messageSessionId").getValue();
                            if (driverMessageSessionId == null) {
                                hasAllreadyMessageSession.isExists(false);
                                return;
                            }
                            ref.child(riderId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot rider) {
//                                    check if the rider has MessageSession and get his array list
                                    if (rider.hasChild("messageSessionId")){
                                        ArrayList<String> riderMessageSessionId = (ArrayList<String>) rider.child("messageSessionId").getValue();
                                        if (riderMessageSessionId == null){
                                            hasAllreadyMessageSession.isExists(false);
                                            return;
                                        }
//                                        if the rider and drier has a mutual session id return true
                                        for (String riderId:riderMessageSessionId){
                                            if (driverMessageSessionId.contains(riderId)){
                                                hasAllreadyMessageSession.isExists(true);
                                                return;
                                            }
                                            hasAllreadyMessageSession.isExists(false);
                                        }
                                    }else {
                                        hasAllreadyMessageSession.isExists(false);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }else {
                            hasAllreadyMessageSession.isExists(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void addRiderToRoute(String routeId, String riderId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .child(routeId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> passengersId;
                if (!snapshot.hasChild("passengersId")){
                    passengersId = new ArrayList<>();
                }else {
                    passengersId = (ArrayList<String>) snapshot.child("passengersId").getValue();
                }
                passengersId.add(riderId);
                ref.child("passengersId").setValue(passengersId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void declineRequest(Request request) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(Request.class.getSimpleName())
                .child(request.getRouteId()).removeValue();
    }

    public void deleteAccount(String password, OnCompleteListener<Void> onCompleteListener){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(this.getEmail(), password);
        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            deleteDatabaseData(complete ->{
                                if (complete){
                                    user.delete().addOnCompleteListener(onCompleteListener);
                                }
                            });
                        }else {
                            onCompleteListener.onComplete(task);
                        }
                    }
                });
    }
    public void deleteDatabaseData(OnProcedureComplete onProcedureComplete){
        DatabaseReference userRef =
                FirebaseDatabase.getInstance().getReference()
                        .child(User.class.getSimpleName())
                        .child(FirebaseAuth.getInstance().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                startProcedures();

//              delete Messages
                if (snapshot.hasChild("messageSessionId")){
                    ArrayList<String> messageSessionId = (ArrayList<String>) snapshot.child("messageSessionId").getValue();

//                            User MessageSessionIds
                    for (String sessionId:messageSessionId){
                        DatabaseReference messageSessionRef =
                                FirebaseDatabase.getInstance().getReference()
                                        .child(MessageSession.class.getSimpleName())
                                        .child(sessionId);

                        messageSessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                        MessageSession Participants
                                ArrayList<String> participants = (ArrayList<String>) snapshot.child("participants").getValue();
                                participants.remove(FirebaseAuth.getInstance().getUid());
                                String participantId = participants.get(0);
                                    DatabaseReference participantRef =
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child(User.class.getSimpleName())
                                                    .child(participantId);
                                    participantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
    //                                                    deleteMessageSessionId from the other participants
                                            ArrayList<String> messageSessionId = (ArrayList<String>) snapshot.child("messageSessionId").getValue();
                                            if (!messageSessionId.contains(sessionId)) return;
                                            addProcedures();
                                            messageSessionId.remove(sessionId);
                                            participantRef.child("messageSessionId").setValue(messageSessionId);
    //                                                    finally remove the Message Session Completely
                                            messageSessionRef.removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    addProcedures();
                    userRef.child("messageSessionId").removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
                }

//                      delete Routes and Requests
                if (snapshot.hasChild("lastRoutes")) {
                    ArrayList<String> lastRoutes = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
                    for (String routeId : lastRoutes) {
                        DatabaseReference routeRef =
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Route.class.getSimpleName())
                                        .child(routeId);
                        routeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("passengersId")){
                                    ArrayList<String> passengersId = (ArrayList<String>) snapshot.child("passengersId").getValue();
                                    for (String userId:passengersId){
                                        DatabaseReference userRef =
                                        FirebaseDatabase.getInstance().getReference()
                                                .child(User.class.getSimpleName())
                                                .child(userId);
                                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("lastRoutes")){
                                                    ArrayList<String> lastRoutes = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
                                                    if (lastRoutes.contains(routeId)){
                                                        lastRoutes.remove(routeId);
                                                        addProcedures();
                                                        userRef.child("lastRoutes").setValue(lastRoutes)
                                                                .addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                                    }
                                }
                                addProcedures();
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Request.class.getSimpleName())
                                        .child(routeId).removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));

                                addProcedures();
                                routeRef.removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                    }
                }
//              Delete Reviews
                addProcedures();
                FirebaseDatabase.getInstance().getReference()
                        .child(Review.class.getSimpleName())
                        .child(FirebaseAuth.getInstance().getUid())
                        .removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
//              delete User
                addProcedures();
                userRef.removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
//              deletePhotos
                StorageReference photo = FirebaseStorage.getInstance().getReference();
                addProcedures();
                photo.child(User.class.getSimpleName()).child(Driver.this.getUserId()).delete()
                        .addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()))
                        .addOnFailureListener(task -> onProcedureComplete.isComplete(isCompleted()));
                addProcedures();
                photo.child(Car.class.getSimpleName()).child(Driver.this.getUserId()).delete()
                        .addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()))
                        .addOnFailureListener(task -> onProcedureComplete.isComplete(isCompleted()));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private static int proceduresCount;
    private void startProcedures(){
        proceduresCount = 0;
    }
    private void addProcedures(){
        proceduresCount++;
    }
    private boolean isCompleted(){
        proceduresCount--;
        return proceduresCount == 0;
    }
    public void deleteRoute(Route deletedRoute){
//        delete all the route data
        DatabaseReference routeRef = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .child(deletedRoute.getRouteId());
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        routeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                delete route from the passengers
                if (snapshot.hasChild("passengersId")){
                    ArrayList<String> passengerId = (ArrayList<String>) snapshot.child("passengersId").getValue();
                        deleteRoutePassengers(deletedRoute.getRouteId(),passengerId, task -> {
                        userRef.child(Driver.this.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> routeId = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
                                routeId.remove(deletedRoute.getRouteId());
                                userRef.child(Driver.this.getUserId()).child("lastRoutes").setValue(routeId);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    });
                }
                //        delete Route
                routeRef.removeValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
//        deleteRequest
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                        .child(deletedRoute.getRouteId()).removeValue();

    }

    public void deletePassenger(String routeId, String passengerId, OnCompleteListener<Void> onCompleteListener){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        DatabaseReference messageSessionRef = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName());

        System.out.println(routeId+" "+passengerId);
        usersRef.child(this.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot driver) {
                        ArrayList<String> driverLastRoutes = (ArrayList<String>) driver.child("lastRoutes").getValue();
                        ArrayList<String> driverMessageSessionId = (ArrayList<String>) driver.child("messageSessionId").getValue();
                        System.out.println("driverLastRoutes "+driverLastRoutes);
                        System.out.println("driverMessageSessionId "+driverMessageSessionId);
                        usersRef.child(passengerId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot passenger) {
                                        ArrayList<String> passengerLastRoutes = (ArrayList<String>) passenger.child("lastRoutes").getValue();
                                        ArrayList<String> passengerMessageSessionId = (ArrayList<String>) passenger.child("messageSessionId").getValue();
                                        System.out.println("passengerLastRoutes "+passengerLastRoutes+" "+ passengerId);
                                        System.out.println("passengerMessageSessionId "+passengerMessageSessionId+" "+ passengerId);
                                        passengerLastRoutes.remove(routeId);
                                        driverLastRoutes.remove(routeId);
                                        if (!hasMutualRoutes(driverLastRoutes,passengerLastRoutes)){
//                                          if they don't have other mutual route delete the Messages
                                            for (String id:driverMessageSessionId){
                                                if (passengerMessageSessionId.contains(id)){
                                                    driverMessageSessionId.remove(id);
                                                    passengerMessageSessionId.remove(id);
                                                    messageSessionRef.child(id).removeValue();
                                                    break;
                                                }
                                            }
                                        }
                                        usersRef.child(passengerId).child("lastRoutes").setValue(passengerLastRoutes).addOnCompleteListener(onCompleteListener);
                                        usersRef.child(passengerId).child("messageSessionId").setValue(passengerMessageSessionId);
                                        usersRef.child(Driver.this.getUserId()).child("messageSessionId").setValue(driverMessageSessionId);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public void deleteRoutePassengers(String routeId, List<String> passengersId, OnCompleteListener<Void> onCompleteListener){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        DatabaseReference messageSessionRef = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName());

        usersRef.child(this.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot driver) {
                        ArrayList<String> driverLastRoutes = (ArrayList<String>) driver.child("lastRoutes").getValue();
                        ArrayList<String> driverMessageSessionId = (ArrayList<String>) driver.child("messageSessionId").getValue();
                        for (String id:passengersId) {
                            usersRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot passenger) {
                                    ArrayList<String> passengerLastRoutes = (ArrayList<String>) passenger.child("lastRoutes").getValue();
                                    ArrayList<String> passengerMessageSessionId = (ArrayList<String>) passenger.child("messageSessionId").getValue();
                                    passengerLastRoutes.remove(routeId);
                                    if (!hasMutualRoutes(driverLastRoutes, passengerLastRoutes)) {
//                                          if they don't have other mutual route delete the Messages
                                        for (String id : driverMessageSessionId) {
                                            if (passengerMessageSessionId.contains(id)) {
                                                driverMessageSessionId.remove(id);
                                                passengerMessageSessionId.remove(id);
                                                messageSessionRef.child(id).removeValue();
                                                break;
                                            }
                                        }
                                    }
                                    usersRef.child(id).child("lastRoutes").setValue(passengerLastRoutes);
                                    usersRef.child(id).child("messageSessionId").setValue(passengerMessageSessionId);
                                    if (passengersId.indexOf(id) == passengersId.size()-1){
                                        usersRef.child(Driver.this.getUserId()).child("messageSessionId").setValue(driverMessageSessionId).addOnCompleteListener(onCompleteListener);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private boolean hasMutualRoutes(ArrayList<String> driverLastRoutes, ArrayList<String> passengerLastRoutes) {
        for (String driverRouteId:driverLastRoutes){
            if (passengerLastRoutes.contains(driverRouteId)) {
                return true;
            }
        }
        return false;
    }
}
