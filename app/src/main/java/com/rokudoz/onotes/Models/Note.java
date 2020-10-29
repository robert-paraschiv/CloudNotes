package com.rokudoz.onotes.Models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

public class Note {
    private String note_doc_ID;
    private String noteTitle;
    private String noteText;
    private String creator_user_email;
    private Boolean edited;
    private String noteType;
    private List<CheckableItem> checkableItemList;
    private Boolean changedPos;
    private String edit_type;
    private Integer number_of_edits;
    private Boolean deleted;
    private List<String> users;
    private List<Collaborator> collaboratorList;
    private String last_edited_by_user;
    private Boolean has_collaborators;
    private List<NoteChange> noteChangeList;
    private String note_background_color;
    private Integer note_position;

    @ServerTimestamp
    private Date creation_date;

    public Note(String noteTitle, String noteText, String creator_user_email, Date creation_date, Boolean edited, String noteType,
                List<CheckableItem> checkableItemList, String edit_type, Integer number_of_edits, Boolean deleted, List<String> users,
                List<Collaborator> collaboratorList, String last_edited_by_user, List<NoteChange> noteChangeList) {
        this.noteTitle = noteTitle;
        this.noteText = noteText;
        this.creator_user_email = creator_user_email;
        this.creation_date = creation_date;
        this.edited = edited;
        this.noteType = noteType;
        this.checkableItemList = checkableItemList;
        this.edit_type = edit_type;
        this.number_of_edits = number_of_edits;
        this.deleted = deleted;
        this.users = users;
        this.collaboratorList = collaboratorList;
        this.last_edited_by_user = last_edited_by_user;
        this.noteChangeList = noteChangeList;
    }

    public Note() {
    }

    @Exclude
    public Boolean getHas_collaborators() {
        return has_collaborators;
    }

    public void setHas_collaborators(Boolean has_collaborators) {
        this.has_collaborators = has_collaborators;
    }

    public String getNote_background_color() {
        return note_background_color;
    }

    public void setNote_background_color(String note_background_color) {
        this.note_background_color = note_background_color;
    }

    public Integer getNote_position() {
        return note_position;
    }

    public void setNote_position(Integer note_position) {
        this.note_position = note_position;
    }

    public String getLast_edited_by_user() {
        return last_edited_by_user;
    }

    public void setLast_edited_by_user(String last_edited_by_user) {
        this.last_edited_by_user = last_edited_by_user;
    }

    public List<Collaborator> getCollaboratorList() {
        return collaboratorList;
    }

    public void setCollaboratorList(List<Collaborator> collaboratorList) {
        this.collaboratorList = collaboratorList;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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


    public String getCreator_user_email() {
        return creator_user_email;
    }

    public void setCreator_user_email(String creator_user_email) {
        this.creator_user_email = creator_user_email;
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

    public List<NoteChange> getNoteChangeList() {
        return noteChangeList;
    }

    public void setNoteChangeList(List<NoteChange> noteChangeList) {
        this.noteChangeList = noteChangeList;
    }

    @Override
    public String toString() {
        return "Note{" +
                "note_doc_ID='" + note_doc_ID + '\'' +
                ", noteTitle='" + noteTitle + '\'' +
                ", noteText='" + noteText + '\'' +
                ", creator_user_email='" + creator_user_email + '\'' +
                ", edited=" + edited +
                ", noteType='" + noteType + '\'' +
                ", checkableItemList=" + checkableItemList +
                ", changedPos=" + changedPos +
                ", edit_type='" + edit_type + '\'' +
                ", number_of_edits=" + number_of_edits +
                ", deleted=" + deleted +
                ", users=" + users +
                ", collaboratorList=" + collaboratorList +
                ", last_edited_by_user='" + last_edited_by_user + '\'' +
                ", has_collaborators=" + has_collaborators +
                ", noteChangeList=" + noteChangeList +
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
