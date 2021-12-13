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

            db.collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("Notes")
                    .addSnapshotListener((value, error) -> {
                        if (error == null && value != null) {
                            if (value.size() > 0) {
                                for (DocumentSnapshot documentSnapshot : value) {
                                    Note note = documentSnapshot.toObject(Note.class);
                                    if (note != null) {
                                        note.setNote_doc_ID(documentSnapshot.getId());
                                        handleNoteEvent(note);
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

    private void handleNoteEvent(Note note) {
        if (noteList.contains(note)) {
            int notePosition = noteList.indexOf(note);
            Note oldNote = noteList.get(notePosition);
            if (isNoteDifferent(oldNote, note)) {
                note.setNote_position(notePosition);
                noteList.set(notePosition, note);
            }
        } else {
            noteList.add(note);
            noteList.get(noteList.indexOf(note)).setNote_position(noteList.indexOf(note));
            Log.d(TAG, "getNotes: added note " + note.getNoteTitle());
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isNoteDifferent(Note oldNote, Note note) {
        if (!oldNote.getNoteTitle().equals(note.getNoteTitle()))
            return true;

        if (!oldNote.getNoteType().equals(note.getNoteType()))
            return true;

        if (!oldNote.getNoteText().equals(note.getNoteText()))
            return true;

        if (oldNote.getNote_background_color() == null && note.getNote_background_color() != null
                || oldNote.getNote_background_color() != null && note.getNote_background_color() == null
                || (oldNote.getNote_background_color() != null && note.getNote_background_color() != null
                && !oldNote.getNote_background_color().equals(note.getNote_background_color())))
            return true;

        if (oldNote.getCheckableItemList() == null && note.getCheckableItemList() != null
                || oldNote.getCheckableItemList() != null && note.getCheckableItemList() == null
                || (oldNote.getCheckableItemList() != null && note.getCheckableItemList() != null
                && !oldNote.getCheckableItemList().equals(note.getCheckableItemList())))
            return true;

        if (oldNote.getCollaboratorList() == null && note.getCollaboratorList() != null
                || oldNote.getCollaboratorList() != null && note.getCollaboratorList() == null
                || (oldNote.getCollaboratorList() != null && note.getCollaboratorList() != null
                && !oldNote.getCollaboratorList().equals(note.getCollaboratorList())))
            return true;

        if (oldNote.getCreation_date() == null && note.getCreation_date() != null
                || oldNote.getCreation_date() != null && note.getCreation_date() == null
                || (oldNote.getCreation_date() != null && note.getCreation_date() != null
                && !oldNote.getCreation_date().equals(note.getCreation_date())))
            return true;

        if (!oldNote.getUsers().equals(note.getUsers()))
            return true;

        return false;
    }

    public Note getNote(Integer position) {
        if (allNotes.getValue() == null) {
            return null;
        } else {
            return allNotes.getValue().get(position);
        }
    }

    public void deleteNote(int position) {
        if (allNotes.getValue() == null || position >= allNotes.getValue().size()) {
            Log.e(TAG, "deleteNote: null or out of bounds allNotes");
        } else {
            allNotes.getValue().remove(position);
        }
    }

}
