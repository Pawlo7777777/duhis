package com.example.duhis.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.duhis.utils.FirebaseHelper;
import com.example.duhis.utils.NotificationHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DuhisFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "DuhisFCM";
    private static final AtomicInteger notifId = new AtomicInteger(1000);

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        saveFcmToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "DUHIS";
        String body  = "You have a new notification";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body  = remoteMessage.getNotification().getBody();
        } else if (!remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();
            title = data.getOrDefault("title", title);
            body  = data.getOrDefault("body",  body);
        }

        NotificationHelper.showLocalNotification(this, title, body, notifId.incrementAndGet());
    }

    private void saveFcmToken(String token) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", token);
        FirebaseFirestore.getInstance()
                .collection(FirebaseHelper.RTDB_USERS)
                .document(uid)
                .update(update)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save token", e));
    }
}