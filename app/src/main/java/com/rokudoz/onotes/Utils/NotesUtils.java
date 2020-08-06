package com.rokudoz.onotes.Utils;

import android.util.Log;

import com.rokudoz.onotes.Models.CheckableItem;

import java.util.List;

public class NotesUtils {
    private static final String TAG = "NotesUtils";

    public static final String NOTE_CHANGE_TYPE_CHANGE = "change";
    public static final String NOTE_CHANGE_TYPE_ADDED = "removed";
    public static final String NOTE_CHANGE_TYPE_REMOVED = "added";

    public NotesUtils() {
    }

    public boolean compareCheckableItemLists(List<CheckableItem> list1, List<CheckableItem> list2) {

        if (list1 == null || list2 == null) {
            Log.d(TAG, "compareCheckableItemLists: lists are both null");
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
}
