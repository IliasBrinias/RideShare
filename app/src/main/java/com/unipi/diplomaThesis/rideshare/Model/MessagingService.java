package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
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
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        if (FirebaseAuth.getInstance().getUid() == null) return;
        FirebaseDatabase.getInstance().getReference()
                .child(User.class.getSimpleName())
                .child(FirebaseAuth.getInstance().getUid())
                .child("token_FCM").setValue(token);
    }

    NotificationManager mNotificationManager;
       // Override onMessageReceived() method to extract the
        // title and
        // body from the message passed in FCM
    @Override
    public void
    onMessageReceived(RemoteMessage remoteMessage) {
        String channelId = remoteMessage.getData().get("title");
        Activity activeActivity =((MyApplication) getApplication()).getActiveActivity();
        if (channelId.equals(Message.class.getSimpleName())){
            if (activeActivity instanceof ChatActivity||activeActivity instanceof MessengerActivity){
                return;
            }
        }else if (channelId.equals(Request.class.getSimpleName())){
            if (activeActivity instanceof RequestsActivity){
                return;
            }
        }
        String messageSessionId = remoteMessage.getData().get("body");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID");

        Intent resultIntent = new Intent(this, MessengerActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_IMMUTABLE);


        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setSmallIcon(R.drawable.ic_car_notification_icon);
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setAutoCancel(true);
        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
        builder.setChannelId(channelId);

        if (channelId.equals(Message.class.getSimpleName())){
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