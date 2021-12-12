package com.rokudoz.onotes.Data;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rokudoz.onotes.Models.Note;

import java.util.ArrayList;

public class NotesRepo {
    private static final String TAG = "NotesRepo";

    private static NotesRepo instance;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<ArrayList<Note>> allNotes;
    private final ArrayList<Note> noteList;

    public static NotesRepo getInstance() {
        if (instance == null) {
            instance = new NotesRepo();
        }
        return instance;
    }

    public NotesRepo() {
        this.allNotes = new MutableLiveData<>();
        this.noteList = new ArrayList<>();
    }

    public MutableLiveData<ArrayList<Note>> getNotes() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {

            db.collection("Notes").whereArrayContains("users", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                    .addSnapshotListener((value, error) -> {
                        if (error == null && value != null) {
                            if (value.size() > 0) {
                                for (DocumentSnapshot documentSnapshot : value) {
                                    Note note = documentSnapshot.toObject(Note.class);
                                    if (note != null) {
                                        note.setNote_doc_ID(documentSnapshot.getId());
                                        noteList.add(note);
                                        noteList.get(noteList.indexOf(note)).setNote_position(noteList.indexOf(note));
                                        Log.d(TAG, "getNotes: added note " + note.getNoteTitle());
                                    }
                                }
                                allNotes.setValue(noteList);
                            }
                        } else {
                            Log.e(TAG, "getNotes: ", error);
                        }
                    });
        }
        return allNotes;
    }

    public Note getNote(Integer position) {
        if (allNotes.getValue() == null) {
            return null;
        } else {
            return allNotes.getValue().get(position);
        }
    }

}
