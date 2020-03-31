package com.rokudoz.cloudnotes.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

public class EditNoteFragment extends Fragment {
    private static final String TAG = "EditNoteFragment";

    private String noteID = "";
    private View view;

    TextInputEditText tvTitle, tvText;
    MaterialButton saveBtn;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    public EditNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_note, container, false);

        tvText = view.findViewById(R.id.editNoteFragment_textEditText);
        tvTitle = view.findViewById(R.id.editNoteFragment_titleEditText);
        saveBtn = view.findViewById(R.id.editNoteFragment_saveBtn);

        if (getArguments() != null) {
            EditNoteFragmentArgs editNoteFragmentArgs = EditNoteFragmentArgs.fromBundle(getArguments());
            noteID = editNoteFragmentArgs.getNoteDocID();
            getNote(noteID);
        }

        return view;
    }

    private void getNote(String noteID) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null) {
                                Note note = documentSnapshot.toObject(Note.class);
                                if (note != null) {
                                    note.setNote_doc_ID(documentSnapshot.getId());
                                    tvTitle.setText(note.getNoteTitle());
                                    tvText.setText(note.getNoteText());
                                }
                            }

                        }
                    });
    }
}