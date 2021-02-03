package com.rokudoz.onotes;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {
    private static final String TAG = "App";
    SharedPreferences sharedPreferences;

    public static final String SETTINGS_PREFS_NAME = "SettingsPrefs";
    public static boolean HIDE_BANNER = false;
    public static boolean ASKED_ALREADY = false;
    public static final int TIMES_TO_OPEN_APP_TO_ASK_FOR_SUPPORT_AD = 7;
    public static final int MAX_HOME_CHECKBOX_NUMBER = 5;
    public static final int MAX_HOME_COLLABORATORS_PICTURES = 5;


    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

        //Increase Times app started so we can use it for showing rewarded ad
        final SharedPreferences.Editor sharedPrefsEditor = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();
        sharedPrefsEditor.putInt("TimesStartedCounter", sharedPreferences.getInt("TimesStartedCounter", 0) + 1);
        Log.d(TAG, "onCreate: TimesStartedCounter " + sharedPreferences.getInt("TimesStartedCounter", 0));
//        sharedPrefsEditor.putInt("TimesStartedCounter", 5);
        sharedPrefsEditor.apply();

        applyNightModeFromPrefs();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Messages Channel
            NotificationChannel channel_collaborator_notification = new NotificationChannel(
                    getString(R.string.default_notification_channel_id),
                    "Collaborator notifications channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel_collaborator_notification.setDescription("This is the channel used for collaborator notifications");
            channel_collaborator_notification.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel_collaborator_notification.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel_collaborator_notification);
        }
    }

    private void applyNightModeFromPrefs() {

        int mode = sharedPreferences.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}
