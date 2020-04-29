package com.rokudoz.cloudnotes.Utils;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import com.rokudoz.cloudnotes.R;

public class ColorFunctions {
    public ColorFunctions() {
    }

    public void resetStatus_NavigationBar_Colors(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(activity.getResources().getColor(R.color.fragments_background));
        window.setNavigationBarColor(activity.getResources().getColor(R.color.fragments_background));
    }
}
