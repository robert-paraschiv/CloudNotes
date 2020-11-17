package com.rokudoz.onotes.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.Models.NoteDetails;
import com.rokudoz.onotes.Models.User;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.NotesUtils;

public class TestFragment extends Fragment {
    private static final String TAG = "TestFragment";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("Users");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_test, container, false);

    }

    @Override
    public void onStart() {
        super.onStart();

        db.collection("Notes").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot != null) {
                            final Note note = documentSnapshot.toObject(Note.class);
                            if (note != null) {
                                note.setNote_doc_ID(documentSnapshot.getId());

                                for (String email : note.getUsers()) {
                                    usersRef.whereEqualTo("email", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                                final User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                                if (user != null) {
                                                    NoteDetails noteDetails = new NoteDetails(note.getNote_doc_ID(), user.getUser_id(), 0, "");
                                                    usersRef.document(user.getUser_id()).collection(NotesUtils.NOTES_DETAILS).document(note.getNote_doc_ID()).set(noteDetails)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "onSuccess: added note details of note " + note.getNoteTitle() + " to " + user.getEmail());
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    });
                                }

                            }
                        }
                    }
                }
            }
        });
    }
}