package com.rokudoz.onotes.Models;

public class NoteChange {
    private String type;
    private String newText;
    private String oldText;
    private Boolean newCheck;
    private Boolean oldCheck;


    public NoteChange(String type, String newText, String oldText, Boolean newCheck, Boolean oldCheck) {
        this.type = type;
        this.newText = newText;
        this.oldText = oldText;
        this.newCheck = newCheck;
        this.oldCheck = oldCheck;
    }

    public NoteChange() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNewText() {
        return newText;
    }

    public void setNewText(String newText) {
        this.newText = newText;
    }

    public String getOldText() {
        return oldText;
    }

    public void setOldText(String oldText) {
        this.oldText = oldText;
    }

    public Boolean getNewCheck() {
        return newCheck;
    }

    public void setNewCheck(Boolean newCheck) {
        this.newCheck = newCheck;
    }

    public Boolean getOldCheck() {
        return oldCheck;
    }

    public void setOldCheck(Boolean oldCheck) {
        this.oldCheck = oldCheck;
    }

    @Override
    public String toString() {
        return "NoteChange{" +
                "type='" + type + '\'' +
                ", newText='" + newText + '\'' +
                ", oldText='" + oldText + '\'' +
                ", newCheck=" + newCheck +
                ", oldCheck=" + oldCheck +
                '}';
    }
}
