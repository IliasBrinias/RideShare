package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unipi.diplomaThesis.rideshare.Interface.OnCarResponse;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver extends User{
    private Car ownedCar = new Car();

    public Driver() {}

    public Driver(String userId, String email, String fullName) {
        super(userId, email, fullName, Driver.class.getSimpleName());
    }
    public Car getOwnedCar() {
        return ownedCar;
    }
    public void setOwnedCar(Car ownedCar) {
        this.ownedCar = ownedCar;
    }

    /**
     * load the user from the Firebase. If is not Driver returns null
     * @param driverId
     * @param onUserLoadComplete
     */
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

    /**
     * Save new/edited Drivers Routes and added the routeId to his lastRoutes
     * @param r
     * @param onCompleteListener
     */
    public void saveRoute(Routes r, OnCompleteListener<Void> onCompleteListener){
//      save route
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Routes.class.getSimpleName());
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

    /**
     * load Drivers Routes based on Drivers lastRoutes ids
     * @param onRouteResponse
     */
    public void loadDriverRoutes(OnRouteResponse onRouteResponse){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Routes.class.getSimpleName());
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId())
                .child("lastRoutes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null) {
                            onRouteResponse.returnedRoute(null);
                            return;
                        };
                        ArrayList<String> routeIds = (ArrayList<String>) snapshot.getValue();
                        if (routeIds == null) onRouteResponse.returnedRoute(null);
                        for (String id:routeIds) {
                            database.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Routes routes = snapshot.getValue(Routes.class);
                                    if (routes == null) return;
                                    onRouteResponse.returnedRoute(routes);
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

    /**
     * Save Car info and Image to firebase
     * @param c
     * @param carImage
     * @param onCompleteListener
     * @param onFailureListener
     */
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

    /**
     * Load Image from the FirebaseStorage
     * @param riderId
     * @param onImageLoad
     */
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

    /**
     * Create a new MessageSessions Object. Make it visible to the rider (save the id MessageSessions to his Object)
     * and to the Driver. Sends a Notification to the Riders Token with the CloudMessaging by Firebase
     * @param activity
     * @param d
     * @param riderId
     */
    private void createMessageSession(Activity activity, Driver d, String riderId){
        ArrayList<String> participants = new ArrayList<>();
        participants.add(this.getUserId());
        participants.add(riderId);
        MessageSessions messageSessions = new MessageSessions(null, participants,(new Date()).getTime(),null);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(MessageSessions.class.getSimpleName());
        String messageSessionId = databaseReference.push().getKey();
        messageSessions.setMessageSessionId(messageSessionId);
        databaseReference.child(messageSessionId)
                .setValue(messageSessions).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    /**
     * This method is called by createMessageSession method and save the MessageSessions id to the participant
     * @param participants
     * @param sessionId
     */
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

    /**
     * Load Car Image from Firebase Storage
     * @param onImageLoad
     */
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

    /**
     * Saves Car Image
     * @param carImage
     * @param onCompleteListener
     */
    public void saveCarImage(byte[] carImage, OnCompleteListener<UploadTask.TaskSnapshot> onCompleteListener){
        FirebaseStorage.getInstance().getReference()
                .child(Car.class.getSimpleName())
                .child(this.getUserId())
                .putBytes(carImage)
                .addOnCompleteListener(onCompleteListener);
    }

    /**
     * Change the child ("seen") Of messageSession to true
     * @param routeId
     * @param passengerId
     */
    private void makeRequestSeen(String routeId, String passengerId){
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .child(routeId).child(passengerId)
                .child("seen").setValue(true);
    }

    /**
     * Load requests based on Driver last Routes ids
     * @param onRequestLoad
     */
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
                                    Request r = request.getValue(Request.class);
                                    makeRequestSeen(r.getRouteId(),r.getRiderId());
                                    onRequestLoad.returnedRequest(r);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });
    }

    /**
     * Returns lastRoutes ids for
     * @param onReturnedIds
     */
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

    /**
     * Check if the driver has messageSession with the Passenger. If he hasn't
     * calls the createMessageSession.
     * Adds the routeId to participant lastRoutes and adds the participantId to Routes passengersId list
     * @param activity
     * @param request
     */
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
//                                add the rider to the Routes and Routes to riders lastRoutes
                                addRiderToRoute(request.getRouteId(),request.getRiderId());
                                addRouteToRiders(request.getRouteId(),request.getRiderId());
                            });
                        }
                    }
                });

    }

    /**
     * Is used from acceptRequest method, add routeId to lastRoutes passengers List
     * @param routeId
     * @param riderId
     */
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

    /**
     * Add the riderId to Routes passengersId list
     * @param routeId
     * @param riderId
     */
    private void addRiderToRoute(String routeId, String riderId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(Routes.class.getSimpleName())
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
    /**
     * Check if the Driver and Passenger has mutual MessageSessionId
     * @param riderId
     * @param driverId
     * @param hasAllreadyMessageSession
     */
    private void checkIfRiderHasMessageSessionWithDriver(String riderId, String driverId, hasAllreadyMessageSession hasAllreadyMessageSession){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
                ref.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot driver) {
//                        check if driver has MessageSessions and get his arrayList
                        if (driver.hasChild("messageSessionId")){
                            ArrayList<String> driverMessageSessionId = (ArrayList<String>) driver.child("messageSessionId").getValue();
                            if (driverMessageSessionId == null) {
                                hasAllreadyMessageSession.isExists(false);
                                return;
                            }
                            ref.child(riderId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot rider) {
//                                    check if the rider has MessageSessions and get his array list
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
                                        }
                                    }
                                    hasAllreadyMessageSession.isExists(false);
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

    /**
     * Simply deletes the Request from the Passenger
     * @param request
     */
    public void declineRequest(Request request) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(Request.class.getSimpleName())
                .child(request.getRouteId()).removeValue();
    }

    /**
     * When the Driver Delete his account he must Delete:
     *  1. requests
     *  2. reviews
     *  3. passengers of his routes
     *  4. his routes
     *  5. his images (profile, car)
     *  6. his Messages Session id from the riders
     *  7. his Messages Session id
     *  8. MessageSessions
     *  9. His Object
     *  10. his Authorized account
     * @param password
     * @param onCompleteListener
     */
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
                            deleteDatabaseData((t,complete) ->{
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
    private void deleteMessageSession(ArrayList<String> messageSessionId, OnProcedureComplete onProcedureComplete){
//                            User MessageSessionIds
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                                    .child(User.class.getSimpleName());
        DatabaseReference messageSessionRef = FirebaseDatabase.getInstance().getReference()
                                    .child(MessageSessions.class.getSimpleName());

        messageSessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                  MessageSessions Participants
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
                Tasks.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(@NonNull Task<Void> task) throws Exception {
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final ArrayList<Task<Void>> tasks = new ArrayList<>();
//                          deleteMessageSessionId from the other participants
                            for (Map.Entry<String,String> sessionEntry:sessions.entrySet()){
                                ArrayList<String> messageSessionId = (ArrayList<String>) snapshot.child(sessionEntry.getValue()).child("messageSessionId").getValue();
                                if (!messageSessionId.contains(sessionEntry.getKey())) continue;
                                messageSessionId.remove(sessionEntry.getKey());
                                final TaskCompletionSource<Void> sourceParticipant = new TaskCompletionSource<>();
                                userRef.child(sessionEntry.getValue()).child("messageSessionId").setValue(messageSessionId)
                                        .addOnCompleteListener(task -> sourceParticipant.setResult(task.getResult()));
                                tasks.add(sourceParticipant.getTask());
                            }
                            Tasks.whenAll(tasks).continueWith(task1 -> {
                                onProcedureComplete.isComplete(task1,true);
                                return null;
                            });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

                        return null;
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
                ArrayList<String> lastRoutesId;
                final ArrayList<Task<Void>> tasks = new ArrayList<>();
//              delete Messages
                if (snapshot.hasChild("lastRoutes")){
                    lastRoutesId = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
//                  Delete Routes and Requests
                    deleteDriversRoutes(lastRoutesId, (t, c) -> {
                        if (!c) return;
                        ArrayList<String> messageSessionId;
                        if (snapshot.hasChild("messageSessionId")){
                            messageSessionId = (ArrayList<String>) snapshot.child("messageSessionId").getValue();
                            deleteMessageSession(messageSessionId, (task1, complete1)->{
                                //              Delete Reviews
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Reviews.class.getSimpleName())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.getChildrenCount()>0){
                                                    final TaskCompletionSource<Void> sourceReviews = new TaskCompletionSource<>();
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child(Reviews.class.getSimpleName())
                                                            .child(FirebaseAuth.getInstance().getUid())
                                                            .removeValue().addOnCompleteListener(task -> sourceReviews.setResult(task.getResult()));
                                                    tasks.add(sourceReviews.getTask());
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {}
                                        });
//                              delete User
                                final TaskCompletionSource<Void> sourceUser = new TaskCompletionSource<>();
                                userRef.removeValue().addOnCompleteListener(task -> sourceUser.setResult(task.getResult()));
                                tasks.add(sourceUser.getTask());
//                              deletePhotos
                                final TaskCompletionSource<Void> sourceProfile = new TaskCompletionSource<>();
                                StorageReference photo = FirebaseStorage.getInstance().getReference();
                                photo.child(User.class.getSimpleName()).child(Driver.this.getUserId()).delete()
                                        .addOnCompleteListener(task -> sourceProfile.setResult(task.getResult()));
                                tasks.add(sourceProfile.getTask());

                                final TaskCompletionSource<Void> sourceCar = new TaskCompletionSource<>();
                                photo.child(Car.class.getSimpleName()).child(Driver.this.getUserId()).delete()
                                        .addOnCompleteListener(task -> sourceCar.setResult(task.getResult()));
                                tasks.add(sourceCar.getTask());
                                Tasks.whenAll(tasks).addOnCompleteListener(task -> onProcedureComplete.isComplete(task, true));

                            });
                        }
                    });
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
    private void deletePassengers(Map<String,ArrayList<String>> passengers, OnProcedureComplete onProcedureComplete){
        DatabaseReference passengersRef = FirebaseDatabase.getInstance()
                .getReference().child(User.class.getSimpleName());
        passengersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final ArrayList<Task<Void>> tasks = new ArrayList<>();
                for (Map.Entry<String, ArrayList<String>> passEntry:passengers.entrySet()){
                    ArrayList<String> lastRoutesPass = (ArrayList<String>) snapshot.child(passEntry.getKey()).child("lastRoutes").getValue();
                    if (lastRoutesPass == null) continue;
                    lastRoutesPass.removeAll(passEntry.getValue());
                    final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
                    passengersRef.child(passEntry.getKey()).child("lastRoutes").setValue(lastRoutesPass)
                            .addOnCompleteListener(task -> source.setResult(task.getResult()));
                    tasks.add(source.getTask());
                }
                Tasks.whenAll(tasks).addOnCompleteListener(task -> onProcedureComplete.isComplete(task,true));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void deleteDriversRoutes(ArrayList<String> routes, OnProcedureComplete onProcedureComplete){
        FirebaseDatabase.getInstance().getReference().child(Routes.class.getSimpleName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String,ArrayList<String>> passengers = new HashMap<>();
                        ArrayList<String> pass = new ArrayList<>();
                        ArrayList<Task<Void>> tasks = new ArrayList<>();

                        for (String id:routes){
                            pass.clear();
                            if (snapshot.child(id).child("passengersId").getValue() !=null){
                                ArrayList<String> routePass = (ArrayList<String>) snapshot.child(id).child("passengersId").getValue();
                                pass.removeAll(routePass);
                                pass.addAll(routePass);
                            }
                            for (String idPass:pass){
                                if (passengers.get(idPass) == null){
                                    passengers.put(idPass,new ArrayList<>(Collections.singleton(id)));
                                }else {
                                    ArrayList<String> passRouteToDelete = passengers.get(idPass);
                                    passRouteToDelete.add(id);
                                    passengers.put(idPass,passRouteToDelete);
                                }
                            }
                            final TaskCompletionSource<Void> sourceRequest = new TaskCompletionSource<>();
                            final TaskCompletionSource<Void> sourceRoute = new TaskCompletionSource<>();
                            FirebaseDatabase.getInstance().getReference().child(Request.class.getSimpleName())
                                    .child(id).removeValue()
                                    .addOnCompleteListener(task -> sourceRequest.setResult(task.getResult()));
                            tasks.add(sourceRequest.getTask());
                            FirebaseDatabase.getInstance().getReference().child(Routes.class.getSimpleName())
                                    .child(id).removeValue()
                                    .addOnCompleteListener(task -> sourceRoute.setResult(task.getResult()));
                            tasks.add(sourceRoute.getTask());
                        }
                        Tasks.whenAll(tasks).addOnCompleteListener(task->deletePassengers(passengers,onProcedureComplete));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    /**
     * delete Routes and route id from the rider and check for every passenger if this was the only mutual route.
     * if is true deletes and the Messages Session
     * @param deletedRoutes
     */
    public void deleteRoute(Routes deletedRoutes){
//        delete all the route data
        DatabaseReference routeRef = FirebaseDatabase.getInstance().getReference()
                .child(Routes.class.getSimpleName())
                .child(deletedRoutes.getRouteId());
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        routeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                delete route from the passengers
                if (snapshot.hasChild("passengersId")){
                    ArrayList<String> passengerId = (ArrayList<String>) snapshot.child("passengersId").getValue();
                        deleteRoutePassengers(deletedRoutes.getRouteId(),passengerId, task -> {
                        userRef.child(Driver.this.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> routeId = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
                                if (routeId == null) return;
                                routeId.remove(deletedRoutes.getRouteId());
                                userRef.child(Driver.this.getUserId()).child("lastRoutes").setValue(routeId);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    });
                }
                //        delete Routes
                routeRef.removeValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Delete a single passenger from the route and checks if was the only mutual Routes
     * @param routeId
     * @param passengerId
     * @param onCompleteListener
     */
    public void deletePassenger(String routeId, String passengerId, OnCompleteListener<Void> onCompleteListener){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        DatabaseReference routeRef =  FirebaseDatabase.getInstance().getReference()
                .child(Routes.class.getSimpleName())
                .child(routeId);
        final ArrayList<Task<Void>> tasks = new ArrayList<>();
        routeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> passengers = (ArrayList<String>) snapshot.child("passengersId").getValue();
                if (passengers == null) return;
                if (passengers.contains(passengerId)) {
                    passengers.remove(passengerId);
                }
                final TaskCompletionSource<Void> sourcePass = new TaskCompletionSource<>();
                routeRef.child("passengersId").setValue(passengers).addOnCompleteListener(task -> sourcePass.setResult(task.getResult()));
                tasks.add(sourcePass.getTask());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        usersRef.child(this.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot driver) {
                        usersRef.child(passengerId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot passenger) {
                                ArrayList<String> passengerLastRoutes = (ArrayList<String>) passenger.child("lastRoutes").getValue();
                                if (passengerLastRoutes == null) return;
                                passengerLastRoutes.remove(routeId);
                                final TaskCompletionSource<Void> sourcePass = new TaskCompletionSource<>();
                                usersRef.child(passengerId).child("lastRoutes").setValue(passengerLastRoutes)
                                        .addOnCompleteListener(task -> sourcePass.setResult(task.getResult()));
                                tasks.add(sourcePass.getTask());
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        Tasks.whenAll(tasks).addOnCompleteListener(onCompleteListener);
    }

    /**
     * is used from deleteRoute and deletes all the passengers.
     * i created two separate methods because the firebase async calls was not working correctly.
     * @param routeId
     * @param passengersId
     * @param onCompleteListener
     */
    public void deleteRoutePassengers(String routeId, List<String> passengersId, OnCompleteListener<Void> onCompleteListener){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        usersRef.child(this.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot driver) {
                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot passenger) {
                                    final ArrayList<Task<Void>> tasks = new ArrayList<>();
                                    for (String id:passengersId) {
                                        ArrayList<String> passengerLastRoutes = (ArrayList<String>) passenger.child(id).child("lastRoutes").getValue();
                                        if (passengerLastRoutes == null) continue;
                                        passengerLastRoutes.remove(routeId);
                                        final TaskCompletionSource<Void> sourcePassenger = new TaskCompletionSource<>();
                                        usersRef.child(id).child("lastRoutes").setValue(passengerLastRoutes)
                                                .addOnCompleteListener(task -> sourcePassenger.setResult(task.getResult()));
                                        tasks.add(sourcePassenger.getTask());
                                    }
                                    Tasks.whenAll(tasks).addOnCompleteListener(onCompleteListener::onComplete);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public void loadCarData(OnCarResponse onCarResponse) {
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId())
                .child("ownedCar")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        onCarResponse.returnCar(snapshot.getValue(Car.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}