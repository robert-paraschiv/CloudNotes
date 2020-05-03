package com.rokudoz.cloudnotes.Models;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class Collaborator {
    private String user_email;
    private String user_picture;
    private Boolean shouldBeFocused;
    private Boolean isCreator;

    public Collaborator() {
    }

    public Collaborator(String user_email, String user_picture,Boolean isCreator) {
        this.user_email = user_email;
        this.user_picture = user_picture;
        this.isCreator=isCreator;
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

    @Override
    public String toString() {
        return "Collaborator{" +
                "user_email='" + user_email + '\'' +
                ", user_picture='" + user_picture + '\'' +
                ", shouldBeFocused=" + shouldBeFocused +
                ", isCreator=" + isCreator +
                '}';
    }
}
