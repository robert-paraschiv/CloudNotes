package com.rokudoz.onotes.Models;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class Collaborator {
    private String user_id;
    private String user_email;
    private String user_name;
    private String user_picture;
    private Boolean shouldBeFocused;
    private Boolean isCreator;

    public Collaborator() {
    }

    public Collaborator(String user_id,String user_email, String user_name, String user_picture, Boolean isCreator) {
        this.user_id=user_id;
        this.user_email = user_email;
        this.user_name = user_name;
        this.user_picture = user_picture;
        this.isCreator = isCreator;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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
