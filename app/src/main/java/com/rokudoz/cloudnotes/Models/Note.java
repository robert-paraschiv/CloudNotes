package com.rokudoz.cloudnotes.Models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Note {
    private Integer position;
    private String note_doc_ID;
    private String noteTitle;
    private String noteText;
    private String latest_edit_doc_ID;
    private String user_ID;
    private Boolean edited;
    private String noteType;
    private List<CheckableItem> checkableItemList;
    private Boolean changedPos;
    private String edit_type;
    private Integer number_of_edits;

    @ServerTimestamp
    private Date creation_date;

    public Note(Integer position, String noteTitle, String noteText, String latest_edit_doc_ID, String user_ID, Date creation_date, Boolean edited, String noteType,
                List<CheckableItem> checkableItemList, String edit_type, Integer number_of_edits) {
        this.position = position;
        this.noteTitle = noteTitle;
        this.noteText = noteText;
        this.latest_edit_doc_ID = latest_edit_doc_ID;
        this.user_ID = user_ID;
        this.creation_date = creation_date;
        this.edited = edited;
        this.noteType = noteType;
        this.checkableItemList = checkableItemList;
        this.edit_type = edit_type;
        this.number_of_edits = number_of_edits;
    }

    public Note() {
    }

    public String getEdit_type() {
        return edit_type;
    }

    public void setEdit_type(String edit_type) {
        this.edit_type = edit_type;
    }

    public Integer getNumber_of_edits() {
        return number_of_edits;
    }

    public void setNumber_of_edits(Integer number_of_edits) {
        this.number_of_edits = number_of_edits;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getChangedPos() {
        return changedPos;
    }

    public void setChangedPos(Boolean changedPos) {
        this.changedPos = changedPos;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public List<CheckableItem> getCheckableItemList() {
        return checkableItemList;
    }

    public void setCheckableItemList(List<CheckableItem> checkableItemList) {
        this.checkableItemList = checkableItemList;
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
                "position=" + position +
                ", note_doc_ID='" + note_doc_ID + '\'' +
                ", noteTitle='" + noteTitle + '\'' +
                ", noteText='" + noteText + '\'' +
                ", latest_edit_doc_ID='" + latest_edit_doc_ID + '\'' +
                ", user_ID='" + user_ID + '\'' +
                ", edited=" + edited +
                ", noteType='" + noteType + '\'' +
                ", checkableItemList=" + checkableItemList +
                ", changedPos=" + changedPos +
                ", edit_type='" + edit_type + '\'' +
                ", number_of_edits=" + number_of_edits +
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
