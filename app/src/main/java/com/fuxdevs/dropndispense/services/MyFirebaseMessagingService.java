package com.fuxdevs.dropndispense.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.fuxdevs.dropndispense.MainActivity;
import com.fuxdevs.dropndispense.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "parcel_notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        
        // Save token to Firestore
        saveFCMTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            String parcelTrackNo = remoteMessage.getData().get("parcel_track_no");
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            
            if (parcelTrackNo != null) {
                sendNotification(title, body, parcelTrackNo);
            }
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message notification: " + remoteMessage.getNotification().getBody());
            String parcelTrackNo = remoteMessage.getData().get("parcel_track_no");
            sendNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody(),
                parcelTrackNo
            );
        }
    }

    private void sendNotification(String title, String messageBody, String parcelTrackNo) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add parcel tracking number to intent
        if (parcelTrackNo != null) {
            intent.putExtra("parcel_track_no", parcelTrackNo);
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title != null ? title : "Parcel Update")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Parcel Notifications";
            String description = "Notifications for parcel updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void saveFCMTokenToFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Save to users collection
            db.collection("users").document(userId)
                .update("user_fcm_token", token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to users collection"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving FCM token to users", e));
            
            // Save to hardwares collection
            db.collection("hardwares").document(userId)
                .update("user_fcm_token", token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to hardwares collection"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving FCM token to hardwares", e));
        }
    }
}
