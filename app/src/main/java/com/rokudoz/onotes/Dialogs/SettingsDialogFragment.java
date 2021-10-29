package com.rokudoz.onotes.Dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.rokudoz.onotes.Fragments.HomeFragmentDirections;
import com.rokudoz.onotes.LoginActivity;
import com.rokudoz.onotes.Models.User;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.SettingsUtils;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static com.rokudoz.onotes.App.SETTINGS_PREFS_NAME;

public class SettingsDialogFragment extends BottomSheetDialogFragment {
    private final User user;
    private final View parentView;

    public SettingsDialogFragment(User user, View parentView) {
        this.user = user;
        this.parentView = parentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

        //Bottom sheet dialog for "Settings"
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(),
                R.style.CustomBottomSheetDialogTheme);

        LinearLayout themeLinearLayout = dialogView.findViewById(R.id.dialog_settings_theme_LL);
        TextView themeTextView = dialogView.findViewById(R.id.dialog_settings_theme_textView);
        CircleImageView profilePic = dialogView.findViewById(R.id.dialog_settings_profilePic);
        TextView emailTv = dialogView.findViewById(R.id.dialog_settings_email);
        TextView name = dialogView.findViewById(R.id.dialog_settings_name);
        LinearLayout trashLL = dialogView.findViewById(R.id.dialog_settings_trash_LL);
        RelativeLayout accountRL = dialogView.findViewById(R.id.dialog_settings_account_RL);
        Glide.with(profilePic).load(user.getUser_profile_picture()).centerCrop().into(profilePic);
        emailTv.setText(user.getEmail());
        name.setText(user.getUser_name());

        bottomSheetDialog.setContentView(dialogView);

        trashLL.setOnClickListener(v -> {
            if (Objects.requireNonNull(Navigation.findNavController(parentView).getCurrentDestination()).getId() == R.id.homeFragment)
                Navigation.findNavController(parentView).navigate(HomeFragmentDirections.actionHomeFragmentToTrashFragment());

            bottomSheetDialog.cancel();
        });

        accountRL.setOnClickListener(v -> {
            //Hide bottom sheet to avoid screen flickering
            bottomSheetDialog.cancel();

            SettingsUtils.showLogOutDialog(requireContext());
        });
        bottomSheetDialog.show();

        //This allows the bottom sheet dialog to display over the nav bar and color it accordingly
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.findViewById(com.google.android.material.R.id.container).setFitsSystemWindows(false);
            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }

        //Set theme text view from prefs
        switch (sharedPreferences.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                themeTextView.setText(R.string.theme_system_default);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeTextView.setText(R.string.theme_light);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeTextView.setText(R.string.theme_dark);
                break;
        }

        // Dialog for app theme
        themeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Need to cancel the dialog instead of hiding because selecting a theme causes app
                 to restart and tries to show it again when it's only hidden and not cancelled */
                bottomSheetDialog.cancel();

                SettingsUtils.showThemeDialog(requireContext());
            }
        });

        return bottomSheetDialog;
    }
}
