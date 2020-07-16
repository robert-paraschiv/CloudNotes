package com.rokudoz.onotes.Models;

public class TextChange {
    private String newText;
    private String oldText;
    private String type;


    public TextChange(String newText, String oldText, String type) {
        this.newText = newText;
        this.oldText = oldText;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TextChange{" +
                "newText='" + newText + '\'' +
                ", oldText='" + oldText + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
