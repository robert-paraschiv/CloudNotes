package com.rokudoz.cloudnotes.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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

import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
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
import com.rokudoz.cloudnotes.MainActivity;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.Models.User;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static com.rokudoz.cloudnotes.App.ASKED_ALREADY;
import static com.rokudoz.cloudnotes.App.HIDE_BANNER;
import static com.rokudoz.cloudnotes.App.SETTINGS_PREFS_NAME;

public class HomeFragment extends Fragment implements HomePageAdapter.OnItemClickListener {
    private static final String TAG = "HomeFragment";

    public static int LAYOUT_STAGGERED_TYPE = 0;
    public static int LAYOUT_LINEAR_TYPE = 1;

    private ActionMode actionMode;
    private MaterialToolbar materialToolbar;

    private int layoutType = 0;

    private View view;
    private RecyclerView recyclerView;
    ItemTouchHelper helper;
    private ImageView layoutManagerIcon;

    private HomePageAdapter staggeredRecyclerViewAdapter;
    private List<Note> noteList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListenerRegistration notesListener, userDetailsListener, deletedNotesListener;

    private CircleImageView userPicture;

    private RewardedAd rewardedAd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefsEditor;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
        sharedPrefsEditor = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();
        if (getActivity() != null && !HIDE_BANNER) {
            getActivity().findViewById(R.id.bannerAdCard).setVisibility(View.VISIBLE);
        }
        materialToolbar = view.findViewById(R.id.homeFragment_toolbar);

        userPicture = view.findViewById(R.id.homeFragment_userImage);
        layoutManagerIcon = view.findViewById(R.id.homeFragment_layoutManagerIcon);

        FloatingActionButton addNewNoteBtn = view.findViewById(R.id.homeFragment_addNoteFab);
        addNewNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment)
                    Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToNewNoteFragment());

            }
        });

        rewardedAd = new RewardedAd(requireContext(), getResources().getString(R.string.rewarded_ad_unit_id));

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                Log.d(TAG, "onRewardedAdLoaded: rewarded ad loaded");

                if (sharedPreferences.getInt("TimesStartedCounter", 0) >= 5) {

                    //Dialog for sign out
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, (ViewGroup) view, false);
                    final Dialog dialog = new Dialog(requireActivity(), R.style.CustomBottomSheetDialogTheme);
                    MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
                    MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
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

                                        sharedPrefsEditor.putInt("TimesStartedCounter", 0);
                                        sharedPrefsEditor.apply();
                                        dialog.cancel();
                                    }

                                    @Override
                                    public void onRewardedAdFailedToShow(int errorCode) {
                                        // Ad failed to display.
                                    }
                                };
                                rewardedAd.show(getActivity(), adCallback);
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
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("4129AB584AC9547A6DDCE83E28748843") // Mi 9T Pro
                .addTestDevice("B141CB779F883EF84EA9A32A7D068B76") // RedMi 5 Plus
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        if (sharedPreferences.getInt("TimesStartedCounter", 0) >= 5 && !ASKED_ALREADY) {
            rewardedAd.loadAd(adRequest, adLoadCallback);
            ASKED_ALREADY = true;
        }


        buildRecyclerView();
        setupFirebaseAuth();

        return view;
    }

    private void buildRecyclerView() {
        recyclerView = view.findViewById(R.id.homeFragment_recyclerView);
        staggeredRecyclerViewAdapter = new HomePageAdapter(getActivity(), noteList);

        if (sharedPreferences.getInt("home_layout_manager_type", 0) == LAYOUT_STAGGERED_TYPE) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            layoutManagerIcon.setImageResource(R.drawable.ic_outline_view_agenda_24);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            layoutManagerIcon.setImageResource(R.drawable.ic_outline_dashboard_24);
        }

        layoutManagerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getInt("home_layout_manager_type", 0) == LAYOUT_STAGGERED_TYPE) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    sharedPrefsEditor.putInt("home_layout_manager_type", 1);
                    layoutManagerIcon.setImageResource(R.drawable.ic_outline_dashboard_24);
                } else {
                    StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(staggeredGridLayoutManager);
                    sharedPrefsEditor.putInt("home_layout_manager_type", 0);
                    layoutManagerIcon.setImageResource(R.drawable.ic_outline_view_agenda_24);
                }
                sharedPrefsEditor.apply();
            }
        });

        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        staggeredRecyclerViewAdapter.setOnItemClickListener(HomeFragment.this);

        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();


                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(noteList, i, i + 1);
                        noteList.get(i + 1).setPosition(i + 1);
                        noteList.get(i).setPosition(i);
                        staggeredRecyclerViewAdapter.notifyItemMoved(i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(noteList, i, i - 1);
                        noteList.get(i - 1).setPosition(i - 1);
                        noteList.get(i).setPosition(i);
                        staggeredRecyclerViewAdapter.notifyItemMoved(i, i - 1);
                    }
                }

                noteList.get(fromPosition).setChangedPos(true);
                noteList.get(toPosition).setChangedPos(true);

                if (actionMode != null) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            if (actionMode != null)
                                actionMode.finish();
                        }
                    }, 800);
                }

                return true;
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

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            getNotes(FirebaseAuth.getInstance().getCurrentUser().getUid());
            getUserInfo();
        }
    }

    private void getNotes(String uid) {
        noteList.clear();
        staggeredRecyclerViewAdapter.notifyDataSetChanged();
        notesListener = usersRef.document(uid).collection("Notes")
                .whereEqualTo("deleted", false)
                .orderBy("position", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && e == null) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Note note = documentSnapshot.toObject(Note.class);
                                if (note != null) {
                                    note.setNote_doc_ID(documentSnapshot.getId());
                                    if (noteList.contains(note)) {
                                        if (note.getDeleted()) {
                                            int notePosition = noteList.indexOf(note);
                                            noteList.remove(note);
                                            staggeredRecyclerViewAdapter.notifyItemRemoved(notePosition);
                                        } else {
                                            noteList.set(noteList.indexOf(note), note);
                                            staggeredRecyclerViewAdapter.notifyItemChanged(noteList.indexOf(note));
                                        }
                                    } else {
                                        if (!note.getDeleted()) {
                                            noteList.add(note);
                                            staggeredRecyclerViewAdapter.notifyItemInserted(noteList.size() - 1);
                                        }
                                    }
                                }
                                Log.d(TAG, "onEvent: " + note.toString());
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

        if (userDetailsListener != null) {
            userDetailsListener.remove();
            userDetailsListener = null;
        }
        if (notesListener != null) {
            notesListener.remove();
            notesListener = null;
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
        userDetailsListener = usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                                //Settings dialog
                                showSettingsBottomSheet(user);
                            }
                        });
                    }
                }
            }
        });
    }

    private void showSettingsBottomSheet(User user) {
        final SharedPreferences.Editor sharedPrefsEditor = requireActivity()
                .getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

        //Bottom sheet dialog for "Settings"
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, (ViewGroup) view, false);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(),
                R.style.CustomBottomSheetDialogTheme);

        LinearLayout themeLinearLayout = dialogView.findViewById(R.id.dialog_settings_theme_LL);
        TextView themeTextView = dialogView.findViewById(R.id.dialog_settings_theme_textView);
        CircleImageView profilePic = dialogView.findViewById(R.id.dialog_settings_profilePic);
        TextView emailTv = dialogView.findViewById(R.id.dialog_settings_email);
        TextView name = dialogView.findViewById(R.id.dialog_settings_name);
        LinearLayout trashLL = dialogView.findViewById(R.id.dialog_settings_trash_LL);
        MaterialButton signOutBtn = dialogView.findViewById(R.id.dialog_settings_signOut);
        Glide.with(profilePic).load(user.getUser_profile_picture()).centerCrop().into(profilePic);
        emailTv.setText(user.getEmail());
        name.setText(user.getUser_name());

        bottomSheetDialog.setContentView(dialogView);

        trashLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment)
                    Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToTrashFragment());

                bottomSheetDialog.cancel();
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dialog for close ad
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, (ViewGroup) view, false);
                final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
                MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
                MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
                TextView title = dialogView.findViewById(R.id.dialog_ShowAd_title);
                title.setText("Are you sure you want to log out?");
                confirmBtn.setText("Yes");
                dialog.setContentView(dialogView);
                dialog.setCancelable(false);

                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log out
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
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
                    requireActivity().finish();
                }
                // ...
            }
        };
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: " + position);
        int selected = staggeredRecyclerViewAdapter.getSelected().size();
        if (actionMode == null) {
            Note note = noteList.get(position);
            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment)
                Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(note.getNote_doc_ID()));
        } else {
            if (selected == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle("Selected: " + selected);
            }
        }
    }

    @Override
    public void onLongItemClick(int position) {
        int selected = staggeredRecyclerViewAdapter.getSelected().size();
        if (actionMode == null) {
            actionMode = materialToolbar.startActionMode(actionModeCallback);
            if (actionMode != null) {
                actionMode.setTitle("Selected: " + selected);
            }
        } else {
            if (selected == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle("Selected: " + selected);
            }
        }
    }


    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.cab_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete_notes) {
                // Delete btn is clicked, proceed to delete notes
                List<Note> notesToDelete = new ArrayList<>(staggeredRecyclerViewAdapter.getSelected());
                deleteSelectedNotes(notesToDelete);

                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            staggeredRecyclerViewAdapter.clearSelected();
            //Deselect selected items when action bar closes
            for (int i = 0; i < noteList.size(); i++) {
                Objects.requireNonNull(Objects.requireNonNull(recyclerView.getLayoutManager()).getChildAt(i))
                        .setBackgroundResource(R.drawable.home_note_background);
            }

            actionMode = null;
        }
    };

    private void deleteSelectedNotes(final List<Note> notesToDelete) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final int[] notesdeleted = {0};
        for (final Note note : notesToDelete) {
            usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(note.getNote_doc_ID())
                    .update("deleted", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: Deleted note " + note.getNoteTitle());
                    notesdeleted[0]++;
                    if (notesdeleted[0] == notesToDelete.size()) {
                        //Clear notes list and get them all again
                        getNotes(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        progressDialog.cancel();
                    }
                }
            });
        }
    }
}
