package com.rokudoz.cloudnotes.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
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
    MaterialButton backBtn, deleteBtn;

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
        deleteBtn = view.findViewById(R.id.editNoteFragment_deleteBtn);

        if (getArguments() != null) {
            EditNoteFragmentArgs editNoteFragmentArgs = EditNoteFragmentArgs.fromBundle(getArguments());
            noteID = editNoteFragmentArgs.getNoteDocID();
            getNote(noteID);
        }


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(getActivity());
                Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getActivity(),
                        R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered);
                materialAlertDialogBuilder.setMessage("Are you sure you want to delete this note?");
                materialAlertDialogBuilder.setCancelable(true);
                materialAlertDialogBuilder.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                //Delete note
                                final WriteBatch batch = db.batch();
                                usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
                                        .collection("Edits").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                batch.delete(documentSnapshot.getReference());
                                            }
                                            batch.delete(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .collection("Notes").document(noteID));
                                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getContext(), "Deleted note", Toast.LENGTH_SHORT).show();
                                                    hideSoftKeyboard(getActivity());
                                                    Navigation.findNavController(view).navigate(EditNoteFragmentDirections.actionEditNoteFragmentToHomeFragment());
                                                    dialog.cancel();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });

                materialAlertDialogBuilder.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                materialAlertDialogBuilder.show();


            }
        });

        return view;
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

                                    if (mNote.getCreation_date() != null && mNote.getEdited() != null && mNote.getEdited()) {
                                        Date date = mNote.getCreation_date();
                                        LastEdit lastEdit = new LastEdit();
                                        lastEditTv.setText(lastEdit.getLastEdit(date.getTime()));
                                        lastEditTv.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Navigation.findNavController(view).navigate(EditNoteFragmentDirections
                                                        .actionEditNoteFragmentToNoteEditsFragment(noteID));
                                            }
                                        });
                                    }

                                }
                            }
                        }
                    });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        if (mNote != null) {
            if (!mNote.getNoteText().equals(textInput.getText().toString()) || !mNote.getNoteTitle().equals(titleInput.getText().toString())) {
                Note note = new Note(Objects.requireNonNull(titleInput.getText()).toString(),
                        Objects.requireNonNull(textInput.getText()).toString(),
                        null,
                        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                        null, true);

                WriteBatch batch = db.batch();
                batch.set(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID), note);
                batch.set(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
                        .collection("Edits").document(), note);

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
}