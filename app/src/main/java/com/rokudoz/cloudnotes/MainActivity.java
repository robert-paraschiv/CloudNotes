package com.rokudoz.cloudnotes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.transition.Transition;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.transition.ChangeBounds;
import androidx.transition.Explode;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
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
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.cloudnotes.Adapters.HomePageAdapter;
import com.rokudoz.cloudnotes.Fragments.HomeFragment;
import com.rokudoz.cloudnotes.Fragments.HomeFragmentDirections;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.Models.User;
import com.rokudoz.cloudnotes.Utils.BannerAdManager;
import com.rokudoz.cloudnotes.Utils.ColorFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.rokudoz.cloudnotes.App.ASKED_ALREADY;
import static com.rokudoz.cloudnotes.App.HIDE_BANNER;
import static com.rokudoz.cloudnotes.App.SETTINGS_PREFS_NAME;

public class MainActivity extends AppCompatActivity implements HomePageAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";

    public static int LAYOUT_STAGGERED_TYPE = 0;
    public static int LAYOUT_LINEAR_TYPE = 1;

    private ActionMode actionMode;
    private MaterialToolbar materialToolbar;

    private RecyclerView recyclerView;
    ItemTouchHelper helper;
    private ImageView layoutManagerIcon;

    private HomePageAdapter staggeredRecyclerViewAdapter;
    private List<Note> noteList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListenerRegistration notesListener, userDetailsListener;

    private CircleImageView userPicture;

    private RewardedAd supportAppRewardedAd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefsEditor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);

        recyclerView = findViewById(R.id.homeFragment_recyclerView);
        FloatingActionButton addNewNoteBtn = findViewById(R.id.homeFragment_addNoteFab);

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
        sharedPrefsEditor = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();

        //Reset status bar color
        ColorFunctions colorFunctions = new ColorFunctions();
        colorFunctions.resetStatus_NavigationBar_Colors(this);

        BannerAdManager bannerAdManager = new BannerAdManager();


        //Show Banner Ad
        if (!HIDE_BANNER) {
            bannerAdManager.showBannerAd(this);
        } else if (HIDE_BANNER) {
            //Move Add note Fab lower
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) addNewNoteBtn.getLayoutParams();
            params.setMargins(0, 0, bannerAdManager.convertDpToPixel(this, 16), bannerAdManager.convertDpToPixel(this, 16));

            //Move recyclerview lower
            CoordinatorLayout.LayoutParams recyclerviewParams = (CoordinatorLayout.LayoutParams) findViewById(R.id.homeFragment_recyclerView_layout).getLayoutParams();
            recyclerviewParams.setMargins(bannerAdManager.convertDpToPixel(this, 4),
                    0,
                    bannerAdManager.convertDpToPixel(this, 4),
                    0);
//            recyclerView.setLayoutParams(recyclerviewParams);
            Log.d(TAG, "onCreateView: modified recyclerview");
        }

        materialToolbar = findViewById(R.id.homeFragment_toolbar);

        userPicture = findViewById(R.id.homeFragment_userImage);
        layoutManagerIcon = findViewById(R.id.homeFragment_layoutManagerIcon);


        addNewNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add new note activity
//                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment)
//                    Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToNewNoteFragment());

            }
        });

        if (savedInstanceState == null)
            buildRecyclerView();
        setupFirebaseAuth();
    }

    private void buildRecyclerView() {

        staggeredRecyclerViewAdapter = new HomePageAdapter(this, noteList);

        //Get last user selected type of home layout and apply it
        if (sharedPreferences.getInt("home_layout_manager_type", 0) == LAYOUT_STAGGERED_TYPE) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            layoutManagerIcon.setImageResource(R.drawable.ic_outline_view_agenda_24);
        } else if (sharedPreferences.getInt("home_layout_manager_type", 0) == LAYOUT_LINEAR_TYPE) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            layoutManagerIcon.setImageResource(R.drawable.ic_outline_dashboard_24);
        }

        //Change type of home layout on click : Staggered / Linear
        layoutManagerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getInt("home_layout_manager_type", 0) == LAYOUT_STAGGERED_TYPE) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
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
        staggeredRecyclerViewAdapter.setOnItemClickListener(MainActivity.this);

        //Touch helper to order notes on long press
        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();

                //Swap notes position
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

                recyclerView.scrollToPosition(toPosition);

                //Update notes individual position in the list
                noteList.get(fromPosition).setChangedPos(true);
                noteList.get(toPosition).setChangedPos(true);

                if (actionMode != null) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Stop action mode after delay
                            if (actionMode != null)
                                actionMode.finish();
                        }
                    }, 200);
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuthListener != null)
            FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {


        }
    }

    private void getNotes(String uid) {
//        noteList.clear();
//        if (staggeredRecyclerViewAdapter != null)
//            staggeredRecyclerViewAdapter.notifyDataSetChanged();
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null)
            notesListener = db.collection("Notes").whereArrayContains("users", FirebaseAuth.getInstance().getCurrentUser().getEmail())
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
                                                //TODO NEed to check if new note is different (Create a function you can use in many places, fucktard)
                                                Log.d(TAG, "onEvent: CHANGED " + noteList.indexOf(note));
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
                                }
                            }
                        }
                    });
    }

    @Override
    public void onStop() {
        super.onStop();
        //Detach Auth Listener
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }

        //Detach FireStore listeners
        if (userDetailsListener != null) {
            userDetailsListener.remove();
            userDetailsListener = null;
        }
        if (notesListener != null) {
            notesListener.remove();
            notesListener = null;
        }

        //If the user has rearranged any notes, update their position
        for (Note note : noteList) {
            if (note.getChangedPos() != null && note.getChangedPos()) {
                db.collection("Notes").document(note.getNote_doc_ID())
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
        userDetailsListener = usersRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e == null && documentSnapshot != null) {
                            final User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                if (user.getUser_name() != null) {
                                    Log.d(TAG, "onEvent: " + user.getUser_name());
                                }
                                if (user.getUser_profile_picture() != null) {
                                    Glide.with(MainActivity.this).load(user.getUser_profile_picture()).centerCrop().into(userPicture);
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
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void showSettingsBottomSheet(User user) {
        final SharedPreferences.Editor sharedPrefsEditor = this
                .getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();
        final SharedPreferences sharedPreferences = this.getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

        //Bottom sheet dialog for "Settings"
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this,
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

                // TODO: 08-May-20 Trash notes
//                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment)
//                    Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToTrashFragment());

                bottomSheetDialog.cancel();
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                //Dialog for close ad
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, null);
                final Dialog dialog = new Dialog(MainActivity.this, R.style.CustomBottomSheetDialogTheme);
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
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
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
                View themeView = getLayoutInflater().inflate(R.layout.dialog_theme_settings, null);
                final Dialog dialog = new Dialog(MainActivity.this, R.style.CustomBottomSheetDialogTheme);
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
                        getNotes(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        getUserInfo();
                        Log.d(TAG, "onAuthStateChanged: MAIL VERIFIED");
                    } else {
                        Toast.makeText(MainActivity.this, "Email is not Verified\nCheck your Inbox", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(MainActivity.this, "Not logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };
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
                if (recyclerView.getLayoutManager() != null && recyclerView.getLayoutManager().getChildAt(i) != null) {
                    if (noteList.get(i).getBackgroundColor() == null) {
                        Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background);
                    } else {
                        switch (noteList.get(i).getBackgroundColor()) {
                            case "yellow":
                                Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_yellow);
                                break;
                            case "red":
                                Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_red);
                                break;
                            case "green":
                                Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_green);
                                break;
                            case "blue":
                                Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_blue);
                                break;
                            case "orange":
                                Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_orange);
                                break;
                            case "purple":
                                Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_purple);
                                break;
                        }
                    }
                }
            }

            actionMode = null;
        }
    };

    private void deleteSelectedNotes(final List<Note> notesToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomBottomSheetDialogTheme);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_please_wait);
        final AlertDialog dialog = builder.create();
        dialog.show();
        final int[] notesdeleted = {0};
        for (final Note note : notesToDelete) {

            //If current user is the creator of the note, delete it
            if (note.getCreator_user_email().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())) {
                db.collection("Notes")
                        .document(note.getNote_doc_ID())
                        .update("deleted", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Deleted note " + note.getNoteTitle());
                        notesdeleted[0]++;
                        if (notesdeleted[0] == notesToDelete.size()) {
                            //Clear notes list and get them all again
                            getNotes(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            dialog.cancel();
                        }
                    }
                });
            } else {  //The current user isn't the creator of the note, update it and remove current user from collaborators
                for (int i = 0; i < note.getCollaboratorList().size(); i++) {
                    if (note.getCollaboratorList().get(i).getUser_email().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        note.getCollaboratorList().remove(i);
                        break;
                    }
                }
                for (int i = 0; i < note.getUsers().size(); i++) {
                    if (note.getUsers().get(i).equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        note.getUsers().remove(i);
                        break;
                    }
                }
                WriteBatch batch = db.batch();
                batch.update(db.collection("Notes").document(note.getNote_doc_ID()), "users", note.getUsers());
                batch.update(db.collection("Notes").document(note.getNote_doc_ID()), "collaboratorList", note.getCollaboratorList());
                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: updated collaborators successfully");
                        notesdeleted[0]++;
                        if (notesdeleted[0] == notesToDelete.size()) {
                            //Clear notes list and get them all again
                            getNotes(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            dialog.cancel();
                        }
                    }
                });
            }
        }
    }


    @Override
    public void onItemClick(final int position, final TextView title, final TextView text, final RecyclerView checkboxRv, final RecyclerView collaboratorsRv, final RelativeLayout rootLayout) {
        Log.d(TAG, "onItemClick: " + position);
        int selected = staggeredRecyclerViewAdapter.getSelected().size();
        if (actionMode == null) {
            Note note = noteList.get(position);
            FragmentNavigator.Extras extras = null;
            Log.d(TAG, "onCreateView: note position " + position);

            if (checkboxRv == null && text != null) {
                extras = new FragmentNavigator.Extras.Builder()
                        .addSharedElement(title, title.getTransitionName())
                        .addSharedElement(text, text.getTransitionName())
                        .addSharedElement(collaboratorsRv, collaboratorsRv.getTransitionName())
//                            .addSharedElement(rootLayout, rootLayout.getTransitionName())
                        .build();
                Log.d(TAG, "onItemClick: CHECKBOX NULL");

            } else if (checkboxRv != null && text == null) {
                extras = new FragmentNavigator.Extras.Builder()
                        .addSharedElement(title, title.getTransitionName())
                        .addSharedElement(checkboxRv, checkboxRv.getTransitionName())
                        .addSharedElement(collaboratorsRv, collaboratorsRv.getTransitionName())
//                            .addSharedElement(rootLayout, rootLayout.getTransitionName())
                        .build();
                Log.d(TAG, "onItemClick: TEXT NULL");

            }

//                NavDirections navDirections = HomeFragmentDirections
//                        .actionHomeFragmentToEditNoteFragment(note.getNote_doc_ID(), note.getBackgroundColor(), position);
//                if (extras != null) {
//                    Log.d(TAG, "onItemClick: extras not null");
//                    Navigation.findNavController(view).navigate(navDirections, extras);
//                }

            Transition explode = new android.transition.Explode();
//                this.getWindow().setExitTransition(explode);

            Intent intent = new Intent(this, EditNoteActivity.class);
            // create the transition animation - the images in the layouts
            // of both activities are defined with android:transitionName="robot"
            intent.putExtra("transition_name", rootLayout.getTransitionName());
            intent.putExtra("backgroundColor", note.getBackgroundColor());
//                Pair<View, String> pair1 = Pair.create((View) title, title.getTransitionName());
//                Pair<View, String> pair2 = Pair.create((View) text, text.getTransitionName());
//                Pair<View, String> pair3 = Pair.create((View)checkboxRv, checkboxRv.getTransitionName());
//                Pair<View, String> pair4 = Pair.create((View) collaboratorsRv, collaboratorsRv.getTransitionName());

            Log.d(TAG, "onItemClick: " + rootLayout.getTransitionName());

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, rootLayout, rootLayout.getTransitionName());
            // start the new activity
            startActivity(intent, options.toBundle());


        } else {
            if (selected == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle("" + selected);
            }
        }
    }

    @Override
    public void onLongItemClick(int position) {
        int selected = staggeredRecyclerViewAdapter.getSelected().size();
        if (actionMode == null) {
            actionMode = materialToolbar.startActionMode(actionModeCallback);
            if (actionMode != null) {
                actionMode.setTitle("" + selected);
            }
        } else {
            if (selected == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle("" + selected);
            }
        }
    }
}
