package com.rokudoz.onotes.Utils;

import android.util.Log;

import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Note;

import java.util.List;

public class NotesUtils {
    private static final String TAG = "NotesUtils";

    public static final String NOTE_CHANGE_TYPE_CHANGE = "change";
    public static final String NOTE_CHANGE_TYPE_ADDED = "added";
    public static final String NOTE_CHANGE_TYPE_REMOVED = "removed";

    public NotesUtils() {
    }

    public static boolean compareCheckableItemLists(List<CheckableItem> list1, List<CheckableItem> list2) {

        if (list1 == null && list2 == null) {
            Log.d(TAG, "compareCheckableItemLists: lists are both null");
        } else if (list1 != null && list2 == null) {
            Log.d(TAG, "compareCheckableItemLists: first list not null, second list null");
            return true;
        } else if (list1 == null && list2 != null) {
            Log.d(TAG, "compareCheckableItemLists: first list null, second list not null");
            return true;
        } else {
            if (list1.size() == list2.size()) {

                //Compare first list checkboxes with second
                for (CheckableItem item : list1) {
                    if (list2.contains(item)) {
                        if (list2.get(list2.indexOf(item)).getChecked() != item.getChecked()) {
                            //list contains the checkbox, but its checked differently
                            Log.d(TAG, "compareCheckableItemLists: item1 " + item.getUid() + " " + item.getText() + " " + item.getChecked() + " item2 " + list2.get(list2.indexOf(item)).getUid() + " " + list2.get(list2.indexOf(item)).getText() +
                                    " " + list2.get(list2.indexOf(item)).getChecked() + "");
                            return true;
                        }
                    } else {
                        Log.d(TAG, "compareCheckableItemLists: list2 doesnt contain " + item.getText() + " " + item.getChecked());
                        //list doesn't contain this checkbox
                        return true;
                    }
                }
                //Compare second list checkboxes with first
                for (CheckableItem item : list2) {
                    if (list1.contains(item)) {
                        if (list1.get(list1.indexOf(item)).getChecked() != item.getChecked()) {
                            //list contains the checkbox, but its checked differently
                            Log.d(TAG, "compareCheckableItemLists: item1 " + item.getText() + " " + item.getChecked() + " item2 " + list1.get(list1.indexOf(item)).getText() +
                                    " " + list1.get(list1.indexOf(item)).getChecked() + "");
                            return true;
                        }
                    } else {
                        //list doesn't contain this checkbox
                        return true;
                    }
                }

            } else {
                //list have different checkbox list sizes, clearly they're different
                Log.d(TAG, "compareCheckableItemLists: list have different checkbox list sizes, clearly they're different");
                return true;
            }
        }

        return false;
    }

    public static boolean checkIfNotesAreDifferent(Note newNote, Note oldNote) {

        //Check if they have different Titles
        if (!newNote.getNoteTitle().equals(oldNote.getNoteTitle()))
            return true;


        //If notes have different types, they're changed
        if (!newNote.getNoteType().equals(oldNote.getNoteType())) {
            return true;
        } else {
            //Check if they have different text
            if (newNote.getNoteType().equals("text")) {
                if (!newNote.getNoteText().equals(oldNote.getNoteText()))
                    return true;

            } else if (newNote.getNoteType().equals("checkbox")) {
                if (newNote.getCheckableItemList() != null && oldNote.getCheckableItemList() == null) {
                    return true;
                } else if (newNote.getCheckableItemList() == null && oldNote.getCheckableItemList() != null) {
                    return true;
                } else if (newNote.getCheckableItemList() != null && oldNote.getCheckableItemList() != null) {
                    boolean different = compareCheckableItemLists(newNote.getCheckableItemList(), oldNote.getCheckableItemList());
                    if (different)
                        return true;
                }
            }
        }

        //Check for collaborators differences
        if (newNote.getCollaboratorList().size() != oldNote.getCollaboratorList().size()) {
            return true;
        }
        if (newNote.getUsers().size() != oldNote.getUsers().size()) {
            return true;
        } else {
            for (String user : newNote.getUsers()) {
                if (!oldNote.getUsers().contains(user)) {
                    return true;
                }
            }
            for (String user : oldNote.getUsers()) {
                if (!newNote.getUsers().contains(user)) {
                    return true;
                }
            }
        }

        Log.d(TAG, "checkIfNotesAreDifferent: false note: " + newNote.getNoteTitle());
        return false;
    }

}
