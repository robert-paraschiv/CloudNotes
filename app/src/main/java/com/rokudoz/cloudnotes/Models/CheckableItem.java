package com.rokudoz.cloudnotes.Models;

import android.util.Log;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class CheckableItem {
    private String text;
    private Boolean isChecked;

    public CheckableItem() {
    }

    public CheckableItem(String text, Boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "CheckableItem{" +
                "text='" + text + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CheckableItem))
            return false;
        if (obj == this)
            return true;
        return this.text.equals(((CheckableItem) obj).text);
    }

}
