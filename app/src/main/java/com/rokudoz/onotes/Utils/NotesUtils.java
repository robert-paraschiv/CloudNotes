package com.rokudoz.onotes.Utils;

import android.util.Log;

import com.rokudoz.onotes.Models.CheckableItem;

import java.util.List;

public class NotesUtils {
    private static final String TAG = "NotesUtils";

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
                            return true;
                        }
                    } else {
                        //list doesn't contain this checkbox
                        return true;
                    }
                }
                //Compare second list checkboxes with first
                for (CheckableItem item : list2) {
                    if (list1.contains(item)) {
                        if (list1.get(list1.indexOf(item)).getChecked() != item.getChecked()) {
                            //list contains the checkbox, but its checked differently
                            return true;
                        }
                    } else {
                        //list doesn't contain this checkbox
                        return true;
                    }
                }

            } else {
                //list have different checkbox list sizes, clearly they're different
                return true;
            }
        }

        return false;
    }
}
