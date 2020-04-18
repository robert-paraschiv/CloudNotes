package com.rokudoz.cloudnotes;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Objects;

public class App extends Application {
    private static final String TAG = "App";
    SharedPreferences sharedPreferences;
    public static final String SETTINGS_PREFS_NAME = "SettingsPrefs";


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
