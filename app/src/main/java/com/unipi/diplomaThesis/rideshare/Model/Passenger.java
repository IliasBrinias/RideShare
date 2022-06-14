package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
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
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteSearchResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Passenger extends User{

    public Passenger(){
        super();
    }

    public Passenger(String userId, String email, String name) {
        super(userId, email, name, Passenger.class.getSimpleName());
    }

    /**
     * call for RouteFilter Activity. Returns the Min and Max price of the routes
     * @param onDataReturn
     */
    public void findMinMaxPrice(OnDataReturn onDataReturn){
        FirebaseDatabase.getInstance().getReference()
                .child(Routes.class.getSimpleName())
                .orderByChild("costPerRider")
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String,Object> returnData = new HashMap<>();
                        for (DataSnapshot route:snapshot.getChildren()) {
                            Routes r = route.getValue(Routes.class);
                            returnData.put("min",r.getCostPerRider());
                        }
                        onDataReturn.returnData(returnData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        FirebaseDatabase.getInstance().getReference()
                .child(Routes.class.getSimpleName())
                .orderByChild("costPerRider")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String,Object> returnData = new HashMap<>();
                        for (DataSnapshot route:snapshot.getChildren()) {
                            Routes r = route.getValue(Routes.class);
                            returnData.put("max",r.getCostPerRider());
                        }
                        onDataReturn.returnData(returnData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private static int routeCount=0;

    /**
     * search for routes based on Routes filter
     * @param c
     * @param routeFilter
     * @param onRouteSearchResponse
     */
    public void routeSearch(Context c, RouteFilter routeFilter, OnRouteSearchResponse onRouteSearchResponse){
        FirebaseDatabase.getInstance().getReference().child(Routes.class.getSimpleName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        routeCount = Math.toIntExact(snapshot.getChildrenCount()-1);
                        for (DataSnapshot route:snapshot.getChildren()) {
                            Routes r = route.getValue(Routes.class);
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
                            boolean isRider = false;
                            for (String p:r.getPassengersId()){
                                if (FirebaseAuth.getInstance().getUid().equals(p)){
                                    isRider = true;
                                }
                            }
                            if (isRider) {
                                if (checkIfIsTheLastRoute()) onRouteSearchResponse.returnedData(null,null,0,0,0);
                                continue;
                            }
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
                                                loadReviewTotalScore(user.getKey(),(totalScore, ReviewCount) ->
                                                onRouteSearchResponse.returnedData(r, user.getValue(User.class),distanceDeviation, totalScore, ReviewCount));
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
                    public void onCancelled(@NonNull DatabaseError error) {}
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

    /**
     * Create a request and sends a notification to driver
     * @param activity
     * @param driver
     * @param routes
     * @param request
     * @param onCompleteListener
     */
    public void makeRequest(Activity activity, User driver, Routes routes, Request request, OnCompleteListener<Void> onCompleteListener){
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
                        Passenger.this.getFullName()+" "+activity.getString(R.string.is_instrested_for)+" "+ routes.getName(),
                        activity
                    );
                    fcmNotificationsSender.SendNotifications();
                    onCompleteListener.onComplete(task);
                });
    }

    /**
     * load Passenger from the Firebase. If is not Passenger returns null
     * @param userId
     * @param onUserLoadComplete
     */
    public static void loadUser(String userId, OnUserLoadComplete onUserLoadComplete){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        onUserLoadComplete.returnedUser(snapshot.getValue(Passenger.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /**
     * Save review for the Driver
     * @param participantId
     * @param rating
     * @param description
     * @param onCompleteListener
     */
    public void saveReview(String participantId, double rating, String description,OnCompleteListener<Void> onCompleteListener) {
        Reviews reviews = new Reviews(participantId,rating,description,new Date().getTime(),this.getUserId());
        FirebaseDatabase.getInstance().getReference()
                .child(Reviews.class.getSimpleName())
                .child(participantId)
                .child(this.getUserId())
                .setValue(reviews).addOnCompleteListener(onCompleteListener);
    }

    /**
     * Deletes Passenger account:
     *  1. delete his id from the route participant
     *  2. delete id MessageSessions from the Drivers and the Messages Session
     *  3. delete the Requests
     *  4. deletes the Reviews
     *  5. delete account
     *  6. delete auth account
     * @param password
     * @param onCompleteListener
     */
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
                            deleteAccountDataDatabase((t) ->{
                                if (!t.isSuccessful()) t.getException().printStackTrace();
                                user.delete().addOnCompleteListener(onCompleteListener);
                            });
                        }else {
                            onCompleteListener.onComplete(task);
                        }
                    }
                });
    }
    private void deleteAccountDataDatabase(OnCompleteListener<Void> onCompleteListener){
        deleteRiderData(task -> {
                //        delete photos
                StorageReference photo= FirebaseStorage.getInstance().getReference();
                photo.child(User.class.getSimpleName()).child(Passenger.this.getUserId())
                        .delete()
                        .addOnCompleteListener(t1 -> FirebaseDatabase.getInstance().getReference()
                                .child(User.class.getSimpleName())
                                .child(Passenger.this.getUserId())
                                .removeValue()
                                .addOnCompleteListener(onCompleteListener));
        });
    }
    private void deleteMessageSession(ArrayList<String> messageSessionId, OnCompleteListener<Void> onCompleteListener){
//      User MessageSessionIds
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        DatabaseReference messageSessionRef = FirebaseDatabase.getInstance().getReference()
                .child(MessageSessions.class.getSimpleName());
        messageSessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                  MessageSessions
                final ArrayList<Task<Void>> tasks = new ArrayList<>();
                Map<String,String> sessions = new HashMap<>();
                for (String sessionId:messageSessionId) {
                    ArrayList<String> participants = (ArrayList<String>) snapshot.child(sessionId).child("participants").getValue();
                    participants.remove(FirebaseAuth.getInstance().getUid());
                    String participantId = participants.get(0);
                    sessions.put(sessionId,participantId);
                    final TaskCompletionSource<Void> sourceMessageSession = new TaskCompletionSource<>();
                    messageSessionRef.child(sessionId).removeValue()
                            .addOnCompleteListener(task -> sourceMessageSession.setResult(task.getResult()));
                    tasks.add(sourceMessageSession.getTask());
                }

                Tasks.whenAll(tasks).addOnCompleteListener(task -> {
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final ArrayList<Task<Void>> tasks = new ArrayList<>();
//                              deleteMessageSessionId from the other users
                                for (Map.Entry<String,String> sessionEntry:sessions.entrySet()){
                                    ArrayList<String> messageSessionId = (ArrayList<String>) snapshot.child(sessionEntry.getValue()).child("messageSessionId").getValue();
                                    if (!messageSessionId.contains(sessionEntry.getKey())) continue;
                                    messageSessionId.remove(sessionEntry.getKey());
                                    final TaskCompletionSource<Void> sourceParticipant = new TaskCompletionSource<>();
                                    userRef.child(sessionEntry.getValue()).child("messageSessionId").setValue(messageSessionId)
                                            .addOnCompleteListener(task -> sourceParticipant.setResult(task.getResult()));
                                    tasks.add(sourceParticipant.getTask());
                                }
                                Tasks.whenAll(tasks).addOnCompleteListener(onCompleteListener);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
    public void deleteRiderData(OnCompleteListener<Void> onCompleteListener){
        DatabaseReference userRef =
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(FirebaseAuth.getInstance().getUid());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot rider) {
                        final ArrayList<Task<Void>> tasks = new ArrayList<>();

//                        delete Messages
                        if (rider.hasChild("messageSessionId")){
                            ArrayList<String> messageSessionId = (ArrayList<String>) rider.child("messageSessionId").getValue();
                            final TaskCompletionSource<Void> sourceSess = new TaskCompletionSource<>();
                            deleteMessageSession(messageSessionId, task -> {
                                sourceSess.setResult(task.getResult());
                            });
                            tasks.add(sourceSess.getTask());
                        }
//                      delete Routes passengers
                        DatabaseReference routeRef = FirebaseDatabase.getInstance().getReference()
                                .child(Routes.class.getSimpleName());
//                          Remove Request

                        if (rider.hasChild("lastRoutes")) {
                            ArrayList<String> lastRoutes = (ArrayList<String>) rider.child("lastRoutes").getValue();


                                routeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot routeSnapshot) {
                                        for (String id : lastRoutes) {
                                            if (routeSnapshot.child(id).hasChild("passengersId")) {
                                                ArrayList<String> passengersId = (ArrayList<String>) routeSnapshot.child(id).child("passengersId").getValue();
                                                if (passengersId.contains(FirebaseAuth.getInstance().getUid())) {
                                                    passengersId.remove(FirebaseAuth.getInstance().getUid());
                                                    final TaskCompletionSource<Void> sourcePass = new TaskCompletionSource<>();
                                                    routeRef.child(id).child("passengersId").setValue(passengersId)
                                                            .addOnCompleteListener(task -> sourcePass.setResult(task.getResult()));
                                                }
                                            }

                                        }

                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                                    .child(Request.class.getSimpleName());
//                                            delete Request
                            requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot requestRouteMap:snapshot.getChildren()){
                                        if (requestRouteMap.hasChild(FirebaseAuth.getInstance().getUid())){
                                            final TaskCompletionSource<Void> sourceReq = new TaskCompletionSource<>();
                                            requestRef.child(requestRouteMap.getKey()).child(FirebaseAuth.getInstance().getUid()).removeValue()
                                                    .addOnCompleteListener(task -> sourceReq.setResult(task.getResult()));
                                            tasks.add(sourceReq.getTask());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

//                                delete last Routes
                            userRef.child("lastRoutes").removeValue()
                                .addOnCompleteListener(task -> {
                                    //                                delete Message Session
                                    final TaskCompletionSource<Void> sourceMessageSessionUser = new TaskCompletionSource<>();
                                    userRef.child("messageSessionId").removeValue().addOnCompleteListener(t -> sourceMessageSessionUser.setResult(t.getResult()));
                                    tasks.add(sourceMessageSessionUser.getTask());
                                });
                        }

//                        Delete Reviews
                        FirebaseDatabase.getInstance().getReference()
                                .child(Reviews.class.getSimpleName())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot reviewedDriver:snapshot.getChildren()){
                                            if (reviewedDriver.hasChild(FirebaseAuth.getInstance().getUid())){
                                                final TaskCompletionSource<Void> sourceReview = new TaskCompletionSource<>();
                                                FirebaseDatabase.getInstance().getReference()
                                                        .child(Reviews.class.getSimpleName())
                                                        .child(reviewedDriver.getKey())
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .removeValue().addOnCompleteListener(task -> sourceReview.setResult(task.getResult()));
                                                tasks.add(sourceReview.getTask());
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                        Tasks.whenAll(tasks).addOnCompleteListener(onCompleteListener);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

    }

    /**
     * Make the rider Driver. Deletes all the data from the Passenger and becomes Driver with a "clean" account
     *
     * @param c
     * @param carImage
     * @param onCompleteListener
     */
    public void becomeDriver(Car c, byte[] carImage, OnCompleteListener<Void> onCompleteListener){
        deleteRiderData(task -> {
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
}
