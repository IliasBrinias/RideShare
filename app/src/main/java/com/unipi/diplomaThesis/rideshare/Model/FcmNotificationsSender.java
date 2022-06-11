package com.unipi.diplomaThesis.rideshare.Model;

import android.app.Activity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender  {

    String userFcmToken, title, body, type, messageSessionId;
    Activity mActivity;
    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAAp6LRpoo:APA91bGZitAGneJLt268XdGMapbjhN51i-Xcv76YdGsaddCg1_uljNohwxRn37-UIDz_MvQAEC0p5s3plBVIvg-EVfRokozOYnQn-VfiQJhLDLUPH772WXZ5DjY9yg2vm__G0u-HUM36";

    public FcmNotificationsSender(String userFcmToken, String type, String messageSessionId, String title, String body, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.type = type;
        this.messageSessionId = messageSessionId;
        this.body = body;
        this.mActivity = mActivity;
    }

    /**
     * Firebase Cloud Messaging makes a post call with Volley to the user token that you want
     *  to receive the notification. Every user has a unique token and with that token can send
     *  notification each other. This is a service so is working also when the app is on background
     */
    public void SendNotifications() {
        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);

            JSONObject data = new JSONObject();
            data.put("body",messageSessionId);
            data.put("title",type);

            mainObj.put("to", userFcmToken);
            mainObj.put("notification", notiObject);
            mainObj.put("data",data);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, response->{}, error -> {}) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}