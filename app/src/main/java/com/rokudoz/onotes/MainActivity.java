package com.rokudoz.onotes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rokudoz.onotes.Utils.BannerAdManager;

import java.util.ArrayList;
import java.util.List;

import static com.rokudoz.onotes.App.ASKED_ALREADY;
import static com.rokudoz.onotes.App.HIDE_BANNER;
import static com.rokudoz.onotes.App.SETTINGS_PREFS_NAME;
import static com.rokudoz.onotes.App.TIMES_TO_OPEN_APP_TO_ASK_FOR_SUPPORT_AD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    NavController navController;

    int failedToLoadAdCounter = 0;

    private MaterialButton closeAd;

    private RewardedAd closeBannerRewardedAd, supportAppRewardedAd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefsEditor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
        sharedPrefsEditor = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();

        closeAd = findViewById(R.id.closeAdBtn);


        //Banner ad section
        AdView mBannerAd = findViewById(R.id.banner_adView);
        List<String> testDevices = new ArrayList<>();
        testDevices.add("4129AB584AC9547A6DDCE83E28748843");
        testDevices.add("B141CB779F883EF84EA9A32A7D068B76");
        testDevices.add(AdRequest.DEVICE_ID_EMULATOR);
        RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        mBannerAd.loadAd(new AdRequest.Builder().build());

        //Show ads
        loadSupportAppRewardedAd();
        loadCloseBannerRewardedAd();
    }


    private void loadCloseBannerRewardedAd() {
        //Rewarded ad section
        closeBannerRewardedAd = new RewardedAd(this, getResources().getString(R.string.rewarded_ad_unit_id));

        final RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                super.onRewardedAdLoaded();
                closeAd.setVisibility(View.VISIBLE);
                showCloseBannerRewardedAd();
                Log.d(TAG, "onRewardedAdLoaded: Close banner ad loaded");
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                // Ad failed to load.
                super.onRewardedAdFailedToLoad(loadAdError);
                Toast.makeText(MainActivity.this, "FailedToLoad " + loadAdError.toString(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onRewardedAdFailedToLoad: Ad failed to load: " + loadAdError.toString());
                failedToLoadAdCounter++;
                if (failedToLoadAdCounter < 10)
                    loadCloseBannerRewardedAd();
            }
        };

        if (!HIDE_BANNER)
            closeBannerRewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);

    }

    private void showCloseBannerRewardedAd() {
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
                        if (closeBannerRewardedAd.isLoaded()) {
                            RewardedAdCallback adCallback = new RewardedAdCallback() {
                                @Override
                                public void onRewardedAdOpened() {
                                    // Ad opened.
                                    Log.d(TAG, "onRewardedAdOpened: ");
                                }

                                @Override
                                public void onRewardedAdClosed() {
                                    // Ad closed.
                                    Log.d(TAG, "onRewardedAdClosed: ad closed");
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
                                public void onRewardedAdFailedToShow(AdError adError) {
                                    // Ad failed to display.
                                    Toast.makeText(MainActivity.this, "FailedToShow", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onRewardedAdFailedToShow: " + adError.getMessage());
                                    dialog.cancel();
                                }
                            };

                            closeBannerRewardedAd.show(MainActivity.this, adCallback);
                        } else {
                            Log.d(TAG, "The rewarded ad wasn't loaded yet.");
                            Toast.makeText(MainActivity.this, "The Ad couldn't be shown", Toast.LENGTH_SHORT).show();
                            closeAd.setVisibility(View.GONE);
                            dialog.cancel();
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

    private void loadSupportAppRewardedAd() {
        supportAppRewardedAd = new RewardedAd(this, getResources().getString(R.string.rewarded_ad_unit_id));

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                showSupportRewardedAd();
                Log.d(TAG, "onRewardedAdLoaded: rewarded ad loaded");
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                // Ad failed to load.
                Log.d(TAG, "onRewardedAdFailedToLoad: " + loadAdError.toString());
                loadSupportAppRewardedAd();
            }
        };


        if (sharedPreferences.getInt("TimesStartedCounter", 0) >= TIMES_TO_OPEN_APP_TO_ASK_FOR_SUPPORT_AD && !ASKED_ALREADY) {
            supportAppRewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
            ASKED_ALREADY = true;
        }
    }

    private void showSupportRewardedAd() {
        if (sharedPreferences.getInt("TimesStartedCounter", 0) >= TIMES_TO_OPEN_APP_TO_ASK_FOR_SUPPORT_AD) {

            //Dialog for watch ad to support app
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, null);
            final Dialog dialog = new Dialog(this, R.style.CustomBottomSheetDialogTheme);
            MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
            MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
            dialog.setContentView(dialogView);
            dialog.setCancelable(true);
            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (supportAppRewardedAd.isLoaded()) {
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

                                sharedPrefsEditor.putInt("TimesStartedCounter", 0);
                                sharedPrefsEditor.apply();
                                dialog.cancel();
                            }

                            @Override
                            public void onRewardedAdFailedToShow(AdError adError) {
                                // Ad failed to display.
                            }
                        };
                        supportAppRewardedAd.show(MainActivity.this, adCallback);
                    } else {
                        Log.d("TAG", "The rewarded ad wasn't loaded yet.");
                        Toast.makeText(MainActivity.this, "The Ad couldn't be shown", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
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
    }


}
