package com.rokudoz.cloudnotes.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.cloudnotes.Adapters.CheckableItemAdapter;
import com.rokudoz.cloudnotes.Dialogs.FullBottomSheetDialogFragment;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.Models.User;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.BannerAdManager;
import com.rokudoz.cloudnotes.Utils.ColorFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewNoteFragment extends Fragment implements CheckableItemAdapter.OnStartDragListener,
        CheckableItemAdapter.OnItemClickListener, FullBottomSheetDialogFragment.ExampleDialogListener {

    private static final String TAG = "NewNoteFragment";

    ItemTouchHelper helper;
    private String noteType = "text";
    private View view;
    private TextInputEditText textInputEditText, titleInputEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private DocumentReference noteRef;
    private Note mNote = new Note();

    private List<CheckableItem> checkableItemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ScrollView rv_scrollview;
    private CheckableItemAdapter mAdapter;
    private MaterialButton checkboxModeBtn;
    private MaterialButton addCheckboxBtn;
    MaterialCardView bottomCard;
    private boolean discard = false;

    public NewNoteFragment() {
        // Required empty public constructor
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
        rv_scrollview = view.findViewById(R.id.newNoteFragment_scroll_rv);
        MaterialButton settingsBtn = view.findViewById(R.id.newNoteFragment_settingsBtn);
        MaterialButton discardBtn = view.findViewById(R.id.newNoteFragment_discardBtn);

        //Reset status bar color
//        if (getActivity() != null) {
//            ColorFunctions colorFunctions = new ColorFunctions();
//            colorFunctions.resetStatus_NavigationBar_Colors(getActivity());
//        }

        //Hide Banner Ad
        if (getActivity() != null) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.hideBannerAd(getActivity());
        }

        MaterialButton backBtn = view.findViewById(R.id.newNoteFragment_backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    hideSoftKeyboard(getActivity());
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.newNoteFragment)
                    Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());
            }
        });


        titleInputEditText.setFocusableInTouchMode(true);
        titleInputEditText.post(new Runnable() {
            public void run() {
                titleInputEditText.requestFocusFromTouch();
                InputMethodManager lManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(titleInputEditText, 0);
            }
        });


        checkboxModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteType.equals("text")) {
                    checkboxModeBtn.setEnabled(false);

                    checkableItemList.clear();
                    mAdapter.notifyDataSetChanged();

                    noteType = "checkbox";
                    List<String> textList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(textInputEditText.getText()).toString().split("\n")));
                    textInputEditText.setText("");
                    textInputEditText.setVisibility(View.GONE);

                    for (int i = 0; i < textList.size(); i++) {
                        CheckableItem checkableItem = new CheckableItem(textList.get(i), false);
                        checkableItem.setShouldBeFocused(true);
                        checkableItemList.add(checkableItem);
                        mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                    }
                    if (checkableItemList.size() == 0) {
                        CheckableItem checkableItem = new CheckableItem("", false);
                        checkableItem.setShouldBeFocused(true);
                        checkableItemList.add(checkableItem);
                        mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                    }

                    rv_scrollview.setVisibility(View.VISIBLE);
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
                    textInputEditText.setText(text);
                    textInputEditText.setVisibility(View.VISIBLE);

                    checkableItemList.clear();
                    mAdapter.notifyDataSetChanged();

                    rv_scrollview.setVisibility(View.INVISIBLE);
                    checkboxModeBtn.setIconResource(R.drawable.ic_outline_check_box_24);
                    Log.d(TAG, "onClick: " + checkableItemList.toString());
                    Log.d(TAG, "onClick: " + text);

                    checkboxModeBtn.setEnabled(true);
                }

            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorSettings();
            }
        });

        discardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    hideSoftKeyboard(getActivity());
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.newNoteFragment)
                    Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());

                //TODO add code
                discard = true;
            }
        });

        buildRecyclerView();
        return view;
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

            Note note = new Note();
            String title;
            if (Objects.requireNonNull(titleInputEditText.getText()).toString().trim().equals("")) {
                title = "";
            } else {
                title = titleInputEditText.getText().toString();
            }

            List<String> userList = new ArrayList<>();
            userList.add(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

            if (noteType.equals("text")) {
                note = new Note(0, title,
                        Objects.requireNonNull(textInputEditText.getText()).toString(),
                        null,
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                        null, false, noteType, null, "Created", 0,
                        false, mNote.getBackgroundColor(), userList);
            } else if (noteType.equals("checkbox")) {
                note = new Note(0, title,
                        "",
                        null,
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                        null, false, noteType, checkableItemList, "Created", 0,
                        false, mNote.getBackgroundColor(), userList);
            }

            Log.d(TAG, "onStop: note ref " + noteRef);

            if (noteRef == null) {
                final Note finalNote = note;
                db.collection("Notes").add(note)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                noteRef = documentReference;
                                mNote = finalNote;
                                documentReference.collection("Edits").add(finalNote).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "onSuccess: added note");
                                    }
                                });
                            }
                        });
            } else {
                note.setEdited(true);
                if (!mNote.getNoteText().equals(textInputEditText.getText().toString()) || !mNote.getNoteTitle().equals(titleInputEditText.getText().toString())) {
                    note.setEdit_type("Edited");
                    note.setNumber_of_edits(mNote.getNumber_of_edits() + 1);

                    WriteBatch batch = db.batch();
                    batch.set(noteRef, note);
                    batch.set(noteRef.collection("Edits").document(), note);

                    final Note finalNote1 = note;
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: updated note");
                            mNote = finalNote1;
                        }
                    });
                }
            }

        } else {
            Log.d(TAG, "onStop: empty note discarded");
        }
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
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_yellow));
                mNote.setBackgroundColor("yellow");
                yellow.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_red));
                mNote.setBackgroundColor("red");
                red.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_blue));
                mNote.setBackgroundColor("blue");
                blue.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_green));
                mNote.setBackgroundColor("green");
                green.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_orange));
                mNote.setBackgroundColor("orange");
                orange.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.note_background_color_purple));
                mNote.setBackgroundColor("purple");
                purple.setBorderWidth(5);
                bottomSheetDialog.cancel();
            }
        });
        initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                FullBottomSheetDialogFragment fullBottomSheetDialogFragment = new FullBottomSheetDialogFragment("NEW NOTE FRAGMENT");
                fullBottomSheetDialogFragment.setTargetFragment(NewNoteFragment.this,1);
                fullBottomSheetDialogFragment.show(getParentFragmentManager(), "");
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
    public void applyTexts(String text) {
        //TODO implement
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }
}