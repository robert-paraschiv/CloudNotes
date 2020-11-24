package com.rokudoz.onotes.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.Models.NoteChange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotesUtils {
    private static final String TAG = "NotesUtils";

    public static final String NOTES_DETAILS = "NotesDetails";
    public static final String NOTE_CHANGE_TYPE_CHANGE = "change";
    public static final String NOTE_CHANGE_TYPE_ADDED = "added";
    public static final String NOTE_CHANGE_TYPE_REMOVED = "removed";
    public static final String NOTE_TYPE_TEXT = "text";
    public static final String NOTE_TYPE_CHECKBOX = "checkbox";

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

    // Checks for differences between old note's text and new one's
    public static List<NoteChange> getTextNoteChanges(String currentNoteText, String oldNoteText, String oldNoteType) {
        List<NoteChange> noteChangeList = new ArrayList<>();

        List<String> currentNoteTextList = Arrays.asList(currentNoteText.split("\\r?\\n"));
        List<String> oldNoteTextList = Arrays.asList(oldNoteText.split("\\r?\\n"));

        if (!oldNoteType.equals("text"))
            oldNoteTextList = new ArrayList<>();

        for (int i = 0; i < currentNoteTextList.size(); i++) {
            if (oldNoteTextList.size() > i) {
                if (!currentNoteTextList.get(i).equals(oldNoteTextList.get(i))) {
                    noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_CHANGE, currentNoteTextList.get(i), oldNoteTextList.get(i), null, null));
                }
            } else {
                noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_ADDED, currentNoteTextList.get(i), null, null, null));
            }
        }
        if (oldNoteTextList.size() > currentNoteTextList.size()) {
            for (int i = currentNoteTextList.size() ; i < oldNoteTextList.size(); i++) {
                noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_REMOVED, null, oldNoteTextList.get(i), null, null));
            }
        }

        return noteChangeList;
    }

    // Checks for differences between checkbox items
    public static List<NoteChange> getCheckboxNoteChanges(List<CheckableItem> currentCheckboxList, List<CheckableItem> oldCbList) {
        List<NoteChange> noteChangeList = new ArrayList<>();

        List<CheckableItem> oldCheckboxList = oldCbList;
        List<CheckableItem> comparedItemsList = new ArrayList<>();

        if (oldCheckboxList == null)
            oldCheckboxList = new ArrayList<>();

        for (CheckableItem oldItem : oldCheckboxList) {
            if (currentCheckboxList.contains(oldItem)) {

                CheckableItem newItem = currentCheckboxList.get(currentCheckboxList.indexOf(oldItem));
                comparedItemsList.add(newItem);

                if (!oldItem.getText().equals(newItem.getText()) || !oldItem.getChecked().equals(newItem.getChecked())) {
                    noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_CHANGE, newItem.getText(), oldItem.getText(), newItem.getChecked(), oldItem.getChecked()));
                }

            } else {
                noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_REMOVED, null, oldItem.getText(), null, oldItem.getChecked()));
            }
        }

        for (CheckableItem newItem : currentCheckboxList) {
            if (oldCheckboxList.contains(newItem)) {

                CheckableItem oldItem = oldCheckboxList.get(oldCheckboxList.indexOf(newItem));
                if (!comparedItemsList.contains(oldItem)) {
                    if (!oldItem.getText().equals(newItem.getText()) || !oldItem.getChecked().equals(newItem.getChecked())) {
                        comparedItemsList.add(oldItem);
                        noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_CHANGE, newItem.getText(), oldItem.getText(), newItem.getChecked(), oldItem.getChecked()));
                    }
                }

            } else {
                noteChangeList.add(new NoteChange(NOTE_CHANGE_TYPE_ADDED, newItem.getText(), null, newItem.getChecked(), null));
            }
        }
        return noteChangeList;
    }


    /* Splits a string's lines into a list to calculate a RON sum
    *  If the line contains the word "lei" it tries to get the amount and add it to the sum
    * */
    public static Integer CalculateRONPrice(String text, Context context) {
        String[] noteTextList = text.split("\\r?\\n");
        int price = 0;
        for (String line : noteTextList) {
            StringBuilder temp_price = new StringBuilder();
            if (line.contains("lei")) {
                if (Character.isDigit(line.charAt(0))) {
                    for (int i = 0; i < line.length(); i++) {
                        if (Character.isDigit(line.charAt(i))) {
                            temp_price.append(line.charAt(i));
                        } else if (line.charAt(i) == 'l') {
                            break;
                        }
                    }
                }
                if (!temp_price.toString().trim().equals(""))
                    price += Integer.parseInt(temp_price.toString());
            }
        }
        Toast.makeText(context, "" + price, Toast.LENGTH_SHORT).show();
        if (price != 0)
            return price;
        else
            return null;
    }
}
