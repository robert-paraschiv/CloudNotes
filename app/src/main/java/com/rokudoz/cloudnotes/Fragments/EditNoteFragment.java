package com.rokudoz.cloudnotes.Fragments;

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
import android.widget.ScrollView;
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
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.cloudnotes.Adapters.CheckableItemAdapter;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.BannerAdManager;
import com.rokudoz.cloudnotes.Utils.ColorFunctions;
import com.rokudoz.cloudnotes.Utils.LastEdit;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditNoteFragment extends Fragment implements CheckableItemAdapter.OnStartDragListener, CheckableItemAdapter.OnItemClickListener {
    private static final String TAG = "EditNoteFragment";

    private View view;
    private LinearLayout editLinearLayout;
    boolean edit = false;

    private String noteType = "text";
    private String noteID = "";
    private int position = 0;
    private int number_of_edits = 0;
    private List<CheckableItem> checkableItemList = new ArrayList<>();
    private List<CheckableItem> oldList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ScrollView rv_scrollView;
    private CheckableItemAdapter mAdapter;
    private ItemTouchHelper helper;

    private Note mNote = new Note();
    TextInputEditText titleInput, textInput;
    TextView lastEditTv, numberOfEditsTv;
    MaterialButton backBtn, deleteBtn, checkboxModeBtn, addCheckboxBtn, optionsBtn;
    MaterialCardView bottomCard;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    public EditNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_note, container, false);

        textInput = view.findViewById(R.id.editNoteFragment_textEditText);
        titleInput = view.findViewById(R.id.editNoteFragment_titleEditText);
        backBtn = view.findViewById(R.id.editNoteFragment_backBtn);
        lastEditTv = view.findViewById(R.id.editNoteFragment_lastEditTextView);
        numberOfEditsTv = view.findViewById(R.id.editNoteFragment_numberOfedits);
        deleteBtn = view.findViewById(R.id.editNoteFragment_deleteBtn);
        recyclerView = view.findViewById(R.id.editNoteFragment_checkbox_rv);
        checkboxModeBtn = view.findViewById(R.id.editNoteFragment_CheckBoxModeBtn);
        addCheckboxBtn = view.findViewById(R.id.editNoteFragment_add_checkbox_Btn);
        rv_scrollView = view.findViewById(R.id.editNoteFragment_scroll_rv);
        editLinearLayout = view.findViewById(R.id.editNoteFragment_editLayout);
        optionsBtn = view.findViewById(R.id.editNoteFragment_optionsBtn);
        bottomCard = view.findViewById(R.id.editNoteFragment_bottomCard);

        if (getArguments() != null) {
            EditNoteFragmentArgs editNoteFragmentArgs = EditNoteFragmentArgs.fromBundle(getArguments());
            noteID = editNoteFragmentArgs.getNoteDocID();
            setupBackgroundColor(editNoteFragmentArgs.getNoteColor());
            getNote(noteID);
        }

        //Hide Banner Ad
        if (getActivity() != null) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.hideBannerAd(getActivity());
        }

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

                    rv_scrollView.setVisibility(View.VISIBLE);
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

                    rv_scrollView.setVisibility(View.INVISIBLE);
                    checkboxModeBtn.setIconResource(R.drawable.ic_outline_check_box_24);
                    Log.d(TAG, "onClick: " + checkableItemList.toString());
                    Log.d(TAG, "onClick: " + text);

                    checkboxModeBtn.setEnabled(true);
                }

            }
        });

        buildRecyclerView();


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(requireActivity());
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.editNoteFragment)
                    Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());

            }
        });

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
                        db.collection("Notes").document(noteID)
                                .update("deleted", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Deleted note", Toast.LENGTH_SHORT).show();
                                hideSoftKeyboard(requireActivity());
                                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId()
                                        == R.id.editNoteFragment)
                                    Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());
                                dialog.cancel();
                            }
                        });

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

        return view;
    }

    private void setBackgroundColor(int color) {
        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(color);
        window.setNavigationBarColor(color);

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
        }
    }


    private void buildRecyclerView() {
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
            db.collection("Notes").document(noteID)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null && e == null) {
                                mNote = documentSnapshot.toObject(Note.class);
                                if (mNote != null) {
                                    mNote.setNote_doc_ID(documentSnapshot.getId());
                                    titleInput.setText(mNote.getNoteTitle());
                                    textInput.setText(mNote.getNoteText());

                                    if (mNote.getNumber_of_edits() != null)
                                        number_of_edits = mNote.getNumber_of_edits();

                                    if (mNote.getPosition() != null)
                                        position = mNote.getPosition();
                                    if (mNote.getNoteType() != null) {
                                        noteType = mNote.getNoteType();
                                    } else {
                                        noteType = "text";
                                    }
                                    if (noteType.equals("text")) {
                                        textInput.setVisibility(View.VISIBLE);
                                        checkableItemList.clear();
                                        mAdapter.notifyDataSetChanged();
                                        rv_scrollView.setVisibility(View.GONE);
                                        checkboxModeBtn.setIconResource(R.drawable.ic_outline_check_box_24);
                                    } else if (noteType.equals("checkbox")) {
                                        textInput.setText("");
                                        textInput.setVisibility(View.GONE);
                                        checkableItemList.clear();
                                        mAdapter.notifyDataSetChanged();
                                        if (mNote.getCheckableItemList() != null) {
                                            for (CheckableItem item : mNote.getCheckableItemList()) {
                                                checkableItemList.add(item);
                                                oldList.add(new CheckableItem(item.getText(), item.getChecked()));
                                                mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                                            }

                                            Log.d(TAG, "onEvent: " + oldList.toString());
                                        }
                                        rv_scrollView.setVisibility(View.VISIBLE);
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
                                                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId()
                                                        == R.id.editNoteFragment)
                                                    Navigation.findNavController(view).navigate(EditNoteFragmentDirections
                                                            .actionEditNoteFragmentToNoteEditsFragment(noteID, mNote.getBackgroundColor()));
                                            }
                                        });
                                    }

                                    //Get note color from DB and set it
                                    String color = mNote.getBackgroundColor();
                                    setupBackgroundColor(color);

                                    optionsBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showColorSettings();
                                        }
                                    });

                                }
                            }
                        }
                    });
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

        if (mNote.getBackgroundColor() == null) {
            initial.setBorderWidth(5);
        } else {
            switch (mNote.getBackgroundColor()) {
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
                mNote.setBackgroundColor("yellow");
                yellow.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("red");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_red));
                mNote.setBackgroundColor("red");
                red.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("blue");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_blue));
                mNote.setBackgroundColor("blue");
                blue.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("green");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_green));
                mNote.setBackgroundColor("green");
                green.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("orange");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_orange));
                mNote.setBackgroundColor("orange");
                orange.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor("purple");
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_purple));
                mNote.setBackgroundColor("purple");
                purple.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNoteColor(null);
                resetBackgroundColors();
                mNote.setBackgroundColor(null);
                initial.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });

        collaboratorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();
                //TODO FULL SCREEN DIALOG HERE
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

    private void updateNoteColor(final String noteColor) {
        db.collection("Notes").document(noteID)
                .update("backgroundColor", noteColor).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: updated note color + " + noteColor);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        for (CheckableItem checkableItem : checkableItemList) {
            Log.d(TAG, "onStop: " + checkableItem.toString());
        }
        if (mNote != null) {

            for (CheckableItem item : checkableItemList) {
                if (!oldList.contains(item)) {
                    edit = true;
                } else {
                    if (oldList.get(oldList.indexOf(item)).getChecked() != item.getChecked()) {
                        edit = true;
                    }
                }
            }
            for (CheckableItem item : oldList) {
                if (!checkableItemList.contains(item)) {
                    edit = true;
                    break;
                }
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
                    note = new Note(position,
                            title,
                            Objects.requireNonNull(textInput.getText()).toString(),
                            null,
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                            null, true, noteType, null, "Edited", number_of_edits + 1,
                            false, mNote.getBackgroundColor(), mNote.getUsers());
                } else if (noteType.equals("checkbox")) {
                    note = new Note(position,
                            title,
                            "",
                            null,
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                            null, true, noteType, checkableItemList, "Edited", number_of_edits + 1,
                            false, mNote.getBackgroundColor(), mNote.getUsers());
                }

                WriteBatch batch = db.batch();
                batch.set(db.collection("Notes").document(noteID), note);
                batch.set(db.collection("Notes").document(noteID).collection("Edits").document(), note);

                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Updated note successfully");
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
}