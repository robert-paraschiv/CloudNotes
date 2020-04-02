package com.rokudoz.cloudnotes.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Objects;

public class Note {
    private String note_doc_ID;
    private String noteTitle;
    private String noteText;
    private String latest_edit_doc_ID;
    private String user_ID;
    private Boolean edited;

    @ServerTimestamp
    private Date creation_date;

    public Note(String noteTitle, String noteText, String latest_edit_doc_ID, String user_ID, Date creation_date,Boolean edited) {
        this.noteTitle = noteTitle;
        this.noteText = noteText;
        this.latest_edit_doc_ID = latest_edit_doc_ID;
        this.user_ID = user_ID;
        this.creation_date = creation_date;
        this.edited=edited;
    }

    public Note() {
    }

    public String getNote_doc_ID() {
        return note_doc_ID;
    }

    public void setNote_doc_ID(String note_doc_ID) {
        this.note_doc_ID = note_doc_ID;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getLatest_edit_doc_ID() {
        return latest_edit_doc_ID;
    }

    public void setLatest_edit_doc_ID(String latest_edit_doc_ID) {
        this.latest_edit_doc_ID = latest_edit_doc_ID;
    }

    public String getUser_ID() {
        return user_ID;
    }

    public void setUser_ID(String user_ID) {
        this.user_ID = user_ID;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public Boolean getEdited() {
        return edited;
    }

    public void setEdited(Boolean edited) {
        this.edited = edited;
    }

    @Override
    public String toString() {
        return "Note{" +
                "note_doc_ID='" + note_doc_ID + '\'' +
                ", noteTitle='" + noteTitle + '\'' +
                ", noteText='" + noteText + '\'' +
                ", latest_edit_doc_ID='" + latest_edit_doc_ID + '\'' +
                ", user_ID='" + user_ID + '\'' +
                ", creation_date=" + creation_date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(note_doc_ID, note.note_doc_ID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note_doc_ID);
    }
}
