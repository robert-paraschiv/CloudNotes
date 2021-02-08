package com.rokudoz.onotes.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorUtils;

public class TestFragment extends Fragment {
    private static final String TAG = "TestFragment";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("Users");
    RelativeLayout rootLayout;
    MaterialCardView bottomCard;
    TextInputEditText titleInput, textInput;
    View view;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setElevationShadowEnabled(true);
        materialContainerTransform.setAllContainerColors(Color.TRANSPARENT);
        materialContainerTransform.setScrimColor(Color.TRANSPARENT);
        materialContainerTransform.setDrawingViewId(R.id.nav_host_fragment);
        setSharedElementEnterTransition(materialContainerTransform);


//        setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.image_shared_element_transition)
//                .setDuration(getResources().getInteger(R.integer.transition_home_edit_duration)));

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_test, container, false);
        rootLayout = view.findViewById(R.id.testFragment_rootLayout);
        bottomCard = view.findViewById(R.id.testFragment_bottomCard);
        titleInput = view.findViewById(R.id.testFragment_titleEditText);
        textInput = view.findViewById(R.id.testFragment_textEditText);

        //Hide Banner Ad
        if (getActivity() != null) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.hideBannerAd(getActivity());
        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            TestFragmentArgs testFragmentArgs = TestFragmentArgs.fromBundle(getArguments());
            String docId = testFragmentArgs.getNoteDocID();
            rootLayout.setTransitionName("note_home_rootLayout" + docId);
            setBackgroundColor(ColorUtils.getColorIdFromString(testFragmentArgs.getNoteBackgroundColor(), requireContext()));

            if (testFragmentArgs.getNoteTitle() != null) {
                titleInput.setText(testFragmentArgs.getNoteTitle());
            }
            if (testFragmentArgs.getNoteText() != null) {
                textInput.setText(testFragmentArgs.getNoteText());
            }
        }

        postponeEnterTransition();

        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void setBackgroundColor(int color) {
        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);

        bottomCard.setBackgroundColor(color);
        view.setBackgroundColor(color);
    }

}