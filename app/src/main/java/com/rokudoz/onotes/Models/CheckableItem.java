package com.rokudoz.onotes.Models;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class CheckableItem {
    private String text;
    private Boolean isChecked;
    private Boolean shouldBeFocused;
    private String uid;
    private String changeType;

    public CheckableItem() {
    }

    public CheckableItem(String text, Boolean isChecked, String uid) {
        this.text = text;
        this.isChecked = isChecked;
        this.uid = uid;
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

    @Exclude
    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "CheckableItem{" +
                "text='" + text + '\'' +
                ", isChecked=" + isChecked +
                ", shouldBeFocused=" + shouldBeFocused +
                ", uid='" + uid + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckableItem item = (CheckableItem) o;
        return Objects.equals(uid, item.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
