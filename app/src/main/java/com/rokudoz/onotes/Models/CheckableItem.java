package com.rokudoz.onotes.Models;

import com.google.firebase.firestore.Exclude;

public class CheckableItem {
    private String text;
    private Boolean isChecked;
    private Boolean shouldBeFocused;

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

    @Exclude
    public Boolean getShouldBeFocused() {
        return shouldBeFocused;
    }

    public void setShouldBeFocused(Boolean shouldBeFocused) {
        this.shouldBeFocused = shouldBeFocused;
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
