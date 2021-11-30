package com.rokudoz.onotes.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.rokudoz.onotes.R;

import static android.content.ContentValues.TAG;

public class BannerAdManager {
    public BannerAdManager() {
    }

    public void showBannerAd(Activity activity) {

//        View bannerLayout = activity.findViewById(R.id.banner_ad_layout);
//        ViewGroup parent = activity.findViewById(R.id.activity_main_root);
//
//        Transition transition = new Slide(Gravity.BOTTOM);
//        transition.setDuration(activity.getResources().getInteger(R.integer.transition_show_hide_bannerAd_duration));
//        transition.addTarget(R.id.banner_ad_layout);
//
//        TransitionManager.beginDelayedTransition(parent, transition);
//        bannerLayout.setVisibility(View.VISIBLE);
    }

    public void hideBannerAd(Activity activity) {

//        View bannerLayout = activity.findViewById(R.id.banner_ad_layout);
//        ViewGroup parent = activity.findViewById(R.id.activity_main_root);
//
//        Transition transition = new Slide(Gravity.BOTTOM);
//        transition.setDuration(activity.getResources().getInteger(R.integer.transition_show_hide_bannerAd_duration));
//        transition.addTarget(R.id.banner_ad_layout);
//
//        TransitionManager.beginDelayedTransition(parent, transition);
//        bannerLayout.setVisibility(View.GONE);

    }

//    public void hideBanner_modify_layouts(Activity activity) {
//        activity.findViewById(R.id.banner_ad_layout).setVisibility(View.GONE);
//
//        //Move Home Add note Fab lower
//        if (activity.findViewById(R.id.homeFragment_addNoteFab) != null) {
//
//            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) activity.findViewById(R.id.homeFragment_addNoteFab).getLayoutParams();
//            params.setMargins(0, 0, convertDpToPixel(activity, 16), convertDpToPixel(activity, 16));
//
//            activity.findViewById(R.id.homeFragment_addNoteFab).setLayoutParams(params);
//        }
//        if (activity.findViewById(R.id.homeFragment_recyclerView_layout) != null) {
//            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) activity.findViewById(R.id.homeFragment_recyclerView_layout).getLayoutParams();
//            params.setMargins(convertDpToPixel(activity, 4),
//                    0,
//                    convertDpToPixel(activity, 4),
//                    0);
//
//            activity.findViewById(R.id.homeFragment_recyclerView_layout).setLayoutParams(params);
//            Log.d(TAG, "hideBanner_modify_layouts: modified home recyclerview");
//        }else
//            Log.d(TAG, "hideBanner_modify_layouts: home recyclerview null");
//
//        //Move trash fragment recyclerview lower
//        if (activity.findViewById(R.id.trashFragment_recyclerView) != null) {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity.findViewById(R.id.trashFragment_recyclerView).getLayoutParams();
//            params.setMargins(convertDpToPixel(activity, 10)
//                    , 0
//                    , convertDpToPixel(activity, 10)
//                    , 0);
//            activity.findViewById(R.id.trashFragment_recyclerView).setLayoutParams(params);
//        }
//
//        //Move note edits fragment recyclerview lower
//        if (activity.findViewById(R.id.noteEditsFragment_recyclerView) != null) {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity.findViewById(R.id.noteEditsFragment_recyclerView).getLayoutParams();
//            params.setMargins(0
//                    , 0
//                    , 0
//                    , 0);
//            activity.findViewById(R.id.noteEditsFragment_recyclerView).setLayoutParams(params);
//        }
//
//        //Move view note edit fragment recyclerview and text lower
//        if (activity.findViewById(R.id.viewNoteEditFragment_recyclerView) != null && activity.findViewById(R.id.viewNoteEditFragment_text) != null) {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity.findViewById(R.id.viewNoteEditFragment_recyclerView).getLayoutParams();
//            params.setMargins(convertDpToPixel(activity, 8)
//                    , convertDpToPixel(activity, 8)
//                    , convertDpToPixel(activity, 8)
//                    , convertDpToPixel(activity, 8));
//            activity.findViewById(R.id.viewNoteEditFragment_recyclerView).setLayoutParams(params);
//
//            RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams) activity.findViewById(R.id.viewNoteEditFragment_textScrollView).getLayoutParams();
//            textParams.setMargins(convertDpToPixel(activity, 16)
//                    , convertDpToPixel(activity, 8)
//                    , convertDpToPixel(activity, 16)
//                    , convertDpToPixel(activity, 8));
//            activity.findViewById(R.id.viewNoteEditFragment_textScrollView).setLayoutParams(params);
//
//        }
//
//    }

    public int convertDpToPixel(Context context, float value) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                r.getDisplayMetrics()
        );
    }
}
