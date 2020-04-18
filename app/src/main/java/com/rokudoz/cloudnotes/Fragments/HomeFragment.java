package com.rokudoz.cloudnotes.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rokudoz.cloudnotes.Adapters.HomePageAdapter;
import com.rokudoz.cloudnotes.LoginActivity;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.Models.User;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static com.rokudoz.cloudnotes.App.SETTINGS_PREFS_NAME;

public class HomeFragment extends Fragment implements HomePageAdapter.OnItemClickListener {
    private static final String TAG = "HomeFragment";

    private View view;

    ItemTouchHelper helper;

    private HomePageAdapter staggeredRecyclerViewAdapter;
    private List<Note> noteList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListenerRegistration notesListener;

    private CircleImageView userPicture;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);


        userPicture = view.findViewById(R.id.homeFragment_userImage);

        FloatingActionButton addNewNoteBtn = view.findViewById(R.id.homeFragment_addNoteFab);
        addNewNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToNewNoteFragment());
            }
        });


        buildRecyclerView();
        setupFirebaseAuth();

        return view;
    }

    private void buildRecyclerView() {
        RecyclerView recyclerView = view.findViewById(R.id.homeFragment_recyclerView);
        staggeredRecyclerViewAdapter = new HomePageAdapter(getActivity(), noteList);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        staggeredRecyclerViewAdapter.setOnItemClickListener(HomeFragment.this);

        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();

                noteList.get(position_dragged).setPosition(position_target);
                noteList.get(position_target).setPosition(position_dragged);
                noteList.get(position_dragged).setChangedPos(true);
                noteList.get(position_target).setChangedPos(true);


                Collections.swap(noteList, position_dragged, position_target);
                staggeredRecyclerViewAdapter.notifyItemMoved(position_dragged, position_target);

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    private void getNotes(String uid) {
        usersRef.document(uid).collection("Notes")
                .orderBy("position", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && e == null) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Note note = documentSnapshot.toObject(Note.class);
                                if (note != null) {
                                    note.setNote_doc_ID(documentSnapshot.getId());
                                    if (!noteList.contains(note)) {
                                        noteList.add(note);
                                        staggeredRecyclerViewAdapter.notifyItemInserted(noteList.size() - 1);
                                    } else {
                                        noteList.set(noteList.indexOf(note), note);
                                        staggeredRecyclerViewAdapter.notifyItemChanged(noteList.indexOf(note));
                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
        for (Note note : noteList) {
            if (note.getChangedPos() != null && note.getChangedPos()) {
                usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("Notes").document(note.getNote_doc_ID())
                        .update("position", note.getPosition()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: updated position");
                    }
                });
            }
        }
    }

    private void getUserInfo() {
        usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null && documentSnapshot != null) {
                    final User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getUser_name() != null) {
                        Log.d(TAG, "onEvent: " + user.getUser_name());
                    }
                    if (user != null && user.getUser_profile_picture() != null) {
                        Glide.with(userPicture).load(user.getUser_profile_picture()).centerCrop().into(userPicture);
                        userPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final SharedPreferences.Editor sharedPrefsEditor = Objects.requireNonNull(getActivity())
                                        .getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();
                                final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

                                //Bottom sheet dialog for "Settings"
                                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, (ViewGroup) view, false);
                                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Objects.requireNonNull(getContext()),
                                        R.style.CustomBottomSheetDialogTheme);

                                LinearLayout themeLinearLayout = dialogView.findViewById(R.id.dialog_settings_theme_LL);
                                TextView themeTextView = dialogView.findViewById(R.id.dialog_settings_theme_textView);
                                CircleImageView profilePic = dialogView.findViewById(R.id.dialog_settings_profilePic);
                                TextView emailTv = dialogView.findViewById(R.id.dialog_settings_email);
                                TextView name = dialogView.findViewById(R.id.dialog_settings_name);
                                MaterialButton signOutBtn = dialogView.findViewById(R.id.dialog_settings_signOut);
                                Glide.with(profilePic).load(user.getUser_profile_picture()).centerCrop().into(profilePic);
                                emailTv.setText(user.getEmail());
                                name.setText(user.getUser_name());

                                bottomSheetDialog.setContentView(dialogView);

                                signOutBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //Dialog for sign out
                                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getActivity(),
                                                R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered);
                                        materialAlertDialogBuilder.setMessage("Are you sure you want to sign out?");
                                        materialAlertDialogBuilder.setCancelable(true);
                                        materialAlertDialogBuilder.setPositiveButton(
                                                "Yes",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(final DialogInterface dialog, int id) {
                                                        //Delete note
                                                        FirebaseAuth.getInstance().signOut();
                                                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                    }
                                                });

                                        materialAlertDialogBuilder.setNegativeButton(
                                                "No",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });

                                        materialAlertDialogBuilder.show();
                                    }
                                });
                                bottomSheetDialog.show();
                                Window window = bottomSheetDialog.getWindow();
                                if (window != null) {
                                    window.findViewById(com.google.android.material.R.id.container).setFitsSystemWindows(false);
                                    View decorView = window.getDecorView();
                                    decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                                }


                                //Set theme text view from prefs
                                switch (sharedPreferences.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
                                    case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                                        themeTextView.setText("System default");
                                        break;
                                    case AppCompatDelegate.MODE_NIGHT_NO:
                                        themeTextView.setText("Light");
                                        break;
                                    case AppCompatDelegate.MODE_NIGHT_YES:
                                        themeTextView.setText("Dark");
                                        break;
                                }

                                // Dialog for app theme
                                themeLinearLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        View themeView = getLayoutInflater().inflate(R.layout.dialog_theme_settings, (ViewGroup) view, false);
                                        final Dialog dialog = new Dialog(getContext(), R.style.CustomBottomSheetDialogTheme);
                                        RadioGroup appThemeRadioGroup = themeView.findViewById(R.id.settings_appTheme_radioGroup);
                                        dialog.setContentView(themeView);
                                        dialog.show();

                                        switch (sharedPreferences.getInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
                                            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                                                appThemeRadioGroup.check(R.id.dark_mode_follow_system);
                                                break;
                                            case AppCompatDelegate.MODE_NIGHT_NO:
                                                appThemeRadioGroup.check(R.id.dark_mode_light);
                                                break;
                                            case AppCompatDelegate.MODE_NIGHT_YES:
                                                appThemeRadioGroup.check(R.id.dark_mode_dark);
                                                break;
                                        }
                                        appThemeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                                switch (checkedId) {
                                                    case R.id.dark_mode_follow_system:
                                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                                        sharedPrefsEditor.putInt("NightMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                                        sharedPrefsEditor.apply();
                                                        bottomSheetDialog.cancel();
                                                        dialog.cancel();
                                                        break;
                                                    case R.id.dark_mode_light:
                                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                                        sharedPrefsEditor.putInt("NightMode", AppCompatDelegate.MODE_NIGHT_NO);
                                                        sharedPrefsEditor.apply();
                                                        bottomSheetDialog.cancel();
                                                        dialog.cancel();
                                                        break;
                                                    case R.id.dark_mode_dark:
                                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                                        sharedPrefsEditor.putInt("NightMode", AppCompatDelegate.MODE_NIGHT_YES);
                                                        sharedPrefsEditor.apply();
                                                        bottomSheetDialog.cancel();
                                                        dialog.cancel();
                                                        break;

                                                }
                                            }
                                        });

                                    }
                                });


                            }
                        });
                    }
                }
            }
        });
    }

    /*
     ----------------------------- Firebase setup ---------------------------------
    */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //check if email is verified
                    if (user.isEmailVerified()) {
                        // DO STUFF
                        getNotes(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        getUserInfo();
                        Log.d(TAG, "onAuthStateChanged: MAIL VERIFIED");
                    } else {
                        Toast.makeText(getActivity(), "Email is not Verified\nCheck your Inbox", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(getActivity(), "Not logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
                // ...
            }
        };
    }

    @Override
    public void onItemClick(int position) {
        Note note = noteList.get(position);
        Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(note.getNote_doc_ID()));
    }
}
