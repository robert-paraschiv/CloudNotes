package com.rokudoz.onotes.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.Models.NoteChange;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.LastEdit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.rokudoz.onotes.App.MAX_HOME_CHECKBOX_NUMBER;
import static com.rokudoz.onotes.Utils.NotesUtils.NOTE_CHANGE_TYPE_ADDED;
import static com.rokudoz.onotes.Utils.NotesUtils.NOTE_CHANGE_TYPE_CHANGE;
import static com.rokudoz.onotes.Utils.NotesUtils.NOTE_CHANGE_TYPE_REMOVED;

public class NoteEditsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "NoteEditsAdapter";

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;

    private OnItemClickListener mListener;
    private final List<Note> noteList;
    private final Context mContext;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public NoteEditsAdapter(Context context, List<Note> notesList) {
        noteList = notesList;
        mContext = context;
    }

    public class TextNoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView editDate;
        final TextView noteText;
        final TextView editType;
        final TextView creatorEmail;
        final TextView noCollaboratorsDateTv;
        final CircleImageView creatorPicture;
        final LinearLayout creatorLayout;

        public TextNoteViewHolder(View itemView) {
            super(itemView);
            this.editDate = itemView.findViewById(R.id.rv_note_edits_dateTv);
            this.noteText = itemView.findViewById(R.id.rv_note_edits_text);
            this.editType = itemView.findViewById(R.id.rv_note_edits_editType);
            this.creatorEmail = itemView.findViewById(R.id.rv_note_edits_creatorEmail);
            this.creatorPicture = itemView.findViewById(R.id.rv_note_edits_creatorPicture);
            this.creatorLayout = itemView.findViewById(R.id.rv_note_edits_creatorLayout);
            this.noCollaboratorsDateTv = itemView.findViewById(R.id.rv_note_edits_noCollaborators_dateTv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }
    }

    public class CheckboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView noteDate;
        final TextView editType;
        final TextView creatorEmail;
        final TextView noCollaboratorsDateTv;
        final CircleImageView creatorPicture;
        final LinearLayout creatorLayout;
        final RecyclerView recyclerView;

        public CheckboxViewHolder(View itemView) {
            super(itemView);
            this.noteDate = itemView.findViewById(R.id.rv_note_edits_checkboxNote_dateTv);
            this.recyclerView = itemView.findViewById(R.id.rv_note_edits_checkboxNote_recyclerView);
            this.editType = itemView.findViewById(R.id.rv_note_edits_checkboxNote_editType);
            this.creatorEmail = itemView.findViewById(R.id.rv_note_edits_checkboxNote_creatorEmail);
            this.creatorPicture = itemView.findViewById(R.id.rv_note_edits_checkboxNote_creatorPicture);
            this.creatorLayout = itemView.findViewById(R.id.rv_note_edits_checkboxNote_creatorLayout);
            this.noCollaboratorsDateTv = itemView.findViewById(R.id.rv_note_edits_checkboxNote_noCollaborators_dateTv);

            itemView.setOnClickListener(this);
            recyclerView.setOnClickListener(v -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position);
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case CHECKBOX_TYPE:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_note_edits_checkbox_note, parent, false);
                return new CheckboxViewHolder(view);
            case TEXT_TYPE:

            default:
                View textView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_note_edits_item, parent, false);
                return new TextNoteViewHolder(textView);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        int viewType = getItemViewType(position);
        switch (viewType) {
            case CHECKBOX_TYPE:
                CheckboxViewHolder checkboxViewHolder = (CheckboxViewHolder) holder;
                populateCheckBoxViewHolder(checkboxViewHolder, position);
                break;
            case TEXT_TYPE:
                TextNoteViewHolder viewHolder = (TextNoteViewHolder) holder;
                populateTextViewHolder(viewHolder, position);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void populateTextViewHolder(TextNoteViewHolder holder, int position) {
        Note currentItem = noteList.get(position);


        //Get note changes and assign colors accordingly
        if (currentItem.getNoteChangeList() != null) {
            holder.noteText.setMaxLines(Integer.MAX_VALUE);
            //TODO change the colors to make them more pleasing to the eye

            SpannableStringBuilder builder = new SpannableStringBuilder();

            for (int i = 0; i < currentItem.getNoteChangeList().size(); i++) {

                String newText = currentItem.getNoteChangeList().get(i).getNewText();
                String oldText = currentItem.getNoteChangeList().get(i).getOldText();

                if (i < currentItem.getNoteChangeList().size() - 1) {

                    if (currentItem.getNoteChangeList().get(i).getType().equals(NOTE_CHANGE_TYPE_CHANGE)) {

                        SpannableString redSpannable = new SpannableString(oldText);
                        redSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_red))
                                , 0, oldText.length(), 0);
                        builder.append(redSpannable).append("\n");

                        SpannableString blueSpannable = new SpannableString(newText);
                        blueSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_blue))
                                , 0, newText.length(), 0);
                        builder.append(blueSpannable).append("\n");

                    } else if (currentItem.getNoteChangeList().get(i).getType().equals(NOTE_CHANGE_TYPE_REMOVED)) {
                        SpannableString redSpannable = new SpannableString(oldText);
                        redSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_red))
                                , 0, oldText.length(), 0);
                        builder.append(redSpannable).append("\n");
                    } else {
                        SpannableString greenSpannable = new SpannableString(newText);
                        greenSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_green))
                                , 0, newText.length(), 0);
                        builder.append(greenSpannable).append("\n");
                    }

                    builder.append("\n");

                } else { //If this is the last item in the list

                    if (currentItem.getNoteChangeList().get(i).getType().equals(NOTE_CHANGE_TYPE_CHANGE)) {

                        SpannableString redSpannable = new SpannableString(oldText);
                        redSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_red))
                                , 0, oldText.length(), 0);
                        builder.append(redSpannable).append("\n");

                        SpannableString blueSpannable = new SpannableString(newText);
                        blueSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_blue))
                                , 0, newText.length(), 0);
                        builder.append(blueSpannable);
                    } else if (currentItem.getNoteChangeList().get(i).getType().equals(NOTE_CHANGE_TYPE_REMOVED)) {
                        SpannableString redSpannable = new SpannableString(oldText);
                        redSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_red))
                                , 0, oldText.length(), 0);
                        builder.append(redSpannable);
                    } else {
                        SpannableString greenSpannable = new SpannableString(newText);
                        greenSpannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(mContext, R.color.note_background_color_green))
                                , 0, newText.length(), 0);
                        builder.append(greenSpannable);
                    }
                }
            }
            holder.noteText.setText(builder, TextView.BufferType.SPANNABLE);
        } else {
            if (currentItem.getNoteText() != null) {
                holder.noteText.setMaxLines(6);
                holder.noteText.setText(currentItem.getNoteText());
            }
        }


        if (currentItem.getCreation_date() != null) {
            Date date = currentItem.getCreation_date();
            holder.editDate.setText(LastEdit.getLastEdit(date.getTime()));
            holder.noCollaboratorsDateTv.setText(LastEdit.getLastEdit(date.getTime()));
        }

        if (currentItem.getEdit_type() != null) {
            switch (currentItem.getEdit_type()) {
                case "Edited":
                    holder.editType.setTextColor(mContext.getColor(R.color.edit_type_edited));
                    break;
                case "Restored":
                    holder.editType.setTextColor(mContext.getColor(R.color.edit_type_restored));
                    break;
                default:
                    holder.editType.setTextColor(mContext.getColor(R.color.edit_type_created));
                    break;
            }
            holder.editType.setText(currentItem.getEdit_type());
        } else {
            holder.editType.setText("Created");
            holder.editType.setTextColor(mContext.getColor(R.color.edit_type_created));
        }

        if (currentItem.getCollaboratorList() != null && currentItem.getLast_edited_by_user() != null) {

            holder.noCollaboratorsDateTv.setVisibility(View.GONE);
            holder.creatorLayout.setVisibility(View.VISIBLE);
            holder.creatorEmail.setText(currentItem.getLast_edited_by_user());
            String creator_picture = "";
            String creator_name = "";
            for (int i = 0; i < currentItem.getCollaboratorList().size(); i++) {
                if (currentItem.getLast_edited_by_user().equals(currentItem.getCollaboratorList().get(i).getUser_email())) {
                    creator_picture = currentItem.getCollaboratorList().get(i).getUser_picture();
                    if (currentItem.getCollaboratorList().get(i).getUser_name() == null) {
                        creator_name = currentItem.getLast_edited_by_user();
                    } else
                        creator_name = currentItem.getCollaboratorList().get(i).getUser_name();
                    break;
                }
            }
            Glide.with(holder.creatorPicture).load(creator_picture).centerCrop().into(holder.creatorPicture);
            holder.creatorEmail.setText(creator_name);

        } else {
            holder.noCollaboratorsDateTv.setVisibility(View.VISIBLE);
            holder.creatorLayout.setVisibility(View.GONE);
        }

    }

    @SuppressLint("SetTextI18n")
    private void populateCheckBoxViewHolder(CheckboxViewHolder holder, int position) {
        Note currentItem = noteList.get(position);

        if (currentItem.getCreation_date() != null) {
            Date date = currentItem.getCreation_date();
            holder.noteDate.setText(LastEdit.getLastEdit(date.getTime()));
            holder.noCollaboratorsDateTv.setText(LastEdit.getLastEdit(date.getTime()));
        }

        if (currentItem.getEdit_type() != null) {
            switch (currentItem.getEdit_type()) {
                case "Edited":
                    holder.editType.setTextColor(mContext.getColor(R.color.edit_type_edited));
                    break;
                case "Restored":
                    holder.editType.setTextColor(mContext.getColor(R.color.edit_type_restored));
                    break;
                default:
                    holder.editType.setTextColor(mContext.getColor(R.color.edit_type_created));
                    break;
            }
            holder.editType.setText(currentItem.getEdit_type());
        } else {
            holder.editType.setText("Created");
        }

        if (currentItem.getCheckableItemList() != null && currentItem.getCheckableItemList().size() > 0) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            List<CheckableItem> checkableItemList = new ArrayList<>();

            //Get changes into checkbox list
            if (currentItem.getNoteChangeList() != null) {
                for (NoteChange noteChange : currentItem.getNoteChangeList()) {
                    String uid = "" + System.currentTimeMillis() + checkableItemList.size();

                    CheckableItem checkableItem = new CheckableItem();
                    checkableItem.setUid(uid);

                    switch (noteChange.getType()) {
                        case NOTE_CHANGE_TYPE_ADDED:
                            checkableItem.setChangeType(NOTE_CHANGE_TYPE_ADDED);
                            checkableItem.setText(noteChange.getNewText());
                            checkableItem.setChecked(noteChange.getNewCheck());

                            break;
                        case NOTE_CHANGE_TYPE_CHANGE:
                            checkableItem.setChangeType(NOTE_CHANGE_TYPE_CHANGE);
                            checkableItem.setText(noteChange.getNewText());
                            checkableItem.setChecked(noteChange.getNewCheck());

                            break;
                        case NOTE_CHANGE_TYPE_REMOVED:
                            checkableItem.setChangeType(NOTE_CHANGE_TYPE_REMOVED);
                            checkableItem.setText(noteChange.getOldText());
                            checkableItem.setChecked(noteChange.getOldCheck());

                            break;
                    }
                    checkableItemList.add(checkableItem);
                }

            } else {
                //Only show 4 checkboxes Maximum
                if (currentItem.getCheckableItemList().size() <= MAX_HOME_CHECKBOX_NUMBER) {
                    checkableItemList.addAll(currentItem.getCheckableItemList());
                } else {
                    for (int i = 0; i <= MAX_HOME_CHECKBOX_NUMBER; i++) {
                        checkableItemList.add(currentItem.getCheckableItemList().get(i));
                    }
                }
            }

            NonCheckableAdapter nonCheckableAdapter = new NonCheckableAdapter(checkableItemList, position);
            holder.recyclerView.setAdapter(nonCheckableAdapter);
            holder.recyclerView.setHasFixedSize(true);
            holder.recyclerView.suppressLayout(true);
        }

        if (currentItem.getCollaboratorList() != null && currentItem.getLast_edited_by_user() != null) {
            holder.noCollaboratorsDateTv.setVisibility(View.GONE);
            holder.creatorLayout.setVisibility(View.VISIBLE);
            String creator_picture = "";
            String creator_name = "";
            for (int i = 0; i < currentItem.getCollaboratorList().size(); i++) {
                if (currentItem.getLast_edited_by_user().equals(currentItem.getCollaboratorList().get(i).getUser_email())) {
                    creator_picture = currentItem.getCollaboratorList().get(i).getUser_picture();
                    if (currentItem.getCollaboratorList().get(i).getUser_name() == null) {
                        creator_name = currentItem.getLast_edited_by_user();
                    } else
                        creator_name = currentItem.getCollaboratorList().get(i).getUser_name();
                    break;
                }
            }
            Glide.with(holder.creatorPicture).load(creator_picture).centerCrop().into(holder.creatorPicture);
            holder.creatorEmail.setText(creator_name);

        } else {
            holder.creatorLayout.setVisibility(View.GONE);
            holder.noCollaboratorsDateTv.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (noteList.get(position).getNoteType() != null && noteList.get(position).getNoteType().equals("checkbox")) {
            return CHECKBOX_TYPE;
        } else {
            return TEXT_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
