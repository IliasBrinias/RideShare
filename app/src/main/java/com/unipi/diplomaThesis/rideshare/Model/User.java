package com.unipi.diplomaThesis.rideshare.Model;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private String type = Rider.class.getSimpleName();
    private long birthDay = 0;
    private Map<String,UserRating> userRating;
    private ArrayList<String> lastRoutes = new ArrayList<>();
    private ArrayList<String> messageSessionId = new ArrayList<>();


    public User() {
    }

    public User(String userId, String email, String fullName, String description, ArrayList<String> lastRoutes) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.description = description;
        this.lastRoutes = lastRoutes;
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
                            onUserLoadComplete.returnedUser(snapshot.getValue(Rider.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    public static void saveUser(@NonNull User u, OnCompleteUserSave onCompleteUserSave){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        if (u.userId == null) u.userId = FirebaseAuth.getInstance().getUid();
        databaseReference.child(u.userId).setValue(u).addOnCompleteListener(onCompleteUserSave::onComplete);
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
            return gson.fromJson(json, Rider.class);
        }
    }

    public void loadUserImage(OnImageLoad onImageLoad) {
        try {
            File f = File.createTempFile("ProfileImage", ".jpg");
            StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(this.getUserId())
                    .child("ProfileImage.jpg");
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
    public void loadUserImageBackGround(OnImageLoad onImageLoad) {
        try {
            File f = File.createTempFile("ProfileImageBackground", ".jpg");
            StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                    .child(User.class.getSimpleName())
                    .child(this.getUserId())
                    .child("ProfileImageBackground.jpg");
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
    public void saveUserImage(byte[] userImage, OnCompleteListener<UploadTask.TaskSnapshot> onCompleteListener){
        FirebaseStorage.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.userId)
        .putBytes(userImage).addOnCompleteListener(onCompleteListener);
    }
    public void logOut(Context c){
//        log Out from FirebaseAuth
        FirebaseAuth.getInstance().signOut();
//        clear the user from the preferences
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .remove(User.class.getSimpleName())
                .apply();
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
    public void sendMessageTo(MessageSession messageSession, Message m, OnCompleteListener<Void> onCompleteListener){
        if (m.getMessage().endsWith("\n")){
            m.setMessage(m.getMessage().substring(0,m.getMessage().length()-1));
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName())
                .child(messageSession.getMessageSessionId())
                .child("messages");
        m.setMessageId(ref.push().getKey());
        ref.child(m.getMessageId())
                .setValue(m).addOnCompleteListener(onCompleteListener);
    }
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
                            Route.loadRoute(routeId, route -> {
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
    public void loadUserMessageSession(OnMessageSessionLoad onMessageSessionLoad){
        for (String sessionId:this.messageSessionId) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child(MessageSession.class.getSimpleName())
                    .child(sessionId);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    MessageSession messageSession = new MessageSession();
                    messageSession.setMessageSessionId(snapshot.getKey());
                    messageSession.setParticipants((ArrayList<String>) snapshot.child("participants").getValue());
                    messageSession.setCreationTimestamp(snapshot.child("creationTimestamp").getValue(Long.class));
                    if (snapshot.child("messages").getChildrenCount()==0){
                        onMessageSessionLoad.returnedSession(messageSession);
                        return;
                    }
                    ref.child("messages")
                        .orderByChild("timestamp")
                        .limitToLast(1)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String,Message> messageMap = new HashMap<>();
                                for (DataSnapshot msg : snapshot.getChildren()) {
                                    Message m = msg.getValue(Message.class);
                                    messageMap.put(msg.getKey(), m);
                                    messageSession.setMessages(messageMap);
                                    onMessageSessionLoad.returnedSession(messageSession);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }
    private void messageSeen(MessageSession messageSession,Message m){
        FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName())
                .child(messageSession.getMessageSessionId())
                .child("messages")
                .child(m.getMessageId())
                .child("seen").setValue(true);
    }
    ChildEventListener loadMessages;
    public void loadMessages(MessageSession messageSession, OnMessageReturn onMessageReturn){
        loadMessages = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName())
                .child(messageSession.getMessageSessionId())
                .child("messages").orderByChild("timestamp")
                .limitToLast(100)
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (!snapshot.exists()) return;
                    Message m = snapshot.getValue(Message.class);
                    if (m.getUserSenderId().equals(FirebaseAuth.getInstance().getUid())){
                        onMessageReturn.returnedMessage(m);
                        return;
                    }
                    if (!m.isSeen()){
                        m.setSeen(true);
                        messageSeen(messageSession,m);
                    }
                    onMessageReturn.returnedMessage(m);
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    onMessageReturn.returnedMessage(snapshot.getValue(Message.class));

                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    public static void mutualRoutes(Driver driver, Rider rider, OnRouteResponse onRouteResponse){
        for (String routeId:driver.getLastRoutes()){
            Route.loadRoute(routeId, route->{
                for (String p:route.getPassengersId()){
                    if (p.equals(rider.getUserId())){
                        onRouteResponse.returnedRoute(route);
                    }
                }
            });
        }
    }

    public void stopLoadingMessages(MessageSession messageSession) {
        FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName())
                .child(messageSession.getMessageSessionId())
                .child("messages").orderByChild("timestamp")
                .limitToLast(100)
                .removeEventListener(loadMessages);
    }
}
