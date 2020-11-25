package com.rokudoz.onotes.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.rokudoz.onotes.LoginActivity;
import com.rokudoz.onotes.R;

import static android.content.Context.MODE_PRIVATE;
import static com.rokudoz.onotes.App.SETTINGS_PREFS_NAME;

public class SettingsUtils extends Fragment {

    public static void showThemeDialog(Context context) {
        final SharedPreferences.Editor sharedPrefsEditor = context.getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();
        final SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

        View themeView = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_theme_settings, null, false);
        final Dialog dialog = new Dialog(context, R.style.CustomBottomSheetDialogTheme);
        RadioGroup appThemeRadioGroup = themeView.findViewById(R.id.settings_appTheme_radioGroup);
        dialog.setContentView(themeView);
        dialog.show();

        switch (sharedPreferences.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                appThemeRadioGroup.check(R.id.dark_mode_follow_system);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                appThemeRadioGroup.check(R.id.dark_mode_light);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                appThemeRadioGroup.check(R.id.dark_mode_dark);
                break;
        }
        appThemeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.dark_mode_follow_system) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    sharedPrefsEditor.putInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    sharedPrefsEditor.apply();
                    dialog.cancel();
                } else if (checkedId == R.id.dark_mode_light) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    sharedPrefsEditor.putInt("NightMode", AppCompatDelegate.MODE_NIGHT_NO);
                    sharedPrefsEditor.apply();
                    dialog.cancel();
                } else if (checkedId == R.id.dark_mode_dark) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    sharedPrefsEditor.putInt("NightMode", AppCompatDelegate.MODE_NIGHT_YES);
                    sharedPrefsEditor.apply();
                    dialog.cancel();
                }
            }
        });
    }


    public static void showLogOutDialog(final Context context) {
        //Dialog log out
        View dialogView = ((Activity) context).getLayoutInflater().inflate(R.layout.dialog_show_ad, null, false);
        final Dialog dialog = new Dialog(context, R.style.CustomBottomSheetDialogTheme);
        MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
        TextView title = dialogView.findViewById(R.id.dialog_ShowAd_title);
        title.setText("Are you sure you want to log out?");
        confirmBtn.setText("Yes");
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log out
                dialog.cancel();
                LogOut(context);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private static void LogOut(Context context) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

}
