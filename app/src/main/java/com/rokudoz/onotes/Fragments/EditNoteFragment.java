package com.rokudoz.onotes.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.onotes.Adapters.CheckableItemAdapter;
import com.rokudoz.onotes.Adapters.CollaboratorNotesAdapter;
import com.rokudoz.onotes.Dialogs.FullBottomSheetDialogFragment;
import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.Models.User;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorFunctions;
import com.rokudoz.onotes.Utils.LastEdit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.rokudoz.onotes.App.TRANSITION_DURATION;

public class EditNoteFragment extends Fragment implements CheckableItemAdapter.OnStartDragListener,
        CheckableItemAdapter.OnItemClickListener, FullBottomSheetDialogFragment.ExampleDialogListener, CollaboratorNotesAdapter.OnItemClickListener {

    private static final String TAG = "EditNoteFragment";

    int _note_background_color;

    private boolean retrievedNote = false;

    private ProgressBar progressBar;

    private View view;
    private LinearLayout editLinearLayout;
    boolean edit = false;

    private String noteType = "text";
    private String noteID = "";
    private int position = 0;
    private int number_of_edits = 0;
    private List<CheckableItem> checkableItemList = new ArrayList<>();
    private RecyclerView recyclerView, collaboratorsRV;
    private RelativeLayout rv_checkbox_Layout;
    private CheckableItemAdapter mAdapter;
    private CollaboratorNotesAdapter collaboratorNotesAdapter;
    private ItemTouchHelper helper;

    private List<Collaborator> mCollaboratorsList = new ArrayList<>();
    private Collaborator currentUserCollaborator = new Collaborator();

    private Note mNote = new Note();
    TextInputEditText titleInput, textInput;
    TextView lastEditTv, numberOfEditsTv;
    MaterialButton backBtn, deleteBtn, checkboxModeBtn, addCheckboxBtn, optionsBtn;
    MaterialCardView bottomCard;

    private ListenerRegistration noteListener;

    private RelativeLayout rootLayout;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    public EditNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_note, container, false);

        postponeEnterTransition();

        currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        textInput = view.findViewById(R.id.editNoteFragment_textEditText);
        titleInput = view.findViewById(R.id.editNoteFragment_titleEditText);
        backBtn = view.findViewById(R.id.editNoteFragment_backBtn);
        lastEditTv = view.findViewById(R.id.editNoteFragment_lastEditTextView);
        numberOfEditsTv = view.findViewById(R.id.editNoteFragment_numberOfedits);
        deleteBtn = view.findViewById(R.id.editNoteFragment_deleteBtn);
        recyclerView = view.findViewById(R.id.editNoteFragment_checkbox_rv);
        checkboxModeBtn = view.findViewById(R.id.editNoteFragment_CheckBoxModeBtn);
        addCheckboxBtn = view.findViewById(R.id.editNoteFragment_add_checkbox_Btn);
        rv_checkbox_Layout = view.findViewById(R.id.editNoteFragment_scroll_rv);
        editLinearLayout = view.findViewById(R.id.editNoteFragment_editLayout);
        optionsBtn = view.findViewById(R.id.editNoteFragment_optionsBtn);
        bottomCard = view.findViewById(R.id.editNoteFragment_bottomCard);
        collaboratorsRV = view.findViewById(R.id.editNoteFragment_collaboratorsRV);
        progressBar = view.findViewById(R.id.editNoteFragment_progressBar);
        rootLayout = view.findViewById(R.id.rv_home_note_rootLayout);

        _note_background_color = ContextCompat.getColor(requireContext(), R.color.fragments_background);

        if (getArguments() != null) {
            EditNoteFragmentArgs editNoteFragmentArgs = EditNoteFragmentArgs.fromBundle(getArguments());
            noteID = editNoteFragmentArgs.getNoteDocID();
            int notePosition = editNoteFragmentArgs.getPosition();

            //Shared element transition
            titleInput.setTransitionName("note_home_title" + notePosition);
            textInput.setTransitionName("note_home_text" + notePosition);
            collaboratorsRV.setTransitionName("note_home_collaborators" + notePosition);
            recyclerView.setTransitionName("note_home_checkbox" + notePosition);
            rootLayout.setTransitionName(editNoteFragmentArgs.getTransitionName());

            Log.d(TAG, "onCreateView: note position " + notePosition);
            setupBackgroundColor(editNoteFragmentArgs.getNoteColor());
            getNote(noteID);
        }

        //Hide Banner Ad
        if (getActivity() != null) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.hideBannerAd(getActivity());
        }

        setupCheckboxModeButton();

        buildRecyclerView();


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(requireActivity());
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.editNoteFragment)
                    Navigation.findNavController(view).popBackStack();

            }
        });

        setupDeleteNoteBtn();

        //If user comes back from another fragment, hide progress bar
        if (mNote.getNoteTitle() != null)
            progressBar.setVisibility(View.GONE);

        setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.move)
                .setDuration(TRANSITION_DURATION)  // Enter transition duration must be equal to other fragment Exit transition duration
                .excludeTarget(R.id.editNoteFragment_toolbar, true)
                .excludeTarget(R.id.editNoteFragment_bottomCard, true));

        return view;
    }


    private void setupDeleteNoteBtn() {
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                //Dialog for delete note
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, (ViewGroup) view, false);
                final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
                MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
                MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
                TextView title = dialogView.findViewById(R.id.dialog_ShowAd_title);
                title.setText("Are you sure you want to move this note to trash?");
                dialog.setContentView(dialogView);

                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Delete note

                        //If current user is the creator of the note, delete it
                        if (mNote.getCreator_user_email().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())) {
                            db.collection("Notes").document(noteID)
                                    .update("deleted", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Deleted note", Toast.LENGTH_SHORT).show();
                                    hideSoftKeyboard(requireActivity());
                                    if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId()
                                            == R.id.editNoteFragment)
                                        Navigation.findNavController(view).popBackStack();
                                    dialog.cancel();
                                }
                            });
                        } else { //The current user isn't the creator of the note, update it and remove current user from collaborators
                            for (int i = 0; i < mNote.getCollaboratorList().size(); i++) {
                                if (mNote.getCollaboratorList().get(i).getUser_email().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                    mNote.getCollaboratorList().remove(i);
                                    break;
                                }
                            }
                            for (int i = 0; i < mNote.getUsers().size(); i++) {
                                if (mNote.getUsers().get(i).equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                    mNote.getUsers().remove(i);
                                    break;
                                }
                            }
                            WriteBatch batch = db.batch();
                            batch.update(db.collection("Notes").document(noteID), "users", mNote.getUsers());
                            batch.update(db.collection("Notes").document(noteID), "collaboratorList", mNote.getCollaboratorList());
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: updated collaborators successfully");

                                    //Current user is not a collaborator anymore, delete it from his home screen
                                    if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId()
                                            == R.id.editNoteFragment) {
                                        Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment(noteID));
                                    }
                                    dialog.cancel();
                                }
                            });


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

    private void setupCheckboxModeButton() {
        checkboxModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteType.equals("text")) {
                    checkboxModeBtn.setEnabled(false);

                    checkableItemList.clear();
                    mAdapter.notifyDataSetChanged();

                    noteType = "checkbox";
                    List<String> textList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(textInput.getText()).toString().split("\n")));
                    textInput.setText("");
                    textInput.setVisibility(View.GONE);

                    for (int i = 0; i < textList.size(); i++) {
                        checkableItemList.add(new CheckableItem(textList.get(i), false));
                        mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                    }

                    rv_checkbox_Layout.setVisibility(View.VISIBLE);
                    checkboxModeBtn.setIconResource(R.drawable.ic_outline_text_fields_24);
                    Log.d(TAG, "onClick: " + textList.toString());
                    Log.d(TAG, "onClick: " + checkableItemList.toString());
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
                    textInput.setText(text);
                    textInput.setVisibility(View.VISIBLE);

                    checkableItemList.clear();
                    mAdapter.notifyDataSetChanged();

                    rv_checkbox_Layout.setVisibility(View.INVISIBLE);
                    checkboxModeBtn.setIconResource(R.drawable.ic_outline_check_box_24);
                    Log.d(TAG, "onClick: " + checkableItemList.toString());
                    Log.d(TAG, "onClick: " + text);

                    checkboxModeBtn.setEnabled(true);
                }

            }
        });
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
            ColorFunctions colorFunctions = new ColorFunctions();
            colorFunctions.resetStatus_NavigationBar_Colors(getActivity());

            MaterialCardView cardView = new MaterialCardView(requireContext());
            bottomCard.setBackgroundColor(cardView.getCardBackgroundColor().getDefaultColor());
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.fragments_background));
            _note_background_color = ContextCompat.getColor(requireContext(), R.color.fragments_background);
        }
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

        checkableItemList.add(new CheckableItem("", false));
        mAdapter.notifyDataSetChanged();

        addCheckboxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckableItem checkableItem = new CheckableItem("", false);
                checkableItem.setShouldBeFocused(true);
                checkableItemList.add(checkableItemList.size(), checkableItem);
                mAdapter.notifyItemInserted(checkableItemList.size() - 1);
            }
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

                //allow saving
                edit = true;

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void getNote(final String noteID) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            noteListener = db.collection("Notes").document(noteID)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null && e == null && !retrievedNote) {
                                //hide progress bar
                                progressBar.setVisibility(View.GONE);


                                mNote = documentSnapshot.toObject(Note.class);
                                if (mNote != null) {
                                    mNote.setNote_doc_ID(documentSnapshot.getId());
                                    titleInput.setText(mNote.getNoteTitle());
                                    textInput.setText(mNote.getNoteText());

                                    if (mNote.getNumber_of_edits() != null)
                                        number_of_edits = mNote.getNumber_of_edits();

                                    if (mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).getNote_position() != null)
                                        position = mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).getNote_position();

                                    if (mNote.getNoteType() != null) {
                                        noteType = mNote.getNoteType();
                                    } else {
                                        noteType = "text";
                                    }
                                    if (noteType.equals("text")) {
                                        textInput.setVisibility(View.VISIBLE);
                                        checkableItemList.clear();
                                        mAdapter.notifyDataSetChanged();
                                        rv_checkbox_Layout.setVisibility(View.GONE);
                                        checkboxModeBtn.setIconResource(R.drawable.ic_outline_check_box_24);
                                    } else if (noteType.equals("checkbox")) {
                                        textInput.setText("");
                                        textInput.setVisibility(View.GONE);
                                        checkableItemList.clear();
                                        mAdapter.notifyDataSetChanged();

                                        for (CheckableItem item : mNote.getCheckableItemList()) {
                                            checkableItemList.add(new CheckableItem(item.getText(), item.getChecked()));
                                            mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                                        }
                                        rv_checkbox_Layout.setVisibility(View.VISIBLE);
                                        checkboxModeBtn.setIconResource(R.drawable.ic_outline_text_fields_24);
                                    }
                                    checkboxModeBtn.setVisibility(View.VISIBLE);
                                    if (mNote.getCreation_date() != null && mNote.getEdited() != null && mNote.getEdited()) {
                                        Date date = mNote.getCreation_date();
                                        LastEdit lastEdit = new LastEdit();
                                        lastEditTv.setText(MessageFormat.format("Last edit {0}", lastEdit.getLastEdit(date.getTime())));
                                        if (mNote.getNumber_of_edits() != null) {
                                            if (mNote.getNumber_of_edits() == 1) {
                                                numberOfEditsTv.setText(MessageFormat.format("{0} Edit", mNote.getNumber_of_edits()));
                                            } else if (mNote.getNumber_of_edits() > 1) {
                                                numberOfEditsTv.setText(MessageFormat.format("{0} Edits", mNote.getNumber_of_edits()));
                                            }
                                        } else {
                                            numberOfEditsTv.setText("No edits so far");
                                        }
                                        editLinearLayout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                boolean hasCollaborators = false;
                                                if (mNote.getCollaboratorList().size() > 1)
                                                    hasCollaborators = true;

                                                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId()
                                                        == R.id.editNoteFragment)
                                                    Navigation.findNavController(view).navigate(EditNoteFragmentDirections
                                                            .actionEditNoteFragmentToNoteEditsFragment(noteID,
                                                                    mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).getNote_background_color(),
                                                                    hasCollaborators));
                                            }
                                        });
                                    }

                                    //Get note color from DB and set it
                                    String color = mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).getNote_background_color();
                                    setupBackgroundColor(color);

                                    optionsBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showColorSettings();
                                        }
                                    });
                                    retrievedNote = true;

                                    Log.d(TAG, "onEvent: " + mNote.getCollaboratorList().toString());

                                    if (mNote.getCollaboratorList().size() > 1) {
                                        collaboratorsRV.setVisibility(View.VISIBLE);
                                        mCollaboratorsList.clear();
                                        mCollaboratorsList.addAll(mNote.getCollaboratorList());
                                        collaboratorNotesAdapter.notifyDataSetChanged();

                                    } else {
                                        collaboratorsRV.setVisibility(View.GONE);

                                    }


                                    //Start enter animation after info retrieved
                                    startPostponedEnterTransition();
                                }
                            } else if (retrievedNote) {
                                if (documentSnapshot != null && e == null) {
                                    Note newNote = documentSnapshot.toObject(Note.class);
                                    checkNoteEvent(newNote);
                                }
                                Log.d(TAG, "onEvent: GOT EVENT again");
                            }
                        }
                    });
    }

    private void checkNoteEvent(Note note) {
        //Snapshot listener received new event, need to check if note has been modified or just added collaborators

        if (note != null && mNote != null && !note.getLast_edited_by_user().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail())) {
            boolean newNoteIsDifferent = false;

            if (!note.getNoteType().equals(mNote.getNoteType())) {
                newNoteIsDifferent = true;


            } else {
                if (note.getNoteType().equals("text"))
                    if (!note.getNoteText().equals(mNote.getNoteText()))
                        newNoteIsDifferent = true;
                if (note.getNoteType().equals("checkbox")) {
                    if (note.getCheckableItemList() != null && mNote.getCheckableItemList() != null) {
                        if (note.getCheckableItemList().size() != mNote.getCheckableItemList().size())
                            newNoteIsDifferent = true;

                        for (CheckableItem item : note.getCheckableItemList()) {
                            if (!mNote.getCheckableItemList().contains(item) && !item.getText().equals("")) {
                                newNoteIsDifferent = true;
                            } else if (mNote.getCheckableItemList().contains(item)) {
                                if (mNote.getCheckableItemList().get(mNote.getCheckableItemList().indexOf(item)).getChecked() != item.getChecked()) {
                                    newNoteIsDifferent = true;
                                }
                            }
                        }
                        for (CheckableItem item : mNote.getCheckableItemList()) {
                            if (!note.getCheckableItemList().contains(item)) {
                                newNoteIsDifferent = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!note.getNoteTitle().equals(mNote.getNoteTitle()))
                newNoteIsDifferent = true;


            if (newNoteIsDifferent && getActivity() != null) {
                Log.d(TAG, "checkNoteEvent: Someone has just edited this note");
                showReloadDataDialog();
            }

        }


        //Set collaborators list if someone updates them meanwhile
        if (mNote != null && note != null) {
            mNote.setCollaboratorList(note.getCollaboratorList());
            mCollaboratorsList.clear();
            mCollaboratorsList.addAll(mNote.getCollaboratorList());
            collaboratorNotesAdapter.notifyDataSetChanged();
        }
    }

    private void showReloadDataDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, null);
        final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
        TextView textView = dialogView.findViewById(R.id.dialog_ShowAd_title);
        textView.setText("Someone else has just edited this note, do you want to refresh the note ?");
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                retrievedNote = false;
                mNote.getCheckableItemList().clear();
                getNote(noteID);
                dialog.cancel();
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

    private void setupBackgroundColor(String color) {
        if (color != null && getActivity() != null) {
            switch (color) {
                case "yellow":
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_yellow));
                    break;
                case "red":
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_red));
                    break;
                case "green":
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_green));
                    break;
                case "blue":
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_blue));
                    break;
                case "orange":
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_orange));
                    break;
                case "purple":
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_purple));
                    break;
            }
        } else {
            resetBackgroundColors();
        }
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

        if (mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).getNote_background_color() == null) {
            initial.setBorderWidth(5);
        } else {
            switch (mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).getNote_background_color()) {
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

        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("yellow");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_yellow));
                mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color("yellow");
                yellow.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("red");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_red));
                mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color("red");
                red.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("blue");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_blue));
                mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color("blue");
                blue.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("green");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_green));
                mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color("green");
                green.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("orange");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_orange));
                mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color("orange");
                orange.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("purple");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_purple));
                mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color("purple");
                purple.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor(null);
                resetBackgroundColors();
                mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color(null);
                initial.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });


        collaboratorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();

                showCollaboratorsDialog();
            }
        });

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.findViewById(com.google.android.material.R.id.container).setFitsSystemWindows(false);
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    private void showCollaboratorsDialog() {
        boolean isOwner = Objects.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), mNote.getCreator_user_email());

        List<Collaborator> collaboratorList = new ArrayList<>();

        //if note has no collaborators yet, add the current user
        if (mNote.getCollaboratorList() == null) {
            collaboratorList.add(new Collaborator(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).toString(), true,
                    0, ""));
            mNote.setCollaboratorList(collaboratorList);
        } else if (mNote.getCollaboratorList().size() == 0) {
            collaboratorList.add(new Collaborator(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName(),
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).toString(), true,
                    0, ""));
            mNote.setCollaboratorList(collaboratorList);
        } else {
            collaboratorList.addAll(mNote.getCollaboratorList());
        }

        FullBottomSheetDialogFragment fullBottomSheetDialogFragment =
                new FullBottomSheetDialogFragment(_note_background_color, collaboratorList, isOwner);
//        fullBottomSheetDialogFragment.setCancelable(false);
        fullBottomSheetDialogFragment.setTargetFragment(EditNoteFragment.this, 2);
        fullBottomSheetDialogFragment.show(getParentFragmentManager(), "");
    }

    private void updateNoteColor(final String noteColor) {
        mNote.getCollaboratorList().get(mNote.getCollaboratorList().indexOf(currentUserCollaborator)).setNote_background_color(noteColor);
        db.collection("Notes").document(noteID)
                .update("collaboratorList", mNote.getCollaboratorList()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: updated note color + " + noteColor);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (noteListener != null) {
            noteListener.remove();
            noteListener = null;
        }

        //reset retrievedNote
        retrievedNote = false;
        Log.d(TAG, "onStop: ");

        Log.d(TAG, "onStop: " + checkableItemList.toString());
        Log.d(TAG, "onStop: " + mNote.getCheckableItemList().toString());

        if (mNote != null) {

            for (CheckableItem item : checkableItemList) {
                if (!mNote.getCheckableItemList().contains(item) && !item.getText().equals("")) {
                    Log.d(TAG, "onStop: mnote doesnt contain " + item.getText());
                    edit = true;
                } else if (mNote.getCheckableItemList().contains(item)) {
                    if (mNote.getCheckableItemList().get(mNote.getCheckableItemList().indexOf(item)).getChecked() != item.getChecked()) {
                        Log.d(TAG, "onStop: mnote contains but checked different " + item.getText());
                        edit = true;
                    }
                }
            }
            for (CheckableItem item : mNote.getCheckableItemList()) {
                if (!checkableItemList.contains(item)) {
                    Log.d(TAG, "onStop: checkableItemList doesnt contain " + item.getText());
                    edit = true;
                    break;
                } else {
                    if (checkableItemList.get(checkableItemList.indexOf(item)).getChecked() != item.getChecked()) {
                        edit = true;
                        Log.d(TAG, "onStop: checkableItemList contains but checked differently" + item.getText());
                        break;
                    }
                }
            }

            List<CheckableItem> tempCheckableList = new ArrayList<>();
            for (CheckableItem item : checkableItemList) {
                if (!item.getText().trim().equals(""))
                    tempCheckableList.add(new CheckableItem(item.getText(), item.getChecked()));
            }

            Log.d(TAG, "onStop: " + edit);

            if (!mNote.getNoteText().equals(Objects.requireNonNull(textInput.getText()).toString())
                    || !mNote.getNoteTitle().equals(Objects.requireNonNull(titleInput.getText()).toString())
                    || edit) {
                Note note = new Note();
                String title;
                if (Objects.requireNonNull(titleInput.getText()).toString().trim().equals("")) {
                    title = "";
                } else {
                    title = titleInput.getText().toString();
                }
                if (noteType.equals("text")) {
                    note = new Note(title,
                            Objects.requireNonNull(textInput.getText()).toString(),
                            mNote.getCreator_user_email(),
                            null, true, noteType, null, "Edited", number_of_edits + 1,
                            false, mNote.getUsers(), mNote.getCollaboratorList(),
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
                } else if (noteType.equals("checkbox")) {
                    note = new Note(
                            title,
                            "",
                            mNote.getCreator_user_email(),
                            null, true, noteType, tempCheckableList, "Edited", number_of_edits + 1,
                            false, mNote.getUsers(), mNote.getCollaboratorList(),
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
                }

                WriteBatch batch = db.batch();
                batch.set(db.collection("Notes").document(noteID), note);
                batch.set(db.collection("Notes").document(noteID).collection("Edits").document(), note);

                final Note finalNote = note;
                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Updated note successfully");
                        edit = false;
                        if (finalNote.getNoteType().equals("checkbox")) {
                            mNote = finalNote;
                        }
                        if (getActivity() != null)
                            hideSoftKeyboard(getActivity());
                    }
                });
            } else {
                Log.d(TAG, "Note was the same, going back");
            }
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
        CheckableItem checkableItem = new CheckableItem("", false);
        checkableItem.setShouldBeFocused(true);
        checkableItemList.add(position + 1, checkableItem);
        mAdapter.notifyItemInserted(position + 1);
    }

    @Override
    public void getCollaborators(final List<Collaborator> collaboratorList) {
        boolean stillCollaborator = false;
        final List<Collaborator> collaborators = new ArrayList<>();
        final List<String> userList = new ArrayList<>();

        //Get collaborators emails into user array and find if current user removed himself or not
        for (final Collaborator collaborator : collaboratorList) {
            if (!collaborator.getUser_email().trim().equals("") && !userList.contains(collaborator.getUser_email())) {
                userList.add(collaborator.getUser_email());
                if (collaborator.getUser_email().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()))
                    stillCollaborator = true;
            }
        }
        //Get collaborators pictures
        for (final Collaborator collaborator : collaboratorList) {
            if (!collaborator.getUser_email().trim().equals("")) {
                final boolean finalStillCollaborator = stillCollaborator;
                db.collection("Users").whereEqualTo("email", collaborator.getUser_email()).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
                                                collaborators.add(0, new Collaborator(user.getEmail(), user.getUser_name(),
                                                        user.getUser_profile_picture(), collaborator.getCreator(), 0, collaborator.getNote_background_color()));
                                            } else {
                                                collaborators.add(new Collaborator(user.getEmail(), user.getUser_name(),
                                                        user.getUser_profile_picture(), collaborator.getCreator(), 0, collaborator.getNote_background_color()));
                                            }


                                            if (collaborators.size() == userList.size()) {
                                                WriteBatch batch = db.batch();
                                                batch.update(db.collection("Notes").document(noteID), "users", userList);
                                                batch.update(db.collection("Notes").document(noteID), "collaboratorList", collaborators);
                                                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "onSuccess: updated collaborators successfully");
                                                    }
                                                });
                                                mNote.setUsers(userList);
                                                mNote.setCollaboratorList(collaborators);

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

                                                if (!finalStillCollaborator) {
                                                    //Current user is not a collaborator anymore, delete it from his home screen
                                                    if (Navigation.findNavController(view).getCurrentDestination().getId() == R.id.editNoteFragment) {
                                                        Navigation.findNavController(view).navigate(EditNoteFragmentDirections
                                                                .actionEditNoteFragmentToHomeFragment(noteID));
                                                    }
//                                                    Navigation.findNavController(view).popBackStack();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
            }
        }

        Log.d(TAG, "getCollaborators: " + collaboratorList.toString());
    }

    @Override
    public void onCollaboratorClick(int position) {
        showCollaboratorsDialog();
    }
}

