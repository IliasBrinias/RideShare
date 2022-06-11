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
import java.util.Date;
import java.util.List;

public class Driver extends User{
    public static final int REQ_CREATE_DRIVER_ACCOUNT=234;
    public static final int REQ_DISABLE_DRIVER_ACCOUNT=457;
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
     * Save new/edited Drivers Route and added the routeId to his lastRoutes
     * @param r
     * @param onCompleteListener
     */
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

    /**
     * load Drivers Route based on Drivers lastRoutes ids
     * @param onRouteResponse
     */
    public void loadDriverRoutes(OnRouteResponse onRouteResponse){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName());
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
                                    Route route = snapshot.getValue(Route.class);
                                    if (route == null) return;
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
     * Create a new MessageSession Object. Make it visible to the rider (save the id MessageSession to his Object)
     * and to the Driver. Sends a Notification to the Riders Token with the CloudMessaging by Firebase
     * @param activity
     * @param d
     * @param riderId
     */
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

    /**
     * This method is called by createMessageSession method and save the MessageSession id to the participant
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
     * Adds the routeId to participant lastRoutes and adds the participantId to Route passengersId list
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
//                                add the rider to the Route and Route to riders lastRoutes
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
     * Add the riderId to Route passengersId list
     * @param routeId
     * @param riderId
     */
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
                                            System.out.println(riderId+" "+riderMessageSessionId);
                                            System.out.println(driverMessageSessionId);
                                            if (driverMessageSessionId.contains(riderId)){
                                                System.out.println(true);
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
     *  6. his Message Session id from the riders
     *  7. his Message Session id
     *  8. MessageSession
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

                ArrayList<String> messageSessionId;
                addProcedures();
                if (snapshot.hasChild("messageSessionId")){
                    messageSessionId = (ArrayList<String>) snapshot.child("messageSessionId").getValue();
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
                                            messageSessionId.remove(sessionId);
                                            addProcedures();
                                            participantRef.child("messageSessionId").setValue(messageSessionId).addOnCompleteListener(task -> onProcedureComplete.isComplete(null,isCompleted()));
                                            //                                                    finally remove the Message Session Completely
                                            addProcedures();
                                            messageSessionRef.removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(null,isCompleted()));
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
                onProcedureComplete.isComplete(null,isCompleted());
                ArrayList<String> lastRoutesId;
                addProcedures();
                if (snapshot.hasChild("lastRoutes")){
                    lastRoutesId = (ArrayList<String>) snapshot.child("lastRoutes").getValue();
//                  Delete Routes
                    deleteDriversRoutes(lastRoutesId, onProcedureComplete);
                }
                onProcedureComplete.isComplete(null,isCompleted());
//              Delete Reviews
                addProcedures();
                FirebaseDatabase.getInstance().getReference()
                        .child(Review.class.getSimpleName())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getChildrenCount()>0){
                                    addProcedures();
                                    FirebaseDatabase.getInstance().getReference()
                                    .child(Review.class.getSimpleName())
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(task,isCompleted()));
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                onProcedureComplete.isComplete(null,isCompleted());
//              delete User
                addProcedures();
                userRef.removeValue().addOnCompleteListener(task -> onProcedureComplete.isComplete(task,isCompleted()));
//              deletePhotos
                StorageReference photo = FirebaseStorage.getInstance().getReference();
                addProcedures();
                photo.child(User.class.getSimpleName()).child(Driver.this.getUserId()).delete()
                        .addOnCompleteListener(task -> onProcedureComplete.isComplete(task,isCompleted()));
                addProcedures();
                photo.child(Car.class.getSimpleName()).child(Driver.this.getUserId()).delete()
                        .addOnCompleteListener(task -> onProcedureComplete.isComplete(task,isCompleted()));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
    private void deleteDriversRoutes(ArrayList<String> routes, OnProcedureComplete onProcedureComplete){
        FirebaseDatabase.getInstance().getReference().child(Route.class.getSimpleName())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> passengers = new ArrayList<>();
                        addProcedures();
                        for (String id:routes){
                            if (snapshot.child(id).child("passengersId").getValue() !=null){
                                ArrayList<String> routePass = (ArrayList<String>) snapshot.child(id).child("passengersId").getValue();
                                passengers.removeAll(routePass);
                                passengers.addAll(routePass);
                            }
                        }
                        DatabaseReference passengersRef = FirebaseDatabase.getInstance()
                                .getReference().child(User.class.getSimpleName());
                        passengersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (String id:passengers){
                                    ArrayList<String> lastRoutesPass = (ArrayList<String>) snapshot.child(id).child("lastRoutes").getValue();
                                    for (String routeId:routes){
                                        if (lastRoutesPass.contains(routeId)){
                                            lastRoutesPass.remove(routeId);
                                        }
                                    }
                                    addProcedures();
                                    passengersRef.child(id).child("lastRoutes").setValue(lastRoutesPass)
                                            .addOnCompleteListener(t->onProcedureComplete.isComplete(t,isCompleted()));
                                }
                                for (String id:routes) {
                                    addProcedures();
                                    FirebaseDatabase.getInstance().getReference().child(Request.class.getSimpleName())
                                            .child(id).removeValue()
                                            .addOnCompleteListener(task -> onProcedureComplete.isComplete(task, isCompleted()));
                                    addProcedures();
                                    FirebaseDatabase.getInstance().getReference().child(Route.class.getSimpleName())
                                            .child(id).removeValue()
                                            .addOnCompleteListener(task -> onProcedureComplete.isComplete(task, isCompleted()));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        onProcedureComplete.isComplete(null,isCompleted());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    /**
     * delete Route and route id from the rider and check for every passenger if this was the only mutual route.
     * if is true deletes and the Message Session
     * @param deletedRoute
     */
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

    /**
     * Delete a single passenger from the route and checks if was the only mutual Route
     * @param routeId
     * @param passengerId
     * @param onCompleteListener
     */
    public void deletePassenger(String routeId, String passengerId, OnCompleteListener<Void> onCompleteListener){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        DatabaseReference messageSessionRef = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName());
        DatabaseReference routeRef =  FirebaseDatabase.getInstance().getReference()
                .child(Route.class.getSimpleName())
                .child(routeId);
        routeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> passengers = (ArrayList<String>) snapshot.child("passengersId").getValue();
                if (passengers.contains(passengerId)) {
                    passengers.remove(passengerId);
                }
                routeRef.child("passengersId").setValue(passengers);
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
                                        passengerLastRoutes.remove(routeId);
                                        usersRef.child(passengerId).child("lastRoutes").setValue(passengerLastRoutes).addOnCompleteListener(onCompleteListener);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
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
                        for (String id:passengersId) {
                            usersRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot passenger) {
                                    ArrayList<String> passengerLastRoutes = (ArrayList<String>) passenger.child("lastRoutes").getValue();
                                    passengerLastRoutes.remove(routeId);
                                    usersRef.child(id).child("lastRoutes").setValue(passengerLastRoutes).addOnCompleteListener(t-> {
                                        if (passengersId.indexOf(id) == passengersId.size() - 1) {
                                            onCompleteListener.onComplete(t);
                                        }
                                    });
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
     * Simple comparison of two list with Routes id.
     * @param driverLastRoutes
     * @param passengerLastRoutes
     * @return
     */
    private boolean hasMutualRoutes(ArrayList<String> driverLastRoutes, ArrayList<String> passengerLastRoutes) {
        for (String driverRouteId:driverLastRoutes){
            if (passengerLastRoutes.contains(driverRouteId)) {
                return true;
            }
        }
        return false;
    }

    private static int proceduresCount;

    /**
     * I crated a static variable to control when all the delete procedures will end.
     *  The driver must completely deleted and then the auth account must deleted too. If the auth
     *  account deleted faster than driver data the database will lock because your are not authorized.
     */
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