package com.rokudoz.cloudnotes.Fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.LastEdit;

import java.util.Date;
import java.util.Objects;

public class EditNoteFragment extends Fragment {
    private static final String TAG = "EditNoteFragment";

    private String noteID = "";
    private View view;

    private Note mNote = new Note();
    TextInputEditText titleInput, textInput;
    TextView lastEditTv;
    MaterialButton backBtn;

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

        if (getArguments() != null) {
            EditNoteFragmentArgs editNoteFragmentArgs = EditNoteFragmentArgs.fromBundle(getArguments());
            noteID = editNoteFragmentArgs.getNoteDocID();
            getNote(noteID);
        }

        lastEditTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToNoteEditsFragment(noteID));
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNote.getNoteText().equals(textInput.getText().toString()) || !mNote.getNoteTitle().equals(titleInput.getText().toString())) {
                    Note note = new Note(Objects.requireNonNull(titleInput.getText()).toString(),
                            Objects.requireNonNull(textInput.getText()).toString(),
                            null,
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                            null);

                    WriteBatch batch = db.batch();
                    batch.set(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID), note);
                    batch.set(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
                            .collection("Edits").document(), note);

                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Updated note successfully");
                            Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());
                        }
                    });
                } else {
                    Log.d(TAG, "Note was the same, going back");
                    Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());
                }

            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!mNote.getNoteText().equals(textInput.getText().toString()) || !mNote.getNoteTitle().equals(titleInput.getText().toString())) {
                    Note note = new Note(Objects.requireNonNull(titleInput.getText()).toString(),
                            Objects.requireNonNull(textInput.getText()).toString(),
                            null,
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                            null);

                    WriteBatch batch = db.batch();
                    batch.set(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID), note);
                    batch.set(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
                            .collection("Edits").document(), note);

                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: Updated note successfully");
                            Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());
                        }
                    });
                } else {
                    Log.d(TAG, "Note was the same, going back");
                    Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());
                }

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        return view;
    }

    private void getNote(String noteID) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null && e == null) {
                                mNote = documentSnapshot.toObject(Note.class);
                                if (mNote != null) {
                                    mNote.setNote_doc_ID(documentSnapshot.getId());
                                    titleInput.setText(mNote.getNoteTitle());
                                    textInput.setText(mNote.getNoteText());

                                    if (mNote.getCreation_date() != null) {
                                        Date date = mNote.getCreation_date();
                                        LastEdit lastEdit = new LastEdit();
                                        lastEditTv.setText(lastEdit.getLastEdit(date.getTime()));
                                    }

                                }
                            }
                        }
                    });
    }
}