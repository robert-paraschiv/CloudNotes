package com.rokudoz.cloudnotes.Utils;

import android.app.Activity;
import android.view.View;

import com.rokudoz.cloudnotes.R;

public class BannerAdManager {
    public BannerAdManager() {
    }

    public void showBannerAd(Activity activity) {
        activity.findViewById(R.id.bannerAdCard).setVisibility(View.VISIBLE);
    }

    public void hideBannerAd(Activity activity) {
        activity.findViewById(R.id.bannerAdCard).setVisibility(View.GONE);
    }
}
