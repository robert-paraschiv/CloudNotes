package com.rokudoz.cloudnotes;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    NavController navController;

    private AdView mBannerAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        mBannerAd = findViewById(R.id.banner_adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("4129AB584AC9547A6DDCE83E28748843") // Mi 9T Pro
                .addTestDevice("B141CB779F883EF84EA9A32A7D068B76") // RedMi 5 Plus
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();

        mBannerAd.loadAd(adRequest);

    }



}
