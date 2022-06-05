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
import com.unipi.diplomaThesis.rideshare.driver.RequestsActivity;
import com.unipi.diplomaThesis.rideshare.messenger.ChatActivity;
import com.unipi.diplomaThesis.rideshare.messenger.MessengerActivity;
import com.unipi.diplomaThesis.rideshare.rider.RiderActivity;

import java.util.ArrayList;

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final String MESSAGE_SESSION_CHANNEL_ID = "789";
    private ChildEventListener messageSessionChildListener;
    Activity currentActivity;
    ValueEventListener childEventListenerRequest;

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostStopped(activity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;
        if (activity instanceof RiderActivity || activity instanceof DriverActivity) {
            startMessageSessionIdListener();

            if (activity instanceof DriverActivity){
                try {
                    startRequestListener();
                }catch (Exception e){
                    e.printStackTrace();
                    stopRequestListener();
                }
            }
        }

    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Application.ActivityLifecycleCallbacks.super.onActivityPreCreated(activity, savedInstanceState);
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostResumed(activity);
        currentActivity = activity;
        try {
            if (activity instanceof ChatActivity||activity instanceof MessengerActivity) {
                stopMessageSessionIdListener();
            }
            if (activity instanceof RequestsActivity){
                stopRequestListener();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityPaused(Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {
        Application.ActivityLifecycleCallbacks.super.onActivityPostPaused(activity);
        currentActivity = activity;

        try {
            if (activity instanceof ChatActivity||activity instanceof MessengerActivity){
                startMessageSessionIdListener();
            }
        }catch (Exception ignore){}


    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityStopped(Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
    public static final String MESSAGE_CHANNEL_ID="123";
    public static final String REQUEST_CHANNEL_ID="234";
    private void createNotificationMessage(Message m, User u, MessageSession messageSession){
        boolean mute = PreferenceManager.getDefaultSharedPreferences(currentActivity).getBoolean("mute", false) ||
                PreferenceManager.getDefaultSharedPreferences(currentActivity).getBoolean(u.getUserId(), false);
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
        builder.setSilent(mute);

        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(currentActivity);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }
    private void createNotificationRequest(User u, Route r){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(currentActivity, RequestsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(currentActivity, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(currentActivity, MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_car_notification_icon)
                .setContentTitle(currentActivity.getString(R.string.request))
                .setContentText(u.getFullName())
                .setStyle(
                        new NotificationCompat.BigTextStyle().bigText(u.getFullName()+" "+currentActivity.getString(R.string.is_instrested_for)+" "+r.getName())
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setSilent(PreferenceManager.getDefaultSharedPreferences(currentActivity).getBoolean("mute", false));
        // Issue the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(currentActivity);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }
    private void createNotificationMessageSession(User u, MessageSession messageSession){
        boolean mute = PreferenceManager.getDefaultSharedPreferences(currentActivity).getBoolean("mute", false) ||
                        PreferenceManager.getDefaultSharedPreferences(currentActivity).getBoolean(u.getUserId(), false);
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
        builder.setSilent(mute);
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
                                    if (messageSession.isSeen()) return;

                                    createNotificationChannel(currentActivity,"messageSession","messageInfo",MESSAGE_SESSION_CHANNEL_ID);
                                    User.loadUser(p, messageSessionCreator -> {
                                        createNotificationMessageSession(messageSessionCreator, messageSession);
                                    });
                                    return;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
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
                                    if (m.isSeen()) return;
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
    private void startRequestListener(){
        Driver driver;
        try {
           driver = (Driver) User.loadUserInstance(currentActivity);
           if (driver == null) throw new ClassCastException();
        }catch (ClassCastException ignore){
            return;
        }
        driver.loadDriversRouteId(ids -> {
            if (ids == null) return;
            childEventListenerRequest = FirebaseDatabase.getInstance().getReference()
                    .child(Request.class.getSimpleName())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (String id:ids) {
                                if (!snapshot.hasChild(id)) continue;
                                for (DataSnapshot requests : snapshot.child(id).getChildren()) {
                                    Request request = requests.getValue(Request.class);
                                    if (!request.isSeen()){
                                        User.loadUser(request.getRiderId(),u -> {
                                            System.out.println("User: "+u.getFullName());
                                            Route.loadRoute(request.getRouteId(),route -> {
                                                System.out.println("Route: "+route.getRouteId());
                                                createNotificationChannel(currentActivity,"request","requestInfo",REQUEST_CHANNEL_ID);
                                                createNotificationRequest(u,route);
                                                request.makeSeen();
                                            });
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        });
    }
    private void stopRequestListener(){
        FirebaseDatabase.getInstance().getReference()
                .child(Request.class.getSimpleName())
                .removeEventListener(childEventListenerRequest);
    }
}
