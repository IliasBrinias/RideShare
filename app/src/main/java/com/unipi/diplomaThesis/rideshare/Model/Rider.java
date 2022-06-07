package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.content.Context;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.diplomaThesis.rideshare.Interface.OnDataReturn;
import com.unipi.diplomaThesis.rideshare.Interface.OnProcedureComplete;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteSearchResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.R;

import java.util.ArrayList;
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
                            returnData.put("min",Float.parseFloat(r.getCostPerRider()));
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
                            returnData.put("max",Float.parseFloat(r.getCostPerRider()));
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
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0,0,0);
                                continue;
                            }
//                            check if the route has at least one seat free
                            if (r.isFull()) {
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0,0,0);
                                continue;
                            }
//                            check if is already rider
//                            boolean isRider = false;
//                            for (String p:r.getPassengersId()){
//                                if (FirebaseAuth.getInstance().getUid().equals(p)){
//                                    isRider = true;
//                                }
//                            }
//                            if (isRider) {
//                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0,0,0);
//                                continue;
//                            }
//                            check if the route is acceptable
                            routeFilter.filterCheck(c, r, (success, distanceDeviation) ->
                            {
                                if (!success) {
                                    if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0,0,0);
                                    return;
                                }
                                FirebaseDatabase.getInstance().getReference()
                                        .child(User.class.getSimpleName())
                                        .child(r.getDriverId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot user) {
                                                loadReviewTotalScore(user.getKey(),(totalScore, ReviewCount) ->{
                                                            onRouteSearchResponse.returnedData(r, user.getValue(User.class),distanceDeviation, totalScore, ReviewCount);
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0,0,0);
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
    public void makeRequest(Activity activity, User driver, Route route, Request request, OnCompleteListener<Void> onCompleteListener){
        request.setSeen(false);
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .child(request.getRouteId())
                .child(request.getRiderId())
                .setValue(request).addOnCompleteListener( task -> {
                    FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(
                        driver.getToken_FCM(),
                        Request.class.getSimpleName(),
                        null,
                        driver.getFullName(),
                        Rider.this.getFullName()+" "+activity.getString(R.string.is_instrested_for)+" "+route.getName(),
                        activity
                    );
                    fcmNotificationsSender.SendNotifications();
                    onCompleteListener.onComplete(task);
                });
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
    public void saveReview(String participantId, double rating, String description,OnCompleteListener<Void> onCompleteListener) {
        Review review = new Review(participantId,rating,description,new Date().getTime(),this.getUserId());
        FirebaseDatabase.getInstance().getReference()
                .child(Review.class.getSimpleName())
                .child(participantId)
                .child(this.getUserId())
                .setValue(review).addOnCompleteListener(onCompleteListener);
    }
    public void deleteAccount(String password,OnCompleteListener<Void> onCompleteListener){
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
                            deleteAccountDataDatabase(complete ->{
                                if (complete){
//                                    user.delete().addOnCompleteListener(onCompleteListener);
                                    onCompleteListener.onComplete(task);
                                }
                            });
                        }else {
                            onCompleteListener.onComplete(task);
                        }
                    }
                });
    }
    private void deleteAccountDataDatabase(OnProcedureComplete onProcedureComplete){
        deleteRiderData(new OnProcedureComplete() {
            @Override
            public void isComplete(boolean complete) {
                if (complete){
                    //        delete photos
                    StorageReference photo= FirebaseStorage.getInstance().getReference();
                    photo.child(User.class.getSimpleName()).child(Rider.this.getUserId()).delete()
                            .addOnCompleteListener(task ->
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(User.class.getSimpleName())
                                            .child(Rider.this.getUserId())
                                            .removeValue()
                                            .addOnCompleteListener(t->onProcedureComplete.isComplete(true))
                            );
                }
            }
        });
    }
    public void becomeDriver(Car c, byte[] carImage, OnCompleteListener<Void> onCompleteListener){
        deleteRiderData(complete -> {
            DatabaseReference userRef =
            FirebaseDatabase.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(FirebaseAuth.getInstance().getUid());
            userRef.child(REQ_TYPE_TAG).setValue(Driver.class.getSimpleName()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    loadSignInUser(d->{
                                ((Driver) d).saveCar(c,
                                        carImage,
                                        onCompleteListener,
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                e.printStackTrace();
                                            }
                                        });
                            }
                            );
                }
            });

        });
    }
    public void deleteRiderData(OnProcedureComplete onProcedureComplete){
        DatabaseReference userRef =
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(FirebaseAuth.getInstance().getUid());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        startProcedures();
//                        delete Messages
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
                                        for (String participantId: participants){
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
                            addProcedures();
                            userRef.child("messageSessionId").removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
                        }

//                      delete Routes
                        if (snapshot.hasChild("lastRoutes")) {
                            ArrayList<String> lastRoutes = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
                            for (String id : lastRoutes) {
                                DatabaseReference routeRef =
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Route.class.getSimpleName())
                                        .child(id);
                                        routeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("passengersId")){
                                                    ArrayList<String> passengersId = (ArrayList<String>) snapshot.child("passengersId").getValue();
                                                    if (passengersId.size()>0){
                                                        passengersId.remove(FirebaseAuth.getInstance().getUid());
                                                        routeRef.child("passengersId").setValue(passengersId);
                                                    }
                                                    addProcedures();
                                                    userRef.child("lastRoutes").removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });

//                          Remove Request
                            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                                    .child(Request.class.getSimpleName());

                                    requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot routeRequestMap:snapshot.getChildren()){
                                                if (routeRequestMap.hasChild(FirebaseAuth.getInstance().getUid())){
                                                    addProcedures();
                                                    requestRef.child(routeRequestMap.getKey())
                                                            .child(FirebaseAuth.getInstance().getUid())
                                                            .removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));

                                                }

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                            }
                        }
//                        Delete Reviews
                        FirebaseDatabase.getInstance().getReference()
                                .child(Review.class.getSimpleName())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot reviewedDriver:snapshot.getChildren()){
                                            if (reviewedDriver.hasChild(FirebaseAuth.getInstance().getUid())){
                                                addProcedures();
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(Review.class.getSimpleName())
                                                        .child(reviewedDriver.getKey())
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(isCompleted()));
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
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
}
