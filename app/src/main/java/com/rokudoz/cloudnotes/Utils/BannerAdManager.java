package com.rokudoz.cloudnotes.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.rokudoz.cloudnotes.R;

public class BannerAdManager {
    public BannerAdManager() {
    }

    public void showBannerAd(Activity activity) {
        activity.findViewById(R.id.banner_ad_layout).setVisibility(View.VISIBLE);
    }

    public void hideBannerAd(Activity activity) {
        activity.findViewById(R.id.banner_ad_layout).setVisibility(View.GONE);
    }

    public void hideBanner_modifyFab(Activity activity) {
        activity.findViewById(R.id.banner_ad_layout).setVisibility(View.GONE);


        //Move Add note Fab lower
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) activity.findViewById(R.id.homeFragment_addNoteFab).getLayoutParams();
        params.setMargins(0, 0, convertDpToPixel(activity, 16), convertDpToPixel(activity, 16));

        activity.findViewById(R.id.homeFragment_addNoteFab).setLayoutParams(params);
    }

    public int convertDpToPixel(Context context, float value) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                r.getDisplayMetrics()
        );
    }
}
