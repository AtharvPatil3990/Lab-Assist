package com.android.labassist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.labassist.auth.TokenManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.entities.NotificationEntity;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.UpdateFcmTokenRequest;
import com.android.labassist.network.models.UpdateFcmTokenResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseMsgService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "lab_assist_complaints";

    // 1. Triggered when the app is OPEN and a notification arrives
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "New Notification";
        String body = "";

        // Extract the title and body sent by our Edge Function
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        // Extract the custom data payload (e.g., complaint_id)
        Map<String, String> data = message.getData();

        // Show the visual notification
        sendLocalNotification(title, body, data);

        String complaintId = null;

        // 1. Extract data from the Notification payload (if it exists)
        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        // 2. Extract the hidden custom data (your complaint_id!)
        if (message.getData().size() > 0) {
            complaintId = message.getData().get("complaint_id");

            // Sometimes developers send title/body in the data payload instead,
            // so this is a safe fallback just in case.
            if (message.getData().containsKey("title")) title = message.getData().get("title");
            if (message.getData().containsKey("body")) body = message.getData().get("body");
        }

        // 3. Generate a local ID and timestamp
        long currentTime = System.currentTimeMillis();

        // 4. Create the Room Entity
        NotificationEntity newNotification = new NotificationEntity(
                title,
                body,
                complaintId,
                false, // is_read starts as false!
                currentTime
        );

        // 5. Save it directly to the local SQLite database.
        // NOTE: onMessageReceived already runs on a background thread,
        // so it is 100% safe to do a database insert here without freezing the app!
        try {
            AppDatabase.getInstance(getApplicationContext())
                    .labAssistDao()
                    .insertNotification(newNotification);

            Log.d("FCMService", "Notification saved to local Room database!");
        } catch (Exception e) {
            Log.e("FCMService", "Failed to save notification: " + e.getMessage());
        }
    }

    // 2. Triggered when Google assigns a fresh token to this phone
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        if (token.isEmpty()) return;

        Log.d(TAG, "New Firebase Token: " + token);

        // Only try to sync it to our Edge Function if the user is actually logged in!
        if (TokenManager.getInstance(this).getAccessToken() != null) {
            syncTokenToBackend(token);
        }
    }

    // 3. Helper method to send the token to Supabase
    private void syncTokenToBackend(String token) {
        UpdateFcmTokenRequest request = new UpdateFcmTokenRequest(token);
        ApiController.getInstance(this)
                .getPublicApi()
                .updateFcmToken(request)
                .enqueue(new Callback<UpdateFcmTokenResponse>() {
                    @Override
                    public void onResponse(Call<UpdateFcmTokenResponse> call, Response<UpdateFcmTokenResponse> response) {
                        Log.d(TAG, "Token synced successfully from background service.");
                    }

                    @Override
                    public void onFailure(Call<UpdateFcmTokenResponse> call, Throwable t) {
                        Log.e(TAG, "Failed to sync background token", t);

                    }
                });
    }

    // 4. Helper method to actually draw the notification on the screen
    private void sendLocalNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Pass the complaint_id from the Edge Function into the Intent so MainActivity can deep-link!
        if (data != null && data.containsKey("complaint_id")) {
            intent.putExtra("complaint_id", data.get("complaint_id"));
            intent.putExtra("status", data.get("status"));
        }

        // FLAG_IMMUTABLE is strictly required for modern Android versions
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        // TODO: Ensure you have a simple, transparent white icon named ic_notification in your drawable folder!
                        .setSmallIcon(R.drawable.ic_notification_logo)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Lab Complaints",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Alerts for new and updated lab complaints");
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        if (notificationManager != null) {
            // Using System.currentTimeMillis() ensures multiple notifications stack instead of overwriting each other
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }
}