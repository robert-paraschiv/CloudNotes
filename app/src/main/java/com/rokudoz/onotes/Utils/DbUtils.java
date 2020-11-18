package com.rokudoz.onotes.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rokudoz.onotes.Models.User;

public class DbUtils {

    public static void updateUserTokenInDB(String user_id, String token, final String TAG) {
        FirebaseFirestore.getInstance().collection("Users").document(user_id).update("user_device_token", token)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Updated user token with new one");
                        } else {
                            Log.d(TAG, "onComplete: Failed to updated user device token");
                        }
                    }
                });
    }

    public static void getCurrentRegistrationToken(final User user, final String TAG) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();
                if (user != null) {
                    if (user.getUser_device_token() == null) {
                        updateUserTokenInDB(user.getUser_id(), token, TAG);
                    } else {
                        if (user.getUser_device_token().equals(token)) {
                            Log.d(TAG, "onComplete: User FCM token is the same");
                        } else {
                            // Update user fcm registration token in database
                            updateUserTokenInDB(user.getUser_id(), token, TAG);
                        }
                    }
                }
            }
        });
    }
}
