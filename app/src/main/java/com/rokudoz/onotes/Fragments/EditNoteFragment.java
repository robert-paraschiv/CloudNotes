package com.rokudoz.onotes.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;
import androidx.transition.TransitionManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;
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
import com.rokudoz.onotes.Utils.LastEdit;
import com.rokudoz.onotes.Utils.NotesUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.rokudoz.onotes.Utils.NotesUtils.NOTES_DETAILS;
import static com.rokudoz.onotes.Utils.NotesUtils.NOTE_TYPE_TEXT;
import static com.rokudoz.onotes.Utils.NotesUtils.getCheckboxNoteChanges;
import static com.rokudoz.onotes.Utils.NotesUtils.getTextNoteChanges;

public class EditNoteFragment extends Fragment implements CheckableItemAdapter.OnStartDragListener,
        CheckableItemAdapter.OnItemClickListener, FullBottomSheetDialogFragment.ExampleDialogListener, CollaboratorNotesAdapter.OnItemClickListener {

    private static final String TAG = "EditNoteFragment";

    int note_background_color;
    String note_background_colorName = "";

    boolean showScrollFab = false;

    private boolean retrievedNote = false;

    private ProgressBar progressBar;

    private View view;
    private LinearLayout editLinearLayout;
    boolean edit = false;

    private String noteType = "text";
    private String noteID = "";
    private int notePosition;
    private int number_of_edits = 0;
    private final List<CheckableItem> checkableItemList = new ArrayList<>();
    private RecyclerView recyclerView, collaboratorsRV;
    private RelativeLayout rv_checkbox_Layout;
    private CheckableItemAdapter mAdapter;
    private CollaboratorNotesAdapter collaboratorNotesAdapter;
    private ItemTouchHelper helper;

    RelativeLayout rootLayout;

    private final List<Collaborator> mCollaboratorsList = new ArrayList<>();
    private final Collaborator currentUserCollaborator = new Collaborator();

    private Note mNote = new Note();
    TextInputEditText titleInput, textInput;
    TextView lastEditTv, numberOfEditsTv;
    MaterialButton backBtn, deleteBtn, checkboxModeBtn, addCheckboxBtn, optionsBtn;
    MaterialCardView bottomCard;

    FloatingActionButton scrollFab;
    NestedScrollView nestedScrollView;

    private ListenerRegistration noteListener;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("Users");

    public EditNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_home_edit_duration));
//        materialContainerTransform.setStartDelay(25);
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
        view = inflater.inflate(R.layout.fragment_edit_note, container, false);
        currentUserCollaborator.setUser_email(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

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
        scrollFab = view.findViewById(R.id.editNoteFragment_scroll_fab);
        nestedScrollView = view.findViewById(R.id.editNoteFragment_nestedScrollView);
        note_background_color = ContextCompat.getColor(requireContext(), R.color.fragments_background);


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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            EditNoteFragmentArgs editNoteFragmentArgs = EditNoteFragmentArgs.fromBundle(getArguments());
            noteID = editNoteFragmentArgs.getNoteDocID();
            notePosition = editNoteFragmentArgs.getPosition();

            //Shared element transition
            titleInput.setTransitionName("note_home_title" + editNoteFragmentArgs.getNoteDocID());
            textInput.setTransitionName("note_home_text" + editNoteFragmentArgs.getNoteDocID());
            collaboratorsRV.setTransitionName("note_home_collaborators" + editNoteFragmentArgs.getNoteDocID());
            recyclerView.setTransitionName("note_home_checkbox" + editNoteFragmentArgs.getNoteDocID());
            rootLayout.setTransitionName("note_home_rootLayout" + editNoteFragmentArgs.getNoteDocID());

            Log.d(TAG, "onCreateView: note_home_title " + titleInput.getTransitionName());
            mNote.setNote_background_color(editNoteFragmentArgs.getNoteColor());
            note_background_colorName = editNoteFragmentArgs.getNoteColor();
            setupBackgroundColor(editNoteFragmentArgs.getNoteColor());
            getNote(noteID);
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
                        String uid = "" + System.currentTimeMillis() + checkableItemList.size();
                        checkableItemList.add(new CheckableItem(textList.get(i), false, uid));
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
        note_background_color = color;
        bottomCard.setBackgroundColor(color);
        view.setBackgroundColor(color);

        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

//
        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);
    }

    private void resetBackgroundColors() {
        if (getActivity() != null) {

            MaterialCardView cardView = new MaterialCardView(requireContext());
            bottomCard.setBackgroundColor(cardView.getCardBackgroundColor().getDefaultColor());
            view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_default));
            note_background_color = ContextCompat.getColor(requireContext(), R.color.note_background_color_default);

            Window window = requireActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(cardView.getCardBackgroundColor().getDefaultColor());
            window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_default));
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
        String uid = "" + System.currentTimeMillis() + checkableItemList.size();
        checkableItemList.add(new CheckableItem("", false, uid));
        mAdapter.notifyDataSetChanged();

        addCheckboxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = "" + System.currentTimeMillis() + checkableItemList.size();
                CheckableItem checkableItem = new CheckableItem("", false, uid);
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
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void getNote(final String noteID) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            noteListener = db.collection("Notes").document(noteID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                            mNote.setNote_background_color(note_background_colorName);
                            //Scroll edit text to bottom
                            if (textInput.canScrollVertically(1) && mNote.getNoteType().equals(NOTE_TYPE_TEXT) || showScrollFab) {
                                Log.d(TAG, "onEvent: VISIBLE FAB");
                                scrollFab.setVisibility(View.VISIBLE);
                                showScrollFab = true;
                                scrollFab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (textInput.getText() != null) {
                                            textInput.requestFocus();
                                            textInput.setSelection(textInput.getText().length());
                                            scrollFab.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            } else if (!textInput.canScrollVertically(1)) {
                                Log.d(TAG, "onEvent: FAB NOT VISIBLE");
                            }

                            if (mNote.getNumber_of_edits() != null)
                                number_of_edits = mNote.getNumber_of_edits();

                            if (mNote.getNoteType() != null) {
                                noteType = mNote.getNoteType();
                            } else {
                                noteType = NOTE_TYPE_TEXT;
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
                                    if (item.getUid() == null)
                                        item.setUid(System.currentTimeMillis() + "" + checkableItemList.size());
                                    checkableItemList.add(new CheckableItem(item.getText(), item.getChecked(), item.getUid()));
                                    mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                                }
                                rv_checkbox_Layout.setVisibility(View.VISIBLE);
                                checkboxModeBtn.setIconResource(R.drawable.ic_outline_text_fields_24);
                            }
                            checkboxModeBtn.setVisibility(View.VISIBLE);

                            //Setup last edit text
                            if (mNote.getCreation_date() != null && mNote.getEdited() != null) {
                                Date date = mNote.getCreation_date();

                                if (mNote.getEdited()) {
                                    lastEditTv.setText(MessageFormat.format("Last edit {0}", LastEdit.getLastEdit(date.getTime())));
                                    if (mNote.getNumber_of_edits() != null) {
                                        if (mNote.getNumber_of_edits() == 1) {
                                            numberOfEditsTv.setText(MessageFormat.format("{0} Edit", mNote.getNumber_of_edits()));
                                        } else if (mNote.getNumber_of_edits() > 1) {
                                            numberOfEditsTv.setText(MessageFormat.format("{0} Edits", mNote.getNumber_of_edits()));
                                        }
                                    }
                                } else {
                                    lastEditTv.setText(MessageFormat.format("Created {0}", LastEdit.getLastEdit(date.getTime())));
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
                                                            notePosition,
                                                            mNote.getNote_background_color(),
                                                            hasCollaborators));
                                    }
                                });
                            }

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

//                                if (mCollaboratorsList.size() > 0) {
//                                    mCollaboratorsList.clear();
//                                    collaboratorNotesAdapter.notifyDataSetChanged();
//                                }
//                                for (Collaborator collaborator : mNote.getCollaboratorList()) {
//                                    mCollaboratorsList.add(collaborator);
//                                    collaboratorNotesAdapter.notifyItemInserted(mCollaboratorsList.indexOf(collaborator));
//                                }
                                mCollaboratorsList.clear();
                                collaboratorNotesAdapter.notifyDataSetChanged();

                                mCollaboratorsList.addAll(mNote.getCollaboratorList());
                                collaboratorNotesAdapter.notifyDataSetChanged();

                            } else {
                                collaboratorsRV.setVisibility(View.GONE);

                            }
                            //Start enter animation after info retrieved
//                            startPostponedEnterTransition();
//                            view.setLayerType(View.LAYER_TYPE_NONE, null);
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
            boolean newNoteIsDifferent = NotesUtils.checkIfNotesAreDifferent(note, mNote);

            if (newNoteIsDifferent && getActivity() != null) {
                Log.d(TAG, "checkNoteEvent: Someone has just edited this note");
                showReloadDataDialog();
            } else if (!newNoteIsDifferent) {
                Log.d(TAG, "checkNoteEvent: note not different");
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
        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, null);
        final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
        MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
        TextView textView = dialogView.findViewById(R.id.dialog_ShowAd_title);
        textView.setText(R.string.someone_edited_the_note_meanwhile);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                retrievedNote = false;
                if (mNote.getCheckableItemList() != null)
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
                case "":
                    resetBackgroundColors();
            }
        } else {
            resetBackgroundColors();
        }
        Log.d(TAG, "setupBackgroundColor: " + color);
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

        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Color_on_click("yellow", R.color.note_background_color_yellow, bottomSheetDialog);
                yellow.setBorderWidth(5);
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Color_on_click("red", R.color.note_background_color_red, bottomSheetDialog);
                red.setBorderWidth(5);
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Color_on_click("blue", R.color.note_background_color_blue, bottomSheetDialog);
                blue.setBorderWidth(5);
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Color_on_click("green", R.color.note_background_color_green, bottomSheetDialog);
                green.setBorderWidth(5);
            }
        });
        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Color_on_click("orange", R.color.note_background_color_orange, bottomSheetDialog);
                orange.setBorderWidth(5);
            }
        });
        purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Color_on_click("purple", R.color.note_background_color_purple, bottomSheetDialog);
                purple.setBorderWidth(5);
            }
        });
        initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("");
                resetBackgroundColors();
                bottomSheetDialog.hide();
                mNote.setNote_background_color("");
                initial.setBorderWidth(5);
            }
        });
        collaboratorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.hide();
                showCollaboratorsDialog();
            }
        });

        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        //Allows bottom sheet dialog to be displayed on top of nav bar and color it accordingly
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.findViewById(com.google.android.material.R.id.container).setFitsSystemWindows(false);
            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }
    }

    private void Color_on_click(String color, int colorRes, BottomSheetDialog bottomSheetDialog) {
        updateNoteColor(color);
        setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes));
        mNote.setNote_background_color(color);
        bottomSheetDialog.hide();
    }

    private void showCollaboratorsDialog() {
        boolean isOwner = Objects.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), mNote.getCreator_user_email());

        List<Collaborator> collaboratorList = new ArrayList<>();

        //if note has no collaborators yet, add the current user
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (mNote.getCollaboratorList() == null || mNote.getCollaboratorList().size() == 0) {
                collaboratorList.add(new Collaborator(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName(),
                        firebaseUser.getPhotoUrl().toString(), true));
                mNote.setCollaboratorList(collaboratorList);
            } else {
                collaboratorList.addAll(mNote.getCollaboratorList());
            }
        }
        FullBottomSheetDialogFragment fullBottomSheetDialogFragment =
                new FullBottomSheetDialogFragment(note_background_color, collaboratorList, isOwner);
//        fullBottomSheetDialogFragment.setCancelable(false);
        fullBottomSheetDialogFragment.setTargetFragment(EditNoteFragment.this, 2);
        fullBottomSheetDialogFragment.show(getParentFragmentManager(), "");
    }

    private void updateNoteColor(final String noteColor) {
        mNote.setNote_background_color(noteColor);
        note_background_colorName = noteColor;
        db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection(NotesUtils.NOTES_DETAILS)
                .document(mNote.getNote_doc_ID()).update("note_background_color", noteColor).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Updated note color " + noteColor);
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
        if (mNote.getCheckableItemList() != null)
            Log.d(TAG, "onStop: " + mNote.getCheckableItemList().toString());

        if (mNote != null) {

            if (noteType.equals("checkbox") && mNote.getCheckableItemList() != null) {
                //Compare notes checkbox lists
                edit = NotesUtils.compareCheckableItemLists(mNote.getCheckableItemList(), checkableItemList);
            }
            List<CheckableItem> currentCheckboxList = new ArrayList<>();
            for (CheckableItem item : checkableItemList) {
                if (!item.getText().trim().equals(""))
                    currentCheckboxList.add(new CheckableItem(item.getText(), item.getChecked(), item.getUid()));
            }
            Log.d(TAG, "onStop: EDIT " + edit);

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
                    //Compare current note text to old text and get changes
                    List<NoteChange> noteChangeList = getTextNoteChanges(textInput.getText().toString(), mNote.getNoteText(), mNote.getNoteType());

                    //Get note ready for updating db
                    note = new Note(title,
                            Objects.requireNonNull(textInput.getText()).toString(),
                            mNote.getCreator_user_email(),
                            null, true, noteType, null, "Edited", number_of_edits + 1,
                            false, mNote.getUsers(), mNote.getCollaboratorList(),
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                            noteChangeList);

                } else if (noteType.equals("checkbox")) {
                    //Compare new checkbox values to old ones and get changes
                    List<NoteChange> noteChangeList = getCheckboxNoteChanges(currentCheckboxList, mNote.getCheckableItemList());

                    //Get note ready for updating db
                    note = new Note(
                            title,
                            "",
                            mNote.getCreator_user_email(),
                            null, true, noteType, currentCheckboxList, "Edited", number_of_edits + 1,
                            false, mNote.getUsers(), mNote.getCollaboratorList(),
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                            noteChangeList);
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
        String uid = "" + System.currentTimeMillis() + checkableItemList.size();
        CheckableItem checkableItem = new CheckableItem("", false, uid);
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

        final WriteBatch batch = db.batch();
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
                                                collaborators.add(0, new Collaborator(user.getUser_id(), user.getEmail(), user.getUser_name(),
                                                        user.getUser_profile_picture(), collaborator.getCreator()));
                                            } else {
                                                collaborators.add(new Collaborator(user.getUser_id(), user.getEmail(), user.getUser_name(),
                                                        user.getUser_profile_picture(), collaborator.getCreator()));
                                            }

                                            if (collaborators.size() == userList.size()) {
                                                GetCollaboratorsToUpdate(collaborators, batch, userList, finalStillCollaborator);
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

    private void GetCollaboratorsToUpdate(final List<Collaborator> collaborators, final WriteBatch batch, final List<String> userList,
                                          final boolean finalStillCollaborator) {
        final List<Collaborator> collaboratorsToDelete = new ArrayList<>();
        for (Collaborator collaboratorToDelete : mCollaboratorsList) {
            if (!collaborators.contains(collaboratorToDelete)) {
                collaboratorsToDelete.add(collaboratorToDelete);
            }
        }
        final List<Collaborator> collaboratorsToUpdate = new ArrayList<>();
        collaboratorsToUpdate.addAll(collaborators);
        collaboratorsToUpdate.addAll(collaboratorsToDelete);

        for (final Collaborator collaboratorToUpdate : collaboratorsToUpdate) {
            usersRef.whereEqualTo("email", collaboratorToUpdate.getUser_email()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                final User userToUpdate = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                if (userToUpdate != null) {
                                    usersRef.document(userToUpdate.getUser_id()).collection(NOTES_DETAILS)
                                            .document(mNote.getNote_doc_ID()).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot != null) {
                                                        NoteDetails notedetailslocal = documentSnapshot.toObject(NoteDetails.class);

                                                        if (notedetailslocal == null) {
                                                            Log.d(TAG, "onFailure: NOTE DETAILS got but null " + userToUpdate.getEmail());

                                                            if (!collaboratorsToDelete.contains(collaboratorToUpdate)) {
                                                                NoteDetails noteDetails = new NoteDetails(mNote.getNote_doc_ID(), userToUpdate.getUser_id(), 0, "");
                                                                batch.set(db.collection("Users").document(userToUpdate.getUser_id()).collection(NOTES_DETAILS).document(mNote.getNote_doc_ID()), noteDetails);

                                                            }
                                                        } else {
                                                            Log.d(TAG, "onSuccess: NOT NULL " + notedetailslocal.toString());

                                                            if (collaboratorsToDelete.contains(collaboratorToUpdate)) {
                                                                batch.delete(usersRef.document(userToUpdate.getUser_id()).collection(NOTES_DETAILS)
                                                                        .document(mNote.getNote_doc_ID()));
                                                            }
                                                        }
                                                    } else {
                                                        Log.d(TAG, "onFailure: Note details doc snapshot was NULL " + userToUpdate.getEmail());

                                                        if (!collaboratorsToDelete.contains(collaboratorToUpdate)) {
                                                            NoteDetails noteDetails = new NoteDetails(mNote.getNote_doc_ID(), userToUpdate.getUser_id(), 0, "");
                                                            batch.set(db.collection("Users").document(userToUpdate.getUser_id()).collection(NOTES_DETAILS).document(mNote.getNote_doc_ID()), noteDetails);
                                                        }
                                                    }
                                                    collaboratorsToUpdate.remove(collaboratorToUpdate);
                                                    if (collaboratorsToUpdate.size() < 1)
                                                        UpdateCollaboratorsOfNote(batch, userList, collaborators, finalStillCollaborator);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e);
                                        }
                                    });
                                }
                            }

                        }
                    });
        }
    }

    private void UpdateCollaboratorsOfNote(WriteBatch batch, List<String> userList, List<Collaborator> collaborators, boolean finalStillCollaborator) {
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
            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.editNoteFragment) {
                Navigation.findNavController(view).navigate(EditNoteFragmentDirections
                        .actionEditNoteFragmentToHomeFragment(noteID));
            }
        }
    }

    @Override
    public void onCollaboratorClick(int position) {
        showCollaboratorsDialog();
    }
}