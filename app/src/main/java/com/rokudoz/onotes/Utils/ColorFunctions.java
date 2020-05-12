package com.rokudoz.onotes.Utils;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.rokudoz.onotes.R;

public class ColorFunctions {
    public ColorFunctions() {
    }

    public void resetStatus_NavigationBar_Colors(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(ContextCompat.getColor(activity,R.color.fragments_background));
        window.setNavigationBarColor(ContextCompat.getColor(activity,R.color.fragments_background));
    }
}
