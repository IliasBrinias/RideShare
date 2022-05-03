package com.unipi.diplomaThesis.rideshare.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.unipi.diplomaThesis.rideshare.Interface.OnCompleteUserSave;
import com.unipi.diplomaThesis.rideshare.Interface.OnImageLoad;
import com.unipi.diplomaThesis.rideshare.Interface.OnUserLoadComplete;
import com.unipi.diplomaThesis.rideshare.R;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    public static final String REQ_IS_DRIVER_TAG="isDriver";

    private String userId;
    private String email;
    private String fullName;
    private String description;
    private Boolean isDriver = false;
    private long birthDay = 0;
    private Map<String,UserRating> userRating;
    private Map<String,String> lastRoutes = new HashMap<>();

    public User() {
    }

    public User(String userId, String email, String fullName, String description, Map<String,String> lastRoutes) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.description = description;
        this.lastRoutes = lastRoutes;
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

    public Boolean getIsDriver() {
        return isDriver;
    }

    public void setIsDriver(Boolean driver) {
        isDriver = driver;
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

    public Map<String,String> getLastRoutes() {
        return lastRoutes;
    }

    public void setLastRoutes(Map<String,String> lastRoutes) {
        this.lastRoutes = lastRoutes;
    }

    public static boolean checkIfEditTextIsNull(Context context, List<EditText> editTexts){
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
                        if (snapshot.child("isDriver").getValue(Boolean.class)){
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
    public static void saveUser(User u, OnCompleteUserSave onCompleteUserSave){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName());
        if (u.userId == null) u.userId = FirebaseAuth.getInstance().getUid();
        databaseReference.child(u.userId).setValue(u).addOnCompleteListener(onCompleteUserSave::onComplete);
    }
    public static User loadUserInstance(SharedPreferences preferences){
        String isDriver = preferences.getString(REQ_IS_DRIVER_TAG,null);
        if (isDriver == null) return null;
        Gson gson = new Gson();
        String json = preferences.getString(User.class.getSimpleName(), null);
        if (Boolean.parseBoolean(isDriver)){
            return gson.fromJson(json, Driver.class);
        }else {
            return gson.fromJson(json, Rider.class);
        }
    }
//    update User instance
    public void updateUserInstance(Context c){
//        save into the Preferences and the Firebase
        Gson gson = new Gson();
        String json = gson.toJson(this);
        PreferenceManager.getDefaultSharedPreferences(c).edit()
                .putString(User.class.getSimpleName(),json).apply();
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(this.userId)
                .setValue(this);
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
    public void saveUserImage(){

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
}
