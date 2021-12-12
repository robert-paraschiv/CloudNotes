package com.rokudoz.onotes.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.onotes.Adapters.CheckableItemAdapter;
import com.rokudoz.onotes.Adapters.CollaboratorNotesAdapter;
import com.rokudoz.onotes.Dialogs.FullBottomSheetDialogFragment;
import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.Models.NoteChange;
import com.rokudoz.onotes.Models.NoteDetails;
import com.rokudoz.onotes.Models.User;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorUtils;
import com.rokudoz.onotes.Utils.NotesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.rokudoz.onotes.Utils.NotesUtils.NOTES_DETAILS;
import static com.rokudoz.onotes.Utils.NotesUtils.NOTE_CHANGE_TYPE_ADDED;
import static com.rokudoz.onotes.Utils.NotesUtils.NOTE_CHANGE_TYPE_CHANGE;
import static com.rokudoz.onotes.Utils.NotesUtils.NOTE_CHANGE_TYPE_REMOVED;

public class NewNoteFragment extends Fragment implements CheckableItemAdapter.OnStartDragListener,
        CheckableItemAdapter.OnItemClickListener, FullBottomSheetDialogFragment.ExampleDialogListener, CollaboratorNotesAdapter.OnItemClickListener {

    private static final String TAG = "NewNoteFragment";

    int _note_background_color;

    int collaboratorsCounter = 0;

    ItemTouchHelper helper;
    private String noteType = "text";
    private View view;
    private TextInputEditText textInputEditText, titleInputEditText;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("Users");
    private DocumentReference noteRef;
    private Note mNote = new Note();

    private final List<CheckableItem> checkableItemList = new ArrayList<>();
    private RecyclerView recyclerView, collaboratorsRV;
    private RelativeLayout rv_checkableList_layout;
    private CheckableItemAdapter mAdapter;
    private CollaboratorNotesAdapter collaboratorNotesAdapter;
    private final List<Collaborator> mCollaboratorsList = new ArrayList<>();
    private MaterialButton checkboxModeBtn;
    private MaterialButton addCheckboxBtn;
    MaterialCardView bottomCard;
    private boolean discard = false;
    private boolean collaboratorsUpdated = false;

    public NewNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_home_edit_duration));
        materialContainerTransform.setElevationShadowEnabled(true);
        materialContainerTransform.setAllContainerColors(Color.TRANSPARENT);
        materialContainerTransform.setScrimColor(Color.TRANSPARENT);
        materialContainerTransform.setDrawingViewId(R.id.nav_host_fragment);
        setSharedElementEnterTransition(materialContainerTransform);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_note, container, false);

        bottomCard = view.findViewById(R.id.newNoteFragment_bottom_card);
        textInputEditText = view.findViewById(R.id.newNoteFragment_textInput);
        titleInputEditText = view.findViewById(R.id.newNoteFragment_title_textInput);
        checkboxModeBtn = view.findViewById(R.id.newNoteFragment_CheckBoxModeBtn);
        recyclerView = view.findViewById(R.id.newNoteFragment_checkbox_rv);
        addCheckboxBtn = view.findViewById(R.id.newNoteFragment_add_checkbox_Btn);
        rv_checkableList_layout = view.findViewById(R.id.newNoteFragment_scroll_rv);
        collaboratorsRV = view.findViewById(R.id.newNoteFragment_collaboratorsRV);
        MaterialButton settingsBtn = view.findViewById(R.id.newNoteFragment_settingsBtn);
        MaterialButton discardBtn = view.findViewById(R.id.newNoteFragment_discardBtn);

        view.findViewById(R.id.newNoteFragment_rootLayout).setTransitionName("addnewnote");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            List<Collaborator> collaborators = new ArrayList<>();
            collaborators.add(new Collaborator(
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                    FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).toString(), true));
            mNote.setCollaboratorList(collaborators);
        }


        _note_background_color = ContextCompat.getColor(requireContext(), R.color.fragments_background);
        //Reset status bar color
//        if (getActivity() != null) {
//            ColorFunctions.resetStatus_NavigationBar_Colors(getActivity());
//        }

        //Hide Banner Ad
        if (getActivity() != null) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.hideBannerAd(getActivity());
        }

        MaterialButton backBtn = view.findViewById(R.id.newNoteFragment_backBtn);
        backBtn.setOnClickListener(v -> {
            if (getActivity() != null)
                hideSoftKeyboard(getActivity());
            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.newNoteFragment)
                Navigation.findNavController(view).popBackStack();
        });


        titleInputEditText.setFocusableInTouchMode(true);
        titleInputEditText.post(() -> {
            titleInputEditText.requestFocusFromTouch();
            InputMethodManager lManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            lManager.showSoftInput(titleInputEditText, 0);
        });


        checkboxModeBtn.setOnClickListener(v -> {
            if (noteType.equals("text")) {
                checkboxModeBtn.setEnabled(false);

                checkableItemList.clear();
                mAdapter.notifyDataSetChanged();

                noteType = "checkbox";
                List<String> textList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(textInputEditText.getText()).toString().split("\n")));
                textInputEditText.setText("");
                textInputEditText.setVisibility(View.GONE);

                for (int i = 0; i < textList.size(); i++) {
                    String uid = "" + System.currentTimeMillis() + checkableItemList.size();
                    CheckableItem checkableItem = new CheckableItem(textList.get(i), false, uid);
                    checkableItem.setShouldBeFocused(true);
                    checkableItemList.add(checkableItem);
                    mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                }
                if (checkableItemList.size() == 0) {
                    String uid = "" + System.currentTimeMillis() + checkableItemList.size();
                    CheckableItem checkableItem = new CheckableItem("", false, uid);
                    checkableItem.setShouldBeFocused(true);
                    checkableItemList.add(checkableItem);
                    mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                }

                rv_checkableList_layout.setVisibility(View.VISIBLE);
                checkboxModeBtn.setIconResource(R.drawable.ic_outline_text_fields_24);
                checkboxModeBtn.setEnabled(true);

            } else if (noteType.equals("checkbox")) {
                checkboxModeBtn.setEnabled(false);

                StringBuilder text = new StringBuilder();
                for (int i = 0; i < checkableItemList.size(); i++) {
                    if (i == checkableItemList.size() - 1) {
                        text.append(checkableItemList.get(i).getText());
                    } else {
                        text.append(checkableItemList.get(i).getText()).append("\n");
                    }
                }
                noteType = "text";
                textInputEditText.setText(text);
                textInputEditText.setVisibility(View.VISIBLE);

                checkableItemList.clear();
                mAdapter.notifyDataSetChanged();

                rv_checkableList_layout.setVisibility(View.INVISIBLE);
                checkboxModeBtn.setIconResource(R.drawable.ic_outline_check_box_24);
                checkboxModeBtn.setEnabled(true);
            }

        });

        settingsBtn.setOnClickListener(v -> showColorSettings());

        discardBtn.setOnClickListener(v -> {
            if (getActivity() != null)
                hideSoftKeyboard(getActivity());
            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.newNoteFragment)
                Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());

            discard = true;
        });

        buildRecyclerView();
        return view;
    }

    private void buildRecyclerView() {
        collaboratorNotesAdapter = new CollaboratorNotesAdapter(mCollaboratorsList);
        collaboratorsRV.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        collaboratorsRV.setAdapter(collaboratorNotesAdapter);
        collaboratorNotesAdapter.setOnItemClickListener(this);

        mAdapter = new CheckableItemAdapter(checkableItemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnStartDragListener(this);
        mAdapter.setOnItemClickListener(this);

        String uid = "" + System.currentTimeMillis() + checkableItemList.size();
        checkableItemList.add(new CheckableItem("", false, uid));
        mAdapter.notifyItemInserted(checkableItemList.size() - 1);

        addCheckboxBtn.setOnClickListener(v -> {
            String uid1 = "" + System.currentTimeMillis() + checkableItemList.size();
            CheckableItem checkableItem = new CheckableItem("", false, uid1);
            checkableItem.setShouldBeFocused(true);
            checkableItemList.add(checkableItemList.size(), checkableItem);
            mAdapter.notifyItemInserted(checkableItemList.size() - 1);
        });

        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();

                //Swap notes position
                if (position_dragged < position_target) {
                    for (int i = position_dragged; i < position_target; i++) {
                        Collections.swap(checkableItemList, i, i + 1);
                        mAdapter.notifyItemMoved(i, i + 1);
                    }
                } else {
                    for (int i = position_dragged; i > position_target; i--) {
                        Collections.swap(checkableItemList, i, i - 1);
                        mAdapter.notifyItemMoved(i, i - 1);
                    }
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
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");

        if (discard) {
            Log.d(TAG, "onStop: discarded");
            return;
        }

        for (CheckableItem checkableItem : checkableItemList) {
            Log.d(TAG, "onStop: " + checkableItem.toString());
        }
        boolean empty = true;
        for (CheckableItem item : checkableItemList) {
            if (!item.getText().trim().equals(""))
                empty = false;
        }

        if (!Objects.requireNonNull(textInputEditText.getText()).toString().trim().equals("")
                || !Objects.requireNonNull(titleInputEditText.getText()).toString().trim().equals("")
                || !empty) {

            Note note;
            String title;
            if (Objects.requireNonNull(titleInputEditText.getText()).toString().trim().equals("")) {
                title = "";
            } else {
                title = titleInputEditText.getText().toString();
            }

            if (mNote.getUsers() == null) {
                mNote.setUsers(new ArrayList<>());
                mNote.getUsers().add(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
            }
            List<CheckableItem> currentCheckboxList = new ArrayList<>();
            for (CheckableItem item : checkableItemList) {
                if (!item.getText().trim().equals(""))
                    currentCheckboxList.add(new CheckableItem(item.getText(), item.getChecked(), item.getUid()));
            }


            note = new Note(title, null, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), null, false,
                    noteType, null, "Created", 0, false, mNote.getUsers(), mNote.getCollaboratorList(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                    null);
            note.setNote_background_color(mNote.getNote_background_color());

            if (noteType.equals("text")) {
                note.setNoteText(Objects.requireNonNull(textInputEditText.getText()).toString());
                note.setCheckableItemList(null);

            } else if (noteType.equals("checkbox")) {
                note.setCheckableItemList(currentCheckboxList);
                note.setNoteText("");
            }

            Log.d(TAG, "onStop: note ref " + noteRef);

            //If note hasn't been saved before
            if (noteRef == null) {
                final Note finalNote = note;
                db.collection("Notes").add(note)
                        .addOnSuccessListener(documentReference -> {
                            noteRef = documentReference;
                            Log.d(TAG, "onSuccess: note added " + noteRef.getId());

                            mNote = finalNote;
                            final WriteBatch batch = db.batch();

                            //Add initial "edit version"
                            batch.set(documentReference.collection("Edits").document(), finalNote);
                            collaboratorsCounter = 0; // Restart Counter

                            for (String email : mNote.getUsers()) {
                                usersRef.whereEqualTo("email", email).get().addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                        User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                        if (user != null) {
                                            collaboratorsCounter++;
                                            NoteDetails noteDetails = new NoteDetails(noteRef.getId(), user.getUser_id(), 0,
                                                    mNote.getNote_background_color());
                                            batch.set(usersRef.document(user.getUser_id()).collection(NOTES_DETAILS).document(noteRef.getId()), noteDetails);
                                            if (collaboratorsCounter == finalNote.getUsers().size()) {
                                                batch.commit().addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "onSuccess: added note details docs to collaborators ");
                                                    Log.d(TAG, "onSuccess: note color " + mNote.getNote_background_color());
                                                });
                                            }
                                        }
                                    }
                                });
                            }
                        });
            } else { //If note has been saved before
                note.setEdited(true);
                if (note.getNoteType().equals("text")) {
                    if (!mNote.getNoteText().equals(textInputEditText.getText().toString()) || !mNote.getNoteTitle().equals(titleInputEditText.getText().toString())) {
                        note.setEdit_type("Edited");
                        note.setNumber_of_edits(mNote.getNumber_of_edits() + 1);


                        //Compare current note text to old text and get changes
                        note.setNoteChangeList(getNoteTextChanges());

                        WriteBatch batch = db.batch();
                        batch.set(noteRef, note);
                        batch.set(noteRef.collection("Edits").document(), note);

                        final Note finalNote1 = note;
                        batch.commit().addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "onSuccess: updated note");
                            mNote = finalNote1;
                            UpdateCollaboratorsOfNote();
                        });
                    } else if (collaboratorsUpdated) {
                        UpdateCollaboratorsOfNote();
                    }
                } else {
                    Log.d(TAG, "onStop: note is checkbox type, beginning to check for differences");
                    //Check for checkbox list differences

                    //Compare notes checkboxes lists
                    boolean edited = NotesUtils.compareCheckableItemLists(mNote.getCheckableItemList(), checkableItemList);

                    Log.d(TAG, "onStop: edited= " + edited);

                    if (edited) {
                        note.setEdit_type("Edited");
                        note.setNumber_of_edits(mNote.getNumber_of_edits() + 1);

                        //Compare new checkbox values to old ones and get changes
                        note.setNoteChangeList(getNoteCheckboxChanges(currentCheckboxList));

                        WriteBatch batch = db.batch();
                        batch.set(noteRef, note);
                        batch.set(noteRef.collection("Edits").document(), note);

                        final Note finalNote1 = note;
                        batch.commit().addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "onSuccess: updated note");
                            mNote = finalNote1;
                            UpdateCollaboratorsOfNote();
                        });
                    } else if (collaboratorsUpdated) {
                        //check if collaborators have changed
                        UpdateCollaboratorsOfNote();
                    }
                }
            }
        } else {
            Log.d(TAG, "onStop: empty note discarded");
        }
    }

    private void UpdateCollaboratorsOfNote() {
        final WriteBatch batch = db.batch();
        batch.update(noteRef, "users", mNote.getUsers());
        batch.update(noteRef, "collaboratorList", mNote.getCollaboratorList());
        collaboratorsCounter = 0; // Restart Counter

        for (Collaborator collaborator : mNote.getCollaboratorList()) {
            usersRef.document(collaborator.getUser_id()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot != null) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        collaboratorsCounter++;
                        NoteDetails noteDetails = new NoteDetails(noteRef.getId(), user.getUser_id(), 0, mNote.getNote_background_color());
                        batch.set(usersRef.document(user.getUser_id()).collection(NOTES_DETAILS).document(noteRef.getId()), noteDetails);
                        if (collaboratorsCounter == mNote.getUsers().size()) {
                            batch.commit().addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "onSuccess: added note details docs to collaborators " + mNote.getNote_background_color());
                                collaboratorsUpdated = false;
                            });
                        }
                    }
                }
            });
        }
    }

    private List<NoteChange> getNoteCheckboxChanges(List<CheckableItem> currentCheckboxList) {
        List<NoteChange> noteChangeList = new ArrayList<>();

        List<CheckableItem> oldCheckboxList = mNote.getCheckableItemList();
        List<CheckableItem> comparedItemsList = new ArrayList<>();

        if (oldCheckboxList == null)
            oldCheckboxList = new ArrayList<>();

        for (CheckableItem oldItem : oldCheckboxList) {
            if (currentCheckboxList.contains(oldItem)) {

                CheckableItem newItem = currentCheckboxList.get(currentCheckboxList.indexOf(oldItem));
                comparedItemsList.add(newItem);

                if (!oldItem.getText().equals(newItem.getText()) || !oldItem.getChecked().equals(newItem.getChecked())) {
                    noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_CHANGE, newItem.getText(), oldItem.getText(), newItem.getChecked(), oldItem.getChecked()));
                }

            } else {
                noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_REMOVED, null, oldItem.getText(), null, oldItem.getChecked()));
            }
        }

        for (CheckableItem newItem : currentCheckboxList) {
            if (oldCheckboxList.contains(newItem)) {

                CheckableItem oldItem = oldCheckboxList.get(oldCheckboxList.indexOf(newItem));
                if (!comparedItemsList.contains(oldItem)) {
                    if (!oldItem.getText().equals(newItem.getText()) || !oldItem.getChecked().equals(newItem.getChecked())) {
                        comparedItemsList.add(oldItem);
                        noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_CHANGE, newItem.getText(), oldItem.getText(), newItem.getChecked(), oldItem.getChecked()));
                    }
                }
            } else {
                noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_ADDED, newItem.getText(), null, newItem.getChecked(), null));
            }
        }
        return noteChangeList;
    }

    private List<NoteChange> getNoteTextChanges() {
        List<NoteChange> noteChangeList = new ArrayList<>();

        List<String> currentNoteTextList = Arrays.asList(Objects.requireNonNull(textInputEditText.getText()).toString().split("\\r?\\n"));
        List<String> oldNoteTextList = Arrays.asList(mNote.getNoteText().split("\\r?\\n"));

        if (!mNote.getNoteType().equals(noteType))
            oldNoteTextList = new ArrayList<>();

        for (int i = 0; i < currentNoteTextList.size(); i++) {
            if (oldNoteTextList.size() > i) {
                if (!currentNoteTextList.get(i).equals(oldNoteTextList.get(i))) {
                    noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_CHANGE, currentNoteTextList.get(i), oldNoteTextList.get(i), null, null));
                }
            } else {
                noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_ADDED, currentNoteTextList.get(i), null, null, null));
            }
        }
        return noteChangeList;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
    }


    private void showColorSettings() {
        //Bottom sheet dialog for "Settings"
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_note_settings, (ViewGroup) view, false);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(),
                R.style.CustomBottomSheetDialogTheme);

        final CircleImageView yellow, red, blue, green, orange, purple, initial;
        final MaterialButton collaboratorsBtn;

        yellow = dialogView.findViewById(R.id.noteSettings_color_yellow);
        red = dialogView.findViewById(R.id.noteSettings_color_red);
        blue = dialogView.findViewById(R.id.noteSettings_color_blue);
        green = dialogView.findViewById(R.id.noteSettings_color_green);
        orange = dialogView.findViewById(R.id.noteSettings_color_orange);
        purple = dialogView.findViewById(R.id.noteSettings_color_purple);
        initial = dialogView.findViewById(R.id.noteSettings_color_initial);
        collaboratorsBtn = dialogView.findViewById(R.id.noteSettings_addCollaboratorBtn);

        final Collaborator currentUserCollaborator = new Collaborator();
        currentUserCollaborator.setUser_email(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        if (mNote.getNote_background_color() == null) {
            initial.setBorderWidth(5);
        } else {
            switch (mNote.getNote_background_color()) {
                case "yellow":
                    yellow.setBorderWidth(5);
                    break;
                case "red":
                    red.setBorderWidth(5);
                    break;
                case "green":
                    green.setBorderWidth(5);
                    break;
                case "blue":
                    blue.setBorderWidth(5);
                    break;
                case "orange":
                    orange.setBorderWidth(5);
                    break;
                case "purple":
                    purple.setBorderWidth(5);
                    break;
            }
        }

        yellow.setOnClickListener(v -> {
            Color_on_click("yellow", R.color.note_background_color_yellow, bottomSheetDialog);
            yellow.setBorderWidth(5);
        });
        red.setOnClickListener(v -> {
            Color_on_click("red", R.color.note_background_color_red, bottomSheetDialog);
            red.setBorderWidth(5);
        });
        blue.setOnClickListener(v -> {
            Color_on_click("blue", R.color.note_background_color_blue, bottomSheetDialog);
            blue.setBorderWidth(5);
        });
        green.setOnClickListener(v -> {
            Color_on_click("green", R.color.note_background_color_green, bottomSheetDialog);
            green.setBorderWidth(5);
        });
        orange.setOnClickListener(v -> {
            Color_on_click("orange", R.color.note_background_color_orange, bottomSheetDialog);
            orange.setBorderWidth(5);
        });
        purple.setOnClickListener(v -> {
            Color_on_click("purple", R.color.note_background_color_purple, bottomSheetDialog);
            purple.setBorderWidth(5);
        });
        initial.setOnClickListener(v -> {
            resetBackgroundColors();
            mNote.setNote_background_color("");
            initial.setBorderWidth(5);
            bottomSheetDialog.cancel();
        });

        collaboratorsBtn.setOnClickListener(v -> {
            bottomSheetDialog.cancel();

            showCollaboratorsDialog();
        });

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.findViewById(com.google.android.material.R.id.container).setFitsSystemWindows(false);
            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }
    }

    private void Color_on_click(String string, Integer color, BottomSheetDialog bottomSheetDialog) {
        setBackgroundColor(ContextCompat.getColor(requireContext(), color));
        mNote.setNote_background_color(string);
        bottomSheetDialog.cancel();
    }

    private void showCollaboratorsDialog() {
        List<Collaborator> collaboratorList = new ArrayList<>();

        //if note has no collaborators yet, add the current user
        if (mNote.getCollaboratorList() == null) {
            collaboratorList.add(new Collaborator(
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).toString(), true));
            mNote.setCollaboratorList(collaboratorList);
        } else if (mNote.getCollaboratorList().size() == 0) {
            collaboratorList.add(new Collaborator(
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).toString(), true));
            mNote.setCollaboratorList(collaboratorList);
        } else {
            collaboratorList.addAll(mNote.getCollaboratorList());
        }

        FullBottomSheetDialogFragment fullBottomSheetDialogFragment
                = new FullBottomSheetDialogFragment(_note_background_color, collaboratorList, true);
        fullBottomSheetDialogFragment.setTargetFragment(NewNoteFragment.this, 1);
        fullBottomSheetDialogFragment.show(getParentFragmentManager(), "");
    }

    private void setBackgroundColor(int color) {
        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);
        _note_background_color = color;
        bottomCard.setBackgroundColor(color);
        view.setBackgroundColor(color);
    }

    private void resetBackgroundColors() {
        if (getActivity() != null) {
            ColorUtils.resetStatus_NavigationBar_Colors(getActivity());

            MaterialCardView cardView = new MaterialCardView(requireContext());
            bottomCard.setBackgroundColor(cardView.getCardBackgroundColor().getDefaultColor());
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fragments_background));
            _note_background_color = ContextCompat.getColor(requireContext(), R.color.fragments_background);
        }
    }


    @Override
    public void onStartDrag(int position) {
        helper.startDrag(recyclerView.getChildViewHolder(Objects.requireNonNull(Objects.requireNonNull(recyclerView.getLayoutManager()).getChildAt(position))));
    }

    @Override
    public void onCheckClick(int position, boolean isChecked) {
        CheckableItem checkableItem = checkableItemList.get(position);
        checkableItemList.get(position).setChecked(isChecked);
        mAdapter.notifyItemChanged(position);

        //If user checks the item, move it to the end of the list
        if (isChecked && position < checkableItemList.size() - 1) {
            checkableItemList.remove(position);
            checkableItemList.add(checkableItemList.size(), checkableItem);
            mAdapter.notifyItemMoved(position, checkableItemList.size());
        }
    }

    @Override
    public void onTextChanged(int position, String text) {
        checkableItemList.get(position).setText(text);
//        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onDeleteClick(int position) {
        checkableItemList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onEnterPressed(int position) {
        String uid = "" + System.currentTimeMillis() + checkableItemList.size();
        CheckableItem checkableItem = new CheckableItem("", false, uid);
        checkableItem.setShouldBeFocused(true);
        checkableItemList.add(position + 1, checkableItem);
        mAdapter.notifyItemInserted(position + 1);
    }

    @Override
    public void getCollaborators(final List<Collaborator> collaboratorList) {
        final List<Collaborator> collaborators = new ArrayList<>();
        final List<String> userList = new ArrayList<>();

        //Get collaborators emails into user array
        for (final Collaborator collaborator : collaboratorList) {
            if (!collaborator.getUser_email().trim().equals("") && !userList.contains(collaborator.getUser_email())) {
                userList.add(collaborator.getUser_email());
            }
        }

        //Get collaborators pictures from db
        for (final Collaborator collaborator : collaboratorList) {
            if (!collaborator.getUser_email().trim().equals("")) {
                db.collection("Users").whereEqualTo("email", collaborator.getUser_email()).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                if (user != null) {
                                    boolean containsAlready = false;
                                    for (int i = 0; i < collaborators.size(); i++) {
                                        if (user.getEmail().equals(collaborators.get(i).getUser_email())) {
                                            containsAlready = true;
                                            break;
                                        }
                                    }
                                    if (!containsAlready) {
                                        if (collaborator.getCreator()) {
                                            collaborators.add(0, new Collaborator(user.getUser_id(), user.getEmail(), user.getUser_name(),
                                                    user.getUser_profile_picture(), collaborator.getCreator()));
                                        } else {
                                            collaborators.add(new Collaborator(user.getUser_id(), user.getEmail(), user.getUser_name(),
                                                    user.getUser_profile_picture(), collaborator.getCreator()));
                                        }

                                        //finished getting user pictures
                                        if (collaborators.size() == userList.size()) {
                                            mNote.setUsers(userList);
                                            mNote.setCollaboratorList(collaborators);
                                            collaboratorsUpdated = true;
                                            Log.d(TAG, "getCollaborators: " + collaboratorList.toString());


                                            if (mNote.getCollaboratorList() != null) {
                                                if (mNote.getCollaboratorList().size() > 1) {
                                                    collaboratorsRV.setVisibility(View.VISIBLE);
                                                    mCollaboratorsList.clear();
                                                    mCollaboratorsList.addAll(mNote.getCollaboratorList());
                                                    collaboratorNotesAdapter.notifyDataSetChanged();
                                                } else {
                                                    collaboratorsRV.setVisibility(View.GONE);
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        });
            }
        }


    }

    @Override
    public void onCollaboratorClick(int position) {
        showCollaboratorsDialog();
    }
}