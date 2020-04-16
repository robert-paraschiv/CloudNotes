package com.rokudoz.cloudnotes.Fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rokudoz.cloudnotes.Adapters.CheckableItemAdapter;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NewNoteFragment extends Fragment implements CheckableItemAdapter.OnStartDragListener, CheckableItemAdapter.OnItemClickListener {
    private static final String TAG = "NewNoteFragment";

    ItemTouchHelper helper;
    private String noteType = "text";
    private View view;
    private TextInputEditText textInputEditText, titleInputEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private DocumentReference noteRef;
    private Note mNote;

    private List<CheckableItem> checkableItemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ScrollView rv_scrollview;
    private CheckableItemAdapter mAdapter;
    private MaterialButton checkboxModeBtn, addCheckboxBtn;

    public NewNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_note, container, false);

        textInputEditText = view.findViewById(R.id.newNoteFragment_textInput);
        titleInputEditText = view.findViewById(R.id.newNoteFragment_title_textInput);
        checkboxModeBtn = view.findViewById(R.id.newNoteFragment_CheckBoxModeBtn);
        recyclerView = view.findViewById(R.id.newNoteFragment_checkbox_rv);
        addCheckboxBtn = view.findViewById(R.id.newNoteFragment_add_checkbox_Btn);
        rv_scrollview = view.findViewById(R.id.newNoteFragment_scroll_rv);

        MaterialButton backBtn = view.findViewById(R.id.newNoteFragment_backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(getActivity());
                Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());
            }
        });

        titleInputEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                titleInputEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(titleInputEditText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });
            }
        });
        titleInputEditText.requestFocus();

        checkboxModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteType.equals("text")) {
                    checkboxModeBtn.setEnabled(false);

                    checkableItemList.clear();
                    mAdapter.notifyDataSetChanged();

                    noteType = "checkbox";
                    List<String> textList = new ArrayList<String>(Arrays.asList(Objects.requireNonNull(textInputEditText.getText()).toString().split("\n")));
                    textInputEditText.setText("");
                    textInputEditText.setVisibility(View.GONE);

                    for (int i = 0; i < textList.size(); i++) {
                        checkableItemList.add(new CheckableItem(textList.get(i), false));
                        mAdapter.notifyItemInserted(checkableItemList.size() - 1);
                    }
                    if (checkableItemList.size() == 0) {
                        checkableItemList.add(new CheckableItem("", false));
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

                    rv_scrollview.setVisibility(View.GONE);
                    checkboxModeBtn.setIconResource(R.drawable.ic_outline_check_box_24);
                    Log.d(TAG, "onClick: " + checkableItemList.toString());
                    Log.d(TAG, "onClick: " + text);

                    checkboxModeBtn.setEnabled(true);
                }

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
                checkableItemList.add(new CheckableItem("", false));
                mAdapter.notifyItemInserted(checkableItemList.size());
            }
        });

        helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();

                Collections.swap(checkableItemList, position_dragged, position_target);
                mAdapter.notifyItemMoved(position_dragged, position_target);

                return false;
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
        for (CheckableItem checkableItem : checkableItemList) {
            Log.d(TAG, "onStop: " + checkableItem.toString());
        }
        boolean empty = true;
        for (CheckableItem item : checkableItemList) {
            if (!item.getText().trim().equals(""))
                empty = false;
        }
        if (!textInputEditText.getText().toString().trim().equals("") || !empty) {
            Note note = new Note();
            String title = "";
            if (titleInputEditText.getText().toString().trim().equals("")) {
                title = "";
            } else {
                title = titleInputEditText.getText().toString();
            }
            if (noteType.equals("text")) {
                note = new Note(title,
                        Objects.requireNonNull(textInputEditText.getText()).toString(),
                        null,
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                        null, false, noteType, null);
            } else if (noteType.equals("checkbox")) {
                note = new Note(title,
                        "",
                        null,
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                        null, false, noteType, checkableItemList);
            }


            if (noteRef == null) {
                final Note finalNote = note;
                usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").add(note)
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
                    final Note finalNote1 = note;
                    noteRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            noteRef.collection("Edits").add(finalNote1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "onSuccess: added note");
                                    mNote = finalNote1;
                                }
                            });
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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        helper.startDrag(viewHolder);
    }

    @Override
    public void onCheckClick(int position, boolean isChecked) {
        checkableItemList.get(position).setChecked(isChecked);
        mAdapter.notifyItemChanged(position);
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