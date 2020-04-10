package com.rokudoz.cloudnotes.Fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.Objects;

public class NewNoteFragment extends Fragment {
    private static final String TAG = "NewNoteFragment";

    private View view;
    private TextInputEditText textInputEditText, titleInputEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private DocumentReference noteRef;
    private Note mNote;

    public NewNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_note, container, false);

        textInputEditText = view.findViewById(R.id.newNoteFragment_textInput);
        titleInputEditText = view.findViewById(R.id.newNoteFragment_title_textInput);

        MaterialButton backBtn = view.findViewById(R.id.newNoteFragment_backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(getActivity());
                Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        if (!textInputEditText.getText().toString().trim().equals("")) {
            final Note note = new Note(Objects.requireNonNull(titleInputEditText.getText()).toString(),
                    Objects.requireNonNull(textInputEditText.getText()).toString(),
                    null,
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                    null, false);

            if (noteRef == null) {
                usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").add(note)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                noteRef = documentReference;
                                mNote = note;
                                documentReference.collection("Edits").add(note).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                    noteRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            noteRef.collection("Edits").add(note).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "onSuccess: added note");
                                    mNote = note;
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
}