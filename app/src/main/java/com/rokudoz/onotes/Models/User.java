package com.rokudoz.onotes.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {
    private String user_id;
    private String user_name;
    private String user_userName;
    private String email;
    private String user_profile_picture;

    @ServerTimestamp
    private
    Date user_join_date;

    public User(String user_id, String user_name, String user_userName, String email, String user_profile_picture, Date user_join_date) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_userName = user_userName;
        this.email = email;
        this.user_profile_picture = user_profile_picture;
        this.user_join_date = user_join_date;
    }

    public User() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_userName() {
        return user_userName;
    }

    public void setUser_userName(String user_userName) {
        this.user_userName = user_userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_profile_picture() {
        return user_profile_picture;
    }

    public void setUser_profile_picture(String user_profile_picture) {
        this.user_profile_picture = user_profile_picture;
    }

    public Date getUser_join_date() {
        return user_join_date;
    }

    public void setUser_join_date(Date user_join_date) {
        this.user_join_date = user_join_date;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_userName='" + user_userName + '\'' +
                ", email='" + email + '\'' +
                ", user_profile_picture='" + user_profile_picture + '\'' +
                ", user_join_date=" + user_join_date +
                '}';
    }
}
