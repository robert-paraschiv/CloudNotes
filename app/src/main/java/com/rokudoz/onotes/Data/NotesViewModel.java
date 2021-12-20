package com.rokudoz.onotes.Data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudoz.onotes.Models.Note;

import java.util.ArrayList;

public class NotesViewModel extends AndroidViewModel {
    private final NotesRepo repo;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        repo = NotesRepo.getInstance();
    }

    public MutableLiveData<ArrayList<Note>> loadData() {
        return repo.getNotes();
    }

    public MutableLiveData<Note> loadSingleNote(String noteID) {
        return repo.getSingleNote(noteID);
    }

    public Note loadNote(Integer position) {
        return repo.getNote(position);
    }

    public void swapNotesPositions(int x, int y) {
        repo.swapNotesPositions(x, y);
    }
}
