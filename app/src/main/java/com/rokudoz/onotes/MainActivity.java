package com.rokudoz.onotes;

import static com.rokudoz.onotes.App.SETTINGS_PREFS_NAME;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    NavController navController;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefsEditor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getWindow().setFlags(FLAG_LAYOUT_NO_LIMITS, FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        findViewById(R.id.nav_host_fragment).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);


        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            if (getIntent() != null && getIntent().getStringExtra("note_doc_id") != null) {
                String note_doc_id = getIntent().getStringExtra("note_doc_id");
                Bundle args = new Bundle();
                args.putString("note_doc_ID", note_doc_id);
                args.putString("noteColor", null);
                args.putInt("position", 0);
                args.putString("transition_name", null);
                navController.navigate(R.id.editNoteFragment, args);
            }
        }

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
        sharedPrefsEditor = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();

    }

}
