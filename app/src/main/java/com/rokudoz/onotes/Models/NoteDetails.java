package com.rokudoz.onotes.Models;

import java.util.Objects;

public class NoteDetails {
    private String note_doc_id;
    private String user_id;
    private Integer note_position;
    private String note_background_color;

    public NoteDetails(String note_doc_id, String user_id, Integer note_position, String note_background_color) {
        this.note_doc_id = note_doc_id;
        this.user_id = user_id;
        this.note_position = note_position;
        this.note_background_color = note_background_color;
    }

    public NoteDetails() {
    }

    public String getNote_doc_id() {
        return note_doc_id;
    }

    public void setNote_doc_id(String note_doc_id) {
        this.note_doc_id = note_doc_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Integer getNote_position() {
        return note_position;
    }

    public void setNote_position(Integer note_position) {
        this.note_position = note_position;
    }

    public String getNote_background_color() {
        return note_background_color;
    }

    public void setNote_background_color(String note_background_color) {
        this.note_background_color = note_background_color;
    }

    @Override
    public String toString() {
        return "NoteDetails{" +
                "note_doc_id='" + note_doc_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", note_position=" + note_position +
                ", note_background_color='" + note_background_color + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteDetails that = (NoteDetails) o;
        return note_doc_id.equals(that.note_doc_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(note_doc_id);
    }
}
