package com.rokudoz.cloudnotes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.rokudoz.cloudnotes.Utils.BannerAdManager;

import static com.rokudoz.cloudnotes.App.HIDE_BANNER;
import static com.rokudoz.cloudnotes.App.SETTINGS_PREFS_NAME;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    NavController navController;

    private MaterialButton closeAd;

    private RewardedAd rewardedAd;
    SharedPreferences sharedPreferences;

    private AdView mBannerAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

        closeAd = findViewById(R.id.closeAdBtn);


        //Banner ad section
        mBannerAd = findViewById(R.id.banner_adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("4129AB584AC9547A6DDCE83E28748843") // Mi 9T Pro
                .addTestDevice("B141CB779F883EF84EA9A32A7D068B76") // RedMi 5 Plus
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

        mBannerAd.loadAd(adRequest);


        //Rewarded ad section
        rewardedAd = new RewardedAd(MainActivity.this, getResources().getString(R.string.rewarded_ad_unit_id));

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                closeAd.setVisibility(View.VISIBLE);
                closeAd.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        //Dialog for close ad
                        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, null);
                        final Dialog dialog = new Dialog(MainActivity.this, R.style.CustomBottomSheetDialogTheme);
                        MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
                        MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
                        TextView title = dialogView.findViewById(R.id.dialog_ShowAd_title);
                        title.setText("You can hide this ad until the next time you open the app if you choose to watch another ad for a few seconds instead");
                        confirmBtn.setText("Watch ad");
                        dialog.setContentView(dialogView);
                        dialog.setCancelable(false);

                        confirmBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (rewardedAd.isLoaded()) {
                                    RewardedAdCallback adCallback = new RewardedAdCallback() {
                                        @Override
                                        public void onRewardedAdOpened() {
                                            // Ad opened.
                                        }

                                        @Override
                                        public void onRewardedAdClosed() {
                                            // Ad closed.
                                        }

                                        @Override
                                        public void onUserEarnedReward(@NonNull RewardItem reward) {
                                            // User earned reward.
                                            //Reset times app opened counter
                                            HIDE_BANNER = true;
                                            BannerAdManager bannerAdManager = new BannerAdManager();
                                            bannerAdManager.hideBanner_modify_layouts(MainActivity.this);


                                            dialog.cancel();
                                        }

                                        @Override
                                        public void onRewardedAdFailedToShow(int errorCode) {
                                            // Ad failed to display.
                                        }
                                    };
                                    rewardedAd.show(MainActivity.this, adCallback);
                                } else {
                                    Log.d("TAG", "The rewarded ad wasn't loaded yet.");
                                }
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
                });
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };

        AdRequest rewardedAdRequest = new AdRequest.Builder()
                .addTestDevice("4129AB584AC9547A6DDCE83E28748843") // Mi 9T Pro
                .addTestDevice("B141CB779F883EF84EA9A32A7D068B76") // RedMi 5 Plus
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

        if (!HIDE_BANNER)
            rewardedAd.loadAd(rewardedAdRequest, adLoadCallback);


    }


}
