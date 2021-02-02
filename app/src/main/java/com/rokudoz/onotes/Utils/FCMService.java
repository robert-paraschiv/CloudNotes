package com.rokudoz.onotes.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rokudoz.onotes.App;
import com.rokudoz.onotes.MainActivity;
import com.rokudoz.onotes.R;

public class FCMService extends FirebaseMessagingService implements LifecycleObserver {

    private static final String TAG = "FCMService";
    private boolean isAppInForeground;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (isAppInForeground) {
                Log.d(TAG, "onMessageReceived: notification received while in foreground");
            } else {
                Log.d(TAG, "onMessageReceived: notification received while in background");
            }
            final String click_action = remoteMessage.getData().get("click_action");
            final String note_doc_id = remoteMessage.getData().get("noteID");

            sendNotification(remoteMessage.getData().get("body"), remoteMessage.getData().get("title"), click_action, note_doc_id);
        } else {
            Log.d(TAG, "onMessageReceived: NO payload");
            Log.d(TAG, "onMessageReceived: app foreground " + isAppInForeground);

            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle(), null, null);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    private void sendNotification(String body, String title, String click_action, String note_doc_id) {
        Intent intent = new Intent(click_action);
        intent.putExtra("note_doc_id", note_doc_id);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_notif_icon);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, App.CHANNEL_COLLABORATOR_NOTIFICATION)
                        .setSmallIcon(R.drawable.ic_notif_icon)
                        .setLargeIcon(rawBitmap)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onCreate() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onForegroundStart() {
        isAppInForeground = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onForegroundStop() {
        isAppInForeground = false;
    }
}
