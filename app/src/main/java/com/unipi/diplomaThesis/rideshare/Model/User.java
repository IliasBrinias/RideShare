package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.unipi.diplomaThesis.rideshare.Interface.OnActiveRouteResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnCompleteUserSave;
import com.unipi.diplomaThesis.rideshare.Interface.OnImageLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnMessageReturn;
import com.unipi.diplomaThesis.rideshare.Interface.OnMessageSessionLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnPreviousRouteResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnReturnedIds;
import com.unipi.diplomaThesis.rideshare.Interface.OnReviewResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnReviewTotalScoreResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnRouteResponse;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    public static final String REQ_TYPE_TAG ="type";

    private String userId;
    private String email;
    private String fullName;
    private String description;
    private String token_FCM;
    private String type = Passenger.class.getSimpleName();
    private long birthDay = 0;
    private Map<String,UserRating> userRating;
    private ArrayList<String> lastRoutes = new ArrayList<>();
    private ArrayList<String> messageSessionId = new ArrayList<>();
    private ChildEventListener loadMessages;

    public User() {
    }

    public User(String userId, String email, String fullName, String type) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.type =type;
    }

    public String getToken_FCM() {
        return token_FCM;
    }

    public void setToken_FCM(String token_FCM) {
        this.token_FCM = token_FCM;
    }

    public ArrayList<String> getMessageSessionId() {
        return messageSessionId;
    }

    public void setMessageSessionId(ArrayList<String> messageSessionId) {
        this.messageSessionId = messageSessionId;
    }

    public long getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(long birthDay) {
        this.birthDay = birthDay;
    }

    public Map<String, UserRating> getUserRating() {
        return userRating;
    }

    public void setUserRating(Map<String, UserRating> userRating) {
        this.userRating = userRating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsersImagePath(){
        return this.getUserId()+"/UserImage";
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public ArrayList<String> getLastRoutes() {
        return lastRoutes;
    }

    public void setLastRoutes(ArrayList<String> lastRoutes) {
        this.lastRoutes = lastRoutes;
    }

    public static boolean checkIfEditTextIsNull(Context context, @NonNull List<EditText> editTexts){
        boolean isNull = false;
        for (EditText currentText :editTexts){
            if (currentText.getError()!=null) {
                isNull = true;
                continue;
            }
            if ((currentText.getText().toString()).matches("")){
                currentText.setError(context.getString(R.string.null_error_editText));
                isNull = true;
            }else {
                currentText.setError(null);
            }
        }
        return isNull;
    }

    /**
     * This method is used from personal Data for the update user data
     * @param c
     * @param profile
     * @param newName
     * @param newEmail
     * @param newBirthday
     * @param onDataUpdate
     * @param onUploadComplete
     */
    public void saveUserPersonalData(Context c,ImageView profile, String newName, String newEmail, long newBirthday,OnCompleteListener<Void> onDataUpdate, OnCompleteListener<UploadTask.TaskSnapshot> onUploadComplete){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User u;
                        if (snapshot.child(User.REQ_TYPE_TAG).getValue(String.class).equals(Driver.class.getSimpleName())) {
                            u = snapshot.getValue(Driver.class);
                        }else {
                            u = snapshot.getValue(Passenger.class);
                        }
                        u.setFullName(newName);
                        u.setEmail(newEmail);
                        u.setBirthDay(newBirthday);
                        if (u.getType().equals(Driver.class.getSimpleName())) {
                            ref.setValue((Driver) u).addOnCompleteListener(onDataUpdate);
                        }else {
                            ref.setValue((Passenger) u).addOnCompleteListener(onDataUpdate);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        if (profile.getBackground() == c.getDrawable(R.drawable.ic_default_profile)) profile =null;
        saveUserImage(getByteArray(profile), onUploadComplete);
    }

    /**
     * Saves User Image
     * @param userImage
     * @param onCompleteListener
     */
    public void saveUserImage(byte[] userImage, OnCompleteListener<UploadTask.TaskSnapshot> onCompleteListener){
        FirebaseStorage.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.userId)
                .putBytes(userImage).addOnCompleteListener(onCompleteListener);
    }

    /**
     * Save User Instance with Shared Preferences
     * @param a
     */
    public void saveUserInstance(Activity a){
        PreferenceManager.getDefaultSharedPreferences(a).edit()
                .putString(User.REQ_TYPE_TAG,this.getType()).apply();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        PreferenceManager.getDefaultSharedPreferences(a).edit()
                .putString(User.class.getSimpleName(),json).apply();
    }

    /**
     * Load Authenticated signIn user from the Firebase
     * @param onImageLoad
     */
    public void loadUserImage(OnImageLoad onImageLoad) {
        try {
            File f = File.createTempFile("ProfileImage", ".jpg");
            StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(this.getUserId());
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
     * Removes listener for the Messages
     * @param messageSessions
     */
    public void stopLoadingMessages(MessageSessions messageSessions) {
        FirebaseDatabase.getInstance().getReference()
                .child(MessageSessions.class.getSimpleName())
                .child(messageSessions.getMessageSessionId())
                .child("messages")
                .removeEventListener(loadMessages);
    }

    /**
     * This method is used From the ChatActivity. Saves the new message on database
     * @param messageSessions
     * @param m
     * @param onCompleteListener
     */
    public void sendMessageTo(Activity activity, User participant, MessageSessions messageSessions, Messages m, OnCompleteListener<Void> onCompleteListener){
        if (m.getMessage().endsWith("\n")){
            m.setMessage(m.getMessage().substring(0,m.getMessage().length()-1));
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(MessageSessions.class.getSimpleName())
                .child(messageSessions.getMessageSessionId())
                .child("messages");
        m.setMessageId(ref.push().getKey());
        ref.child(m.getMessageId())
                .setValue(m).addOnCompleteListener(onCompleteListener);
        sendNotification(activity,participant, messageSessions,m);
    }

    /**
     * Make the Messages child ("seen") true
     * @param messageSessions
     * @param m
     */
    private void messageSeen(MessageSessions messageSessions, Messages m){
        FirebaseDatabase.getInstance().getReference()
                .child(MessageSessions.class.getSimpleName())
                .child(messageSessions.getMessageSessionId())
                .child("messages")
                .child(m.getMessageId())
                .child("seen").setValue(true);
    }
    FcmNotificationsSender fcmNotificationsSender;

    /**
     * Send Notification with FCM
     *
     * @param a
     * @param sender
     * @param messageSessions
     * @param messages
     */
    private void sendNotification(Activity a, User sender, MessageSessions messageSessions, Messages messages){
        fcmNotificationsSender = new FcmNotificationsSender(sender.token_FCM,
                Messages.class.getSimpleName(),
                messageSessions.getMessageSessionId(),
                sender.getFullName(),
                messages.getMessage(),
                a);
        fcmNotificationsSender.SendNotifications();
    }
    /**
     * Returns Users MessageSessions
     * @param onReturnedIds
     */
    public void getMessageSessionId(OnReturnedIds onReturnedIds){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("messageSessionId")){
                            ArrayList<String> ids = (ArrayList<String>) snapshot.child("messageSessionId").getValue();
                            onReturnedIds.returnIds(ids);
                        }else {
                            onReturnedIds.returnIds(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void makeMessageSessionSeen(String messageSessionId){
        FirebaseDatabase.getInstance().getReference()
                .child(MessageSessions.class.getSimpleName())
                .child(messageSessionId)
                .child("seen").setValue(true);

    }
    /**
     * Load Riders Messages Session
     * @param onMessageSessionLoad
     */
    public void loadUserMessageSession(OnMessageSessionLoad onMessageSessionLoad){
        getMessageSessionId(sessionIds -> {
            if (sessionIds == null) {
                onMessageSessionLoad.returnedSession(null);
                return;
            }
            for (String id:sessionIds){
                FirebaseDatabase.getInstance().getReference()
                        .child(MessageSessions.class.getSimpleName())
                        .child(id)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!sessionIds.contains(snapshot.getKey())) return;
                                if (!snapshot.hasChild("participants")) return;
                                MessageSessions messageSessions = new MessageSessions();
                                messageSessions.setMessageSessionId(snapshot.getKey());
                                messageSessions.setSeen(snapshot.child("seen").getValue(Boolean.class));
                                if (!messageSessions.isSeen()) makeMessageSessionSeen(snapshot.getKey());
                                messageSessions.setParticipants((ArrayList<String>) snapshot.child("participants").getValue());
                                messageSessions.setCreationTimestamp(snapshot.child("creationTimestamp").getValue(Long.class));
                                if (snapshot.child("messages").getChildrenCount() == 0) {
                                    onMessageSessionLoad.returnedSession(messageSessions);
                                    return;
                                }
                                FirebaseDatabase.getInstance().getReference()
                                        .child(MessageSessions.class.getSimpleName())
                                        .child(messageSessions.getMessageSessionId())
                                        .child("messages")
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Map<String, Messages> messageMap = new HashMap<>();
                                                for (DataSnapshot msg : snapshot.getChildren()) {
                                                    Messages m = msg.getValue(Messages.class);
                                                    messageMap.put(msg.getKey(), m);
                                                    messageSessions.setMessages(messageMap);
                                                    onMessageSessionLoad.returnedSession(messageSessions);
                                                }
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
            onMessageSessionLoad.returnedSession(null);
        });

    }

    /**
     * load and return Riders Messages
     * @param messageSessions
     * @param onMessageReturn
     */
    public void loadMessages(MessageSessions messageSessions, OnMessageReturn onMessageReturn){
        loadMessages = FirebaseDatabase.getInstance().getReference()
                .child(MessageSessions.class.getSimpleName())
                .child(messageSessions.getMessageSessionId())
                .child("messages")
                .limitToFirst(100)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (!snapshot.exists()) return;
                        Messages m = snapshot.getValue(Messages.class);
                        if (m.getUserSenderId().equals(FirebaseAuth.getInstance().getUid())){
                            onMessageReturn.returnedMessage(m);
                            return;
                        }
                        if (!m.isSeen()){
                            m.setSeen(true);
                            messageSeen(messageSessions,m);
                        }
                        onMessageReturn.returnedMessage(m);
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        onMessageReturn.returnedMessage(snapshot.getValue(Messages.class));

                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    public static void mutualRoutes(Driver driver, Passenger passenger, OnRouteResponse onRouteResponse){
        for (String routeId:driver.getLastRoutes()){
            Routes.loadRoute(routeId, route->{
                for (String p:route.getPassengersId()){
                    if (p.equals(passenger.getUserId())){
                        onRouteResponse.returnedRoute(route);
                    }
                }
            });
        }
    }

    /**
     * load routes based on lastRoutes ids
     * @param onActiveRouteResponse
     * @param onPreviousRouteResponse
     */
    public void loadLastRoutes(OnActiveRouteResponse onActiveRouteResponse, OnPreviousRouteResponse onPreviousRouteResponse){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(FirebaseAuth.getInstance().getUid())
                .child("lastRoutes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> lastRoutes = (ArrayList<String>) snapshot.getValue();
                        if (lastRoutes == null) return;
                        for (String routeId:lastRoutes){
                            Routes.loadRoute(routeId, route -> {
                                if (route == null) return;
                                if (route.getRouteDateTime().getEndDateUnix()<=new Date().getTime()){
                                    User.loadUser(route.getDriverId(), u -> onPreviousRouteResponse.returnRoute(route,u));
                                }else {
                                    User.loadUser(route.getDriverId(), u -> onActiveRouteResponse.returnRoute(route,u));
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * load Reviews with order
     * @param driverId
     * @param limit
     * @param onReviewResponse
     */
    public static void loadReviews(String driverId,int limit,OnReviewResponse onReviewResponse){
        FirebaseDatabase.getInstance().getReference()
                .child(Reviews.class.getSimpleName())
                .child(driverId)
                .orderByChild("timestamp")
                .limitToLast(limit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() == 0) onReviewResponse.returnReview(null);
                        for (DataSnapshot reviewMap:snapshot.getChildren()){
                            onReviewResponse.returnReview(reviewMap.getValue(Reviews.class));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * load Reviews Score for the driver
     * @param targetUserId
     * @param onReviewTotalScoreResponse
     */
    public void loadReviewTotalScore(String targetUserId, OnReviewTotalScoreResponse onReviewTotalScoreResponse){
        FirebaseDatabase.getInstance().getReference()
                .child(Reviews.class.getSimpleName())
                .child(targetUserId)
                .orderByChild("timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        float totalScore = 0;
                        for (DataSnapshot review: snapshot.getChildren()){
                            totalScore = review.child("review").getValue(Float.class);
                        }
                        onReviewTotalScoreResponse.returnData(totalScore, (int) snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void logOut(Context c){
//        log Out from FirebaseAuth
        FirebaseAuth.getInstance().signOut();
//        clear the user from the preferences
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit().clear().apply();
    }

    public static String reformatLengthString(String s, int bounds){
        StringBuilder stringBuilder = new StringBuilder(s);
        if (stringBuilder.length()>=bounds-3){
            return stringBuilder.substring(0,bounds-3) +"...";
        }else {
            return s;
        }
    }
    @Nullable
    public static User loadUserInstance(Context c){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        String type = preferences.getString(REQ_TYPE_TAG,null);
        if (type == null) return null;
        Gson gson = new Gson();
        String json = preferences.getString(User.class.getSimpleName(), null);
        if (type.equals(Driver.class.getSimpleName())){
            return gson.fromJson(json, Driver.class);
        }else {
            return gson.fromJson(json, Passenger.class);
        }
    }
    public static void loadSignInUser(OnUserLoadComplete onUserLoadComplete){
        String userId = FirebaseAuth.getInstance().getUid();
        //load the user
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child(User.class.getSimpleName())
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(User.REQ_TYPE_TAG).getValue(String.class).equals(Driver.class.getSimpleName())){
                            onUserLoadComplete.returnedUser(snapshot.getValue(Driver.class));
                        }else {
                            onUserLoadComplete.returnedUser(snapshot.getValue(Passenger.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

    }
    public static void saveUser(@NonNull User u, OnCompleteUserSave onCompleteUserSave){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        if (u.userId == null) u.userId = FirebaseAuth.getInstance().getUid();
        databaseReference.child(u.userId).setValue(u).addOnCompleteListener(onCompleteUserSave::onComplete);
    }
    public static void loadUser(String userId, OnUserLoadComplete onUserLoadComplete){
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        onUserLoadComplete.returnedUser(snapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static byte[] getByteArray(ImageView imageView){
        // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        return data;
    }

    /**
     * Load and save token FCM
     *
     */
    public void loadTokenFCM(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference()
                            .child(User.class.getSimpleName())
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("token_FCM")
                            .setValue(task.getResult());
                    FirebaseMessaging.getInstance().subscribeToTopic("all");
                }
            }
        });

    }

    /**
     * delete all the activities between rider and driver:
     *  1. delete Messages
     *  2. delete Riders routes and message Session
     *  3. delete rider from the routes
     *  4. delete rider requests for the driver
     *  but keeps the reviews for the driver
     * @param participantId
     * @param onCompleteListener
     */
    public void leaveChat(String participantId, OnCompleteListener<Void> onCompleteListener){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName()).child(this.getUserId());
        DatabaseReference participantRef = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName()).child(participantId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot user) {
                        participantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot participant) {
                                if (participant.child(User.REQ_TYPE_TAG).getValue(String.class).equals(Driver.class.getSimpleName())){
                                    deleteMutualActivities(participant.getValue(Driver.class),user.getValue(Passenger.class),onCompleteListener);
                                }else {
                                    deleteMutualActivities(user.getValue(Driver.class),participant.getValue(Passenger.class),onCompleteListener);
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
    public void deleteMutualActivities(Driver driver, Passenger passenger, OnCompleteListener<Void> onCompleteListener){
        final ArrayList<Task<Void>> tasks = new ArrayList<>();
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child(Request.class.getSimpleName());
        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference().child(User.class.getSimpleName()).child(passenger.getUserId());
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child(User.class.getSimpleName()).child(driver.getUserId());
//        delete Messages
        DatabaseReference messageSessionRef = FirebaseDatabase.getInstance().getReference().child(MessageSessions.class.getSimpleName());
        ArrayList<String> lastRoutesPassenger = passenger.getLastRoutes();
        ArrayList<String> lastRoutesDriver = driver.getLastRoutes();
        ArrayList<String> messageSessionIdPassenger = passenger.getMessageSessionId();
        ArrayList<String> messageSessionIdDriver = driver.getMessageSessionId();

        for (String id: messageSessionIdDriver){
            if (passenger.getMessageSessionId().contains(id)){
                TaskCompletionSource<Void> sourceSession = new TaskCompletionSource<>();
                messageSessionRef.child(id).removeValue().addOnCompleteListener(task -> sourceSession.setResult(task.getResult()));
                tasks.add(sourceSession.getTask());
                break;
            }
        }
        for (String id:lastRoutesDriver){
            if (lastRoutesPassenger.contains(id)) {
                lastRoutesPassenger.remove(id);
            }
        }
        for (String id:messageSessionIdDriver){
            if (messageSessionIdPassenger.contains(id)){
                messageSessionIdPassenger.remove(id);
                messageSessionIdDriver.remove(id);
                break;
            }
        }

        final TaskCompletionSource<Void> sourceRouteRider = new TaskCompletionSource<>();
        riderRef.child("lastRoutes").setValue(lastRoutesPassenger).addOnCompleteListener(task -> sourceRouteRider.setResult(task.getResult()));
        tasks.add(sourceRouteRider.getTask());

        final TaskCompletionSource<Void> sourceSessionRider = new TaskCompletionSource<>();
        riderRef.child("messageSessionId").setValue(messageSessionIdPassenger).addOnCompleteListener(task -> sourceSessionRider.setResult(task.getResult()));
        tasks.add(sourceSessionRider.getTask());


        final TaskCompletionSource<Void> sourceSessionDriver = new TaskCompletionSource<>();
        driverRef.child("messageSessionId").setValue(messageSessionIdDriver).addOnCompleteListener(task -> sourceSessionDriver.setResult(task.getResult()));
        tasks.add(sourceSessionDriver.getTask());

        for (String id:driver.getLastRoutes()){
            DatabaseReference routeRef = FirebaseDatabase.getInstance().getReference().child(Routes.class.getSimpleName());
//          delete passenger from the routes
            routeRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> passengersId = (ArrayList<String>) snapshot.child("passengersId").getValue();
                    if (passengersId.contains(passenger.getUserId())){
                        passengersId.remove(passenger.getUserId());
                        TaskCompletionSource<Void> sourcePass = new TaskCompletionSource<>();
                        routeRef.child(id).child("passengersId").setValue(passengersId)
                                .addOnCompleteListener(task -> sourcePass.setResult(task.getResult()));
                        tasks.add(sourcePass.getTask());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
//            delete passenger requests for the driver
            requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(id)){
                        if (snapshot.child(id).hasChild(passenger.getUserId())){
                            TaskCompletionSource<Void> sourceRequest = new TaskCompletionSource<>();
                            requestRef.child(id).child(passenger.getUserId()).removeValue()
                                    .addOnCompleteListener(task -> sourceRequest.setResult(task.getResult()));
                            tasks.add(sourceRequest.getTask());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        Tasks.whenAll(tasks).addOnCompleteListener(onCompleteListener);

    }
}
