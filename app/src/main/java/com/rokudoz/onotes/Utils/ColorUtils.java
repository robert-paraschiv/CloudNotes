package com.rokudoz.onotes.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.rokudoz.onotes.R;

public class ColorUtils {
    public ColorUtils() {
    }

    public static void resetStatus_NavigationBar_Colors(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.fragments_background));
        window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.fragments_background));
    }

    public static int getColorIdFromString(String color, Context context) {
        if (color != null) {
            switch (color) {
                case "yellow":
                    return ContextCompat.getColor(context, R.color.note_background_color_yellow);
                case "red":
                    return ContextCompat.getColor(context, R.color.note_background_color_red);
                case "green":
                    return ContextCompat.getColor(context, R.color.note_background_color_green);
                case "blue":
                    return ContextCompat.getColor(context, R.color.note_background_color_blue);
                case "orange":
                    return ContextCompat.getColor(context, R.color.note_background_color_orange);
                case "purple":
                    return ContextCompat.getColor(context, R.color.note_background_color_purple);
                case "":
                    return ContextCompat.getColor(context, R.color.note_background_color_default);
            }
        } else {
            return ContextCompat.getColor(context, R.color.note_background_color_default);
        }
        return ContextCompat.getColor(context, R.color.note_background_color_default);
    }
}
