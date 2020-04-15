package com.rokudoz.cloudnotes;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {
    private static final String TAG = "App";

    public static final String SETTINGS_PREFS_NAME = "SettingsPrefs";


    @Override
    public void onCreate() {
        super.onCreate();
        applyNightModeFromPrefs();
    }

    private void applyNightModeFromPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
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
