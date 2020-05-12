package com.rokudoz.onotes.Models;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class Collaborator {
    private String user_email;
    private String user_name;
    private String user_picture;
    private Boolean shouldBeFocused;
    private Boolean isCreator;
    private Integer note_position;
    private String note_background_color;

    public Collaborator() {
    }

    public Collaborator(String user_email, String user_name, String user_picture, Boolean isCreator, Integer note_position, String note_background_color) {
        this.user_email = user_email;
        this.user_name = user_name;
        this.user_picture = user_picture;
        this.isCreator = isCreator;
        this.note_position = note_position;
        this.note_background_color = note_background_color;
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

    public Boolean getCreator() {
        return isCreator;
    }

    public void setCreator(Boolean creator) {
        isCreator = creator;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_picture() {
        return user_picture;
    }

    public void setUser_picture(String user_picture) {
        this.user_picture = user_picture;
    }


    @Exclude
    public Boolean getShouldBeFocused() {
        return shouldBeFocused;
    }

    public void setShouldBeFocused(Boolean shouldBeFocused) {
        this.shouldBeFocused = shouldBeFocused;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    @Override
    public String toString() {
        return "Collaborator{" +
                "user_email='" + user_email + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_picture='" + user_picture + '\'' +
                ", shouldBeFocused=" + shouldBeFocused +
                ", isCreator=" + isCreator +
                ", note_position=" + note_position +
                ", note_background_color='" + note_background_color + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collaborator that = (Collaborator) o;
        return user_email.equals(that.user_email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_email);
    }
}
