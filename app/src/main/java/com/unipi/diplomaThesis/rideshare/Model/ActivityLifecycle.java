package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.diplomaThesis.rideshare.Interface.OnMessageReturn;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.DriverActivity;
import com.unipi.diplomaThesis.rideshare.messenger.ChatActivity;
import com.unipi.diplomaThesis.rideshare.messenger.MessengerActivity;
import com.unipi.diplomaThesis.rideshare.rider.RiderActivity;

import java.util.ArrayList;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final String MESSAGE_SESSION_CHANNEL_ID = "789";
    private ChildEventListener messageSessionChildListener;
    Activity currentActivity;
    Application myApplication;
    public ActivityLifecycle(Application myApplication) {
        this.myApplication = myApplication;
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostStopped(activity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostCreated(activity, savedInstanceState);
        try {
            Activity currentActivity = ((MyApplication) myApplication.getApplicationContext()).getCurrentActivity();
            if (currentActivity instanceof RiderActivity || currentActivity instanceof DriverActivity) {
                startMessageSessionIdListener();
            }
        }catch (Exception ignore){}


    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostResumed(activity);
        try {
            Activity currentActivity = ((MyApplication) myApplication.getApplicationContext()).getCurrentActivity();
            if (currentActivity instanceof ChatActivity||currentActivity instanceof MessengerActivity) {
                stopMessageSessionIdListener();
            }
        }catch (Exception ignore){}

    }

    @Override
    public void onActivityPaused(Activity activity) {
        this.currentActivity = activity;

    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostPaused(activity);
        try {
            Activity currentActivity = ( (MyApplication) myApplication.getApplicationContext()).getCurrentActivity();
            if (currentActivity instanceof ChatActivity||currentActivity instanceof MessengerActivity){
                startMessageSessionIdListener();
            }
        }catch (Exception ignore){}


    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        this.currentActivity = activity;

    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
    public static final String MESSAGE_CHANNEL_ID="123";
    private void createNotificationMessage(Message m, User u, MessageSession messageSession){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(currentActivity, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(MessageSession.class.getSimpleName(),messageSession);
        PendingIntent pendingIntent = PendingIntent.getActivity(currentActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(currentActivity, MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_car_notification_icon)
                .setContentTitle(currentActivity.getString(R.string.messages))
                .setContentText(u.getFullName())
                .setStyle(
                        new NotificationCompat.BigTextStyle().bigText(m.getMessage())
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(currentActivity);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }
    private void createNotificationMessageSession(User u, MessageSession messageSession){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(currentActivity, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(MessageSession.class.getSimpleName(),messageSession);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(currentActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(currentActivity, MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_car_notification_icon)
                .setContentTitle(u.getFullName())
                .setStyle(
                        new NotificationCompat.BigTextStyle().bigText(currentActivity.getString(R.string.first_message_chat))
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(currentActivity);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }
    private void createNotificationChannel(Activity activity,String name, String description, String channel_id) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
    private void startMessageSessionIdListener(){
        messageSessionChildListener = FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        try {
                            ArrayList<String> participants = (ArrayList<String>) snapshot.child("participants").getValue();
                            for (String p:participants){
                                if (!FirebaseAuth.getInstance().getUid().equals(p)){
                                    MessageSession messageSession = snapshot.getValue(MessageSession.class);
                                    if (PreferenceManager.getDefaultSharedPreferences(currentActivity)
                                            .getLong(messageSession.getMessageSessionId(),0L) != 0L){
                                        return;
                                    }
                                    PreferenceManager.getDefaultSharedPreferences(currentActivity).edit()
                                            .putLong(messageSession.getMessageSessionId(), messageSession.getCreationTimestamp()).apply();
                                    createNotificationChannel(currentActivity,"messageSession","messageInfo",MESSAGE_SESSION_CHANNEL_ID);
                                    User.loadUser(p, messageSessionCreator -> {
                                        createNotificationMessageSession(messageSessionCreator, snapshot.getValue(MessageSession.class));
                                    });
                                    return;
                                }
                            }
                        }catch (Exception e){
                            stopMessageSessionIdListener();
                        }
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        String userId;
                        try {
                            userId = FirebaseAuth.getInstance().getUid();
                        }catch (Exception e){
                            stopMessageSessionIdListener();
                            return;
                        }
                        ArrayList<String> participants = (ArrayList<String>) snapshot.child("participants").getValue();
                        for (String p:participants){
                            if (userId.equals(p)){
                                MessageSession messageSession = snapshot.getValue(MessageSession.class);
                                getLastMessage(snapshot.getKey(), m -> {
                                    if (m.getUserSenderId().equals(FirebaseAuth.getInstance().getUid())) return;
                                    PreferenceManager.getDefaultSharedPreferences(currentActivity).edit()
                                            .putLong(FirebaseAuth.getInstance().getUid(), m.getTimestamp()).apply();
                                    createNotificationChannel(currentActivity,"message","messageInfo",MESSAGE_CHANNEL_ID);
                                    User.loadUser(m.getUserSenderId(), u -> {
                                        createNotificationMessage(m, u,messageSession);
                                    });
                                });
                                return;
                            }
                        }
                    }
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void stopMessageSessionIdListener(){
        FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName())
                .removeEventListener(messageSessionChildListener);
    }
    private void getLastMessage(String sessionId,OnMessageReturn onMessageReturn){
        long lastMessageTimestamp = PreferenceManager.getDefaultSharedPreferences(currentActivity)
                .getLong(FirebaseAuth.getInstance().getUid(), 0);
        FirebaseDatabase.getInstance().getReference()
                .child(MessageSession.class.getSimpleName())
                .child(sessionId)
                .child("messages")
                .orderByChild("timestamp")
                .limitToLast(1)
                .startAt(lastMessageTimestamp)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        onMessageReturn.returnedMessage(snapshot.getChildren().iterator().next().getValue(Message.class));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

}
