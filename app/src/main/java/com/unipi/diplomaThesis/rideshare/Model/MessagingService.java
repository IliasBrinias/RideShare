package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.unipi.diplomaThesis.rideshare.R;
import com.unipi.diplomaThesis.rideshare.driver.RequestsActivity;
import com.unipi.diplomaThesis.rideshare.messenger.ChatActivity;
import com.unipi.diplomaThesis.rideshare.messenger.MessengerActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    static Map<String,Integer> messageUsers = new HashMap<>();
    static Map<String,Integer> requestAccepted = new HashMap<>();

    /**
     * Replace the User Token if generates a new one
     * @param token
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    NotificationManager mNotificationManager;

    /**
     * On MessageReceive handle the notification that comes from the Firebase Cloud Messaging.
     *  Based on data it shows a different style notification
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String channelId = remoteMessage.getData().get("title");
        String messageSessionId = remoteMessage.getData().get("body");

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("mute",false) ||
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean(messageSessionId,false)) return;

        Activity activeActivity =((MyApplication) getApplication()).getActiveActivity();
        if (channelId.equals(Messages.class.getSimpleName())){
            if (activeActivity instanceof ChatActivity||activeActivity instanceof MessengerActivity){
                return;
            }
        }else if (channelId.equals(Request.class.getSimpleName())){
            if (activeActivity instanceof RequestsActivity){
                return;
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID");
        Intent resultIntent = new Intent(this, MessengerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setSmallIcon(R.drawable.ic_car_notification_icon);
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setAutoCancel(true);
        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        builder.setChannelId(channelId);
        if (channelId.equals(Messages.class.getSimpleName())){
            if (messageUsers.get(messageSessionId)==null){
                messageUsers.put(messageSessionId,new Random().nextInt());
            }
            mNotificationManager.notify(messageUsers.get(messageSessionId),builder.build());
        }else if (channelId.equals(Request.class.getSimpleName())){
            if (requestAccepted.get(messageSessionId)==null){
                requestAccepted.put(messageSessionId,new Random().nextInt());
            }
            mNotificationManager.notify(requestAccepted.get(messageSessionId),builder.build());
        }
    }
}