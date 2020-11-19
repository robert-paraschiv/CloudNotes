package com.rokudoz.onotes.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.transition.TransitionInflater;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rokudoz.onotes.Adapters.HomePageAdapter;
import com.rokudoz.onotes.LoginActivity;
import com.rokudoz.onotes.MainActivity;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.Models.NoteDetails;
import com.rokudoz.onotes.Models.User;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorUtils;
import com.rokudoz.onotes.Utils.DbUtils;
import com.rokudoz.onotes.Utils.NotesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static com.rokudoz.onotes.App.HIDE_BANNER;
import static com.rokudoz.onotes.App.SETTINGS_PREFS_NAME;
import static com.rokudoz.onotes.App.TRANSITION_DURATION;

public class HomeFragment extends Fragment implements HomePageAdapter.OnItemClickListener {
    private static final String TAG = "HomeFragment";

    public static final int LAYOUT_STAGGERED_TYPE = 0;
    public static final int LAYOUT_LINEAR_TYPE = 1;

    private ActionMode actionMode;
    private MaterialToolbar materialToolbar;
    FloatingActionButton addNewNoteBtn;
    private View view;
    private RecyclerView recyclerView;
    ItemTouchHelper helper;
    private ImageView layoutManagerIcon;

    private TextView noNotesTv;

    SearchView searchView;

    private final Collaborator currentUserCollaborator = new Collaborator();

    private HomePageAdapter staggeredRecyclerViewAdapter;
    private final List<Note> noteList = new ArrayList<>();
    private User mUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("Users");
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListenerRegistration notesListener, userDetailsListener, trashNotesListener;

    private CircleImageView userPicture;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefsEditor;

    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //This fixes crash occurring if you click on some note and then press back fast, before enter animation finishes
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
                view = null;
            }
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        if (view == null) {
            Log.d(TAG, "onCreateView: VIEW WAS NULL");

            view = inflater.inflate(R.layout.fragment_home, container, false);
            recyclerView = view.findViewById(R.id.homeFragment_recyclerView);
            addNewNoteBtn = view.findViewById(R.id.homeFragment_addNoteFab);
            noNotesTv = view.findViewById(R.id.homeFragment_empty);
            searchView = view.findViewById(R.id.homeFragment_searchView);

            sharedPreferences = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
            sharedPrefsEditor = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();


            postponeEnterTransition();
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    staggeredRecyclerViewAdapter.getFilter().filter(query);
                    searchView.clearFocus();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    staggeredRecyclerViewAdapter.getFilter().filter(newText);
                    return false;
                }
            });


            materialToolbar = view.findViewById(R.id.homeFragment_toolbar);

            userPicture = view.findViewById(R.id.homeFragment_userImage);
            layoutManagerIcon = view.findViewById(R.id.homeFragment_layoutManagerIcon);


            addNewNoteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment)
                        Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToNewNoteFragment());

                }
            });

            buildRecyclerView();
            setupFirebaseAuth();

            setExitTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.grid_exit_transition)
                    .setDuration(TRANSITION_DURATION)); // EXIT transition duration must be equal to other fragment Enter transition duration
        } else {
            Log.d(TAG, "onCreateView: VIEW NOT NULL");
        }

        BannerAdManager bannerAdManager = new BannerAdManager();

        //Show Banner Ad
        if (getActivity() != null && !HIDE_BANNER) {
            bannerAdManager.showBannerAd(getActivity());
            //Move Add note Fab lower
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) addNewNoteBtn.getLayoutParams();
            params.setMargins(0, 0, bannerAdManager.convertDpToPixel(getActivity(), 16), bannerAdManager.convertDpToPixel(getActivity(), 66));

            //Move recyclerview lower
            CoordinatorLayout.LayoutParams recyclerviewParams = (CoordinatorLayout.LayoutParams) view.findViewById(R.id.homeFragment_recyclerView_layout)
                    .getLayoutParams();
            recyclerviewParams.setMargins(bannerAdManager.convertDpToPixel(getActivity(), 4),
                    0,
                    bannerAdManager.convertDpToPixel(getActivity(), 4),
                    bannerAdManager.convertDpToPixel(getActivity(), 50));
        } else if (getActivity() != null && HIDE_BANNER) {
            //Move Add note Fab lower
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) addNewNoteBtn.getLayoutParams();
            params.setMargins(0, 0, bannerAdManager.convertDpToPixel(getActivity(), 16), bannerAdManager.convertDpToPixel(getActivity(), 16));

            //Move recyclerview lower
            CoordinatorLayout.LayoutParams recyclerviewParams = (CoordinatorLayout.LayoutParams) view.findViewById(R.id.homeFragment_recyclerView_layout)
                    .getLayoutParams();
            recyclerviewParams.setMargins(bannerAdManager.convertDpToPixel(getActivity(), 4),
                    0,
                    bannerAdManager.convertDpToPixel(getActivity(), 4),
                    0);
//            recyclerView.setLayoutParams(recyclerviewParams);
//            Log.d(TAG, "onCreateView: modified recyclerview");
        }

        //Reset status bar color
        if (getActivity() != null) {
            ColorUtils.resetStatus_NavigationBar_Colors(getActivity());
        }

        if (getArguments() != null) {
            HomeFragmentArgs homeFragmentArgs = HomeFragmentArgs.fromBundle(getArguments());
            if (homeFragmentArgs.getNoteId() != null) {
                Note note = new Note();
                note.setNote_doc_ID(homeFragmentArgs.getNoteId());
//                Log.d(TAG, "onCreateView: " + note.getNote_doc_ID());
                if (noteList.size() > 0 && noteList.contains(note)) {
                    int position = noteList.indexOf(note);
                    noteList.remove(position);
                    staggeredRecyclerViewAdapter.notifyItemRemoved(position);
//                    Log.d(TAG, "onCreateView: removed note");
                }
            }
        }

        return view;
    }

    private void buildRecyclerView() {

        staggeredRecyclerViewAdapter = new HomePageAdapter(getActivity(), noteList);

        //Get last user selected type of home layout and apply it
        if (sharedPreferences.getInt("home_layout_manager_type", 0) == LAYOUT_STAGGERED_TYPE) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            layoutManagerIcon.setImageResource(R.drawable.ic_outline_view_agenda_24);
        } else if (sharedPreferences.getInt("home_layout_manager_type", 0) == LAYOUT_LINEAR_TYPE) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            layoutManagerIcon.setImageResource(R.drawable.ic_outline_dashboard_24);
        }

        //Change type of home layout on click : Staggered / Linear
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
                        noteList.get(i).setNote_position(i);
                        noteList.get(i + 1).setNote_position(i + 1);
                        staggeredRecyclerViewAdapter.notifyItemMoved(i, i + 1);

                        //Update notes individual position in the list
                        noteList.get(i).setChangedPos(true);
                        noteList.get(i + 1).setChangedPos(true);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(noteList, i, i - 1);
                        noteList.get(i).setNote_position(i);
                        noteList.get(i - 1).setNote_position(i - 1);
                        staggeredRecyclerViewAdapter.notifyItemMoved(i, i - 1);

                        //Update notes individual position in the list
                        noteList.get(i).setChangedPos(true);
                        noteList.get(i - 1).setChangedPos(true);
                    }
                }

                recyclerView.scrollToPosition(toPosition);


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
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            getNotes();
            getUserInfo();
        }
    }

    private void getNotes() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
            usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection(NotesUtils.NOTES_DETAILS)
                    .orderBy("note_position")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error == null && value != null && value.size() > 0) {
                                for (DocumentSnapshot noteDetailsSnapshot : value) {
                                    final NoteDetails noteDetails = noteDetailsSnapshot.toObject(NoteDetails.class);
                                    if (noteDetails != null) {
                                        db.collection("Notes").document(noteDetails.getNote_doc_id())
                                                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                                                        if (documentSnapshot != null && error == null) {
                                                            Note note = documentSnapshot.toObject(Note.class);
                                                            if (note != null) {
                                                                note.setNote_doc_ID(documentSnapshot.getId());
                                                                note.setNote_position(noteDetails.getNote_position());
                                                                note.setNote_background_color(noteDetails.getNote_background_color());
                                                                if (noteList.contains(note)) {

                                                                    int indexOfCurrentNote = noteList.indexOf(note);

                                                                    if (note.getDeleted()) {
                                                                        noteList.remove(note);
                                                                        Log.d(TAG, "onEvent: note deleted, removing");
                                                                        staggeredRecyclerViewAdapter.notifyItemRemoved(indexOfCurrentNote);
                                                                    } else {
                                                                        //Check if note are different
                                                                        if (NotesUtils.checkIfNotesAreDifferent(note, noteList.get(indexOfCurrentNote))) {
                                                                            noteList.set(indexOfCurrentNote, note);
                                                                            staggeredRecyclerViewAdapter.notifyItemChanged(indexOfCurrentNote);
                                                                            Log.d(TAG, "onEvent: note changed, notifying");

                                                                        } else if (checkIfBackgroundIsChanged(note, noteList.get(indexOfCurrentNote))) {
                                                                            //Notes are the same, user just changed color
                                                                            staggeredRecyclerViewAdapter.unhighlightViewHolder(recyclerView.getChildViewHolder(recyclerView
                                                                                    .getChildAt(indexOfCurrentNote)), note.getNote_background_color());

                                                                            //Change the note background color for the current user in the notes list
                                                                            noteList.get(noteList.indexOf(note)).setNote_background_color(note.getNote_background_color());

                                                                        } else if (checkIfPositionIsChanged(note, noteList.get(indexOfCurrentNote))) {
                                                                            //Change the note position for the current user in the notes list
                                                                            noteList.get(noteList.indexOf(note)).setNote_position(note.getNote_position());
                                                                        }
                                                                        //If user is no longer collaborator, remove note
                                                                        if (!note.getUsers().contains(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                                                            noteList.remove(note);
                                                                            staggeredRecyclerViewAdapter.notifyItemRemoved(indexOfCurrentNote);
                                                                            Log.d(TAG, "onEvent: user no longer collab + removing ");
                                                                        }

                                                                    }
                                                                } else { //Notes list does not contain this note
                                                                    if (!note.getDeleted()) {
                                                                        if (note.getNote_position() != null
                                                                                && noteList.size() >= note.getNote_position()) {
                                                                            noteList.add(note.getNote_position(), note);
                                                                            staggeredRecyclerViewAdapter.notifyItemInserted(noteList.indexOf(note));
                                                                            Log.d(TAG, "onEvent: added note " + note.getNoteTitle() + " at position "
                                                                                    + note.getNote_position());
                                                                        } else {
                                                                            noteList.add(note);
                                                                            staggeredRecyclerViewAdapter.notifyItemInserted(noteList.size() - 1);
                                                                            Log.d(TAG, "onEvent: added note default " + note.getNoteTitle() + " at position " + (noteList.size() - 1) +
                                                                                    " actual note position " + note.getNote_position());
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                });
                                    }

                                }
                            } else {
                                //No notes available, show no notes tv
                                noNotesTv.setVisibility(View.VISIBLE);
                            }
                        }
                    });

        }
    }

    private boolean checkIfBackgroundIsChanged(Note newNote, Note oldNote) {
        if (newNote.getNote_background_color() == null && oldNote.getNote_background_color() != null) {
            return true;
        } else if (newNote.getNote_background_color() != null && oldNote.getNote_background_color() == null) {
            return true;
        } else if (newNote.getNote_background_color() == null && oldNote.getNote_background_color() == null) {
            return false;
        }

        return !newNote.getNote_background_color().equals(oldNote.getNote_background_color());
    }


    private boolean checkIfPositionIsChanged(Note newNote, Note oldNote) {
        if (newNote.getNote_position() == null || oldNote.getNote_position() == null)
            return false;

        return !newNote.getNote_position().equals(oldNote.getNote_position());
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
        if (trashNotesListener != null) {
            trashNotesListener.remove();
            trashNotesListener = null;
        }

        //If the user has rearranged any notes, update their position
        updateNotesPositions();
    }

    private void updateNotesPositions() {
        for (final Note note : noteList) {
            if (note.getChangedPos() != null && note.getChangedPos()) {
                db.collection("Notes").document(note.getNote_doc_ID())
                        .update("collaboratorList", note.getCollaboratorList())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: updated position " + note.getNoteTitle() + " - "
                                        + note.getNote_position());
                            }
                        });
                usersRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection(NotesUtils.NOTES_DETAILS).document(note.getNote_doc_ID())
                        .update("note_position", note.getNote_position()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: updated user note position " + note.getNoteTitle() + " " + note.getNote_position());
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
                            mUser = documentSnapshot.toObject(User.class);
                            if (mUser != null) {
                                if (mUser.getUser_profile_picture() != null) {
                                    Glide.with(requireContext()).load(mUser.getUser_profile_picture()).centerCrop().into(userPicture);
                                    userPicture.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //Settings dialog
                                            showSettingsBottomSheet(mUser);
                                        }
                                    });
                                }
                                //User device token is null, update the db with new token
                                if (mUser.getUser_device_token() == null) {
                                    DbUtils.getCurrentRegistrationToken(mUser, TAG);
                                }
                            }
                        }
                    }
                });
    }


    @SuppressLint("SetTextI18n")
    private void showSettingsBottomSheet(User user) {
        final SharedPreferences.Editor sharedPrefsEditor = requireActivity()
                .getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE).edit();
        final SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);

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
            @SuppressLint("SetTextI18n")
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
                        LogOut();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
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
                final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
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

    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    /*
     ----------------------------- Firebase setup ---------------------------------
    */
    private void setupFirebaseAuth() {
//        Log.d(TAG, "setupFirebaseAuth: started");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //check if email is verified
                    if (user.isEmailVerified()) {
                        // DO STUFF

                        if (FirebaseAuth.getInstance().getCurrentUser() != null)
                            currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());
//                        Log.d(TAG, "onAuthStateChanged: MAIL VERIFIED");
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


    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
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
                    if (noteList.get(i).getCollaboratorList() != null && noteList.get(i).getCollaboratorList().contains(currentUserCollaborator))
                        if (noteList.get(i).getNote_background_color() == null
                                || noteList.get(i).getNote_background_color()
                                .equals("")) {
                            Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background);
                        } else {
                            switch (noteList.get(i).getNote_background_color()) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomBottomSheetDialogTheme);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_please_wait);
        final AlertDialog dialog = builder.create();
        dialog.show();
        final int[] notesDeleted = {0};
        for (final Note note : notesToDelete) {

            //If current user is the creator of the note, delete it
            if (note.getCreator_user_email().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())) {
                db.collection("Notes")
                        .document(note.getNote_doc_ID())
                        .update("deleted", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Deleted note " + note.getNoteTitle());
                        if (noteList.contains(note)) {
                            int position = noteList.indexOf(note);
                            noteList.remove(position);
                            staggeredRecyclerViewAdapter.notifyItemRemoved(position);
                        }

                        notesDeleted[0]++;
                        if (notesDeleted[0] == notesToDelete.size()) {
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
                batch.delete(db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection(NotesUtils.NOTES_DETAILS)
                        .document(note.getNote_doc_ID()));
                batch.update(db.collection("Notes").document(note.getNote_doc_ID()), "users", note.getUsers());
                batch.update(db.collection("Notes").document(note.getNote_doc_ID()), "collaboratorList", note.getCollaboratorList());
                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: updated collaborators successfully");

                        if (noteList.contains(note)) {
                            int position = noteList.indexOf(note);
                            noteList.remove(position);
                            staggeredRecyclerViewAdapter.notifyItemRemoved(position);
                        }

                        notesDeleted[0]++;
                        if (notesDeleted[0] == notesToDelete.size()) {
                            dialog.cancel();
                        }
                    }
                });
            }
        }
    }


    @Override
    public void onItemClick(final int position, final TextView title, final TextView text, final RecyclerView checkboxRv,
                            final RecyclerView collaboratorsRv, final RelativeLayout rootLayout) {
//        Log.d(TAG, "onItemClick: " + position);
        int selected = staggeredRecyclerViewAdapter.getSelected().size();
        if (actionMode == null) {
            Note note = noteList.get(position);
            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.homeFragment) {
                FragmentNavigator.Extras extras = null;
//                Log.d(TAG, "onCreateView: note position " + position);

                if (checkboxRv == null && text != null) {
                    extras = new FragmentNavigator.Extras.Builder()
//                            .addSharedElement(title, title.getTransitionName())
//                            .addSharedElement(text, text.getTransitionName())
//                            .addSharedElement(collaboratorsRv, collaboratorsRv.getTransitionName())
                            .addSharedElement(rootLayout, rootLayout.getTransitionName())
                            .build();
//                    Log.d(TAG, "onItemClick: CHECKBOX NULL");

                } else if (checkboxRv != null && text == null) {
                    extras = new FragmentNavigator.Extras.Builder()
//                            .addSharedElement(title, title.getTransitionName())
//                            .addSharedElement(checkboxRv, checkboxRv.getTransitionName())
//                            .addSharedElement(collaboratorsRv, collaboratorsRv.getTransitionName())
                            .addSharedElement(rootLayout, rootLayout.getTransitionName())
                            .build();
//                    Log.d(TAG, "onItemClick: TEXT NULL");

                }
                Log.d(TAG, "onItemClick: " + note.getNote_background_color());

                NavDirections navDirections = HomeFragmentDirections
                        .actionHomeFragmentToEditNoteFragment(note.getNote_doc_ID(),
                                note.getNote_background_color(),
                                position,
                                rootLayout.getTransitionName());
                if (extras != null) {
//                    Log.d(TAG, "onItemClick: extras not null");
                    Navigation.findNavController(view).navigate(navDirections, extras);
                }
            }


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
