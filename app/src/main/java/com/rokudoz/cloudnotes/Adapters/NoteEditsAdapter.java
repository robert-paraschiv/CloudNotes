package com.rokudoz.cloudnotes.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.LastEdit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.rokudoz.cloudnotes.App.MAX_HOME_CHECKBOX_NUMBER;

public class NoteEditsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "NoteEditsAdapter";

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;

    private OnItemClickListener mListener;
    private List<Note> noteList;
    private Context mContext;


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

        TextView editDate, noteText, editType, creatorEmail;
        CircleImageView creatorPicture;
        LinearLayout creatorLayout;

        public TextNoteViewHolder(View itemView) {
            super(itemView);
            this.editDate = itemView.findViewById(R.id.rv_note_edits_dateTv);
            this.noteText = itemView.findViewById(R.id.rv_note_edits_text);
            this.editType = itemView.findViewById(R.id.rv_note_edits_editType);
            this.creatorEmail = itemView.findViewById(R.id.rv_note_edits_creatorEmail);
            this.creatorPicture = itemView.findViewById(R.id.rv_note_edits_creatorPicture);
            this.creatorLayout = itemView.findViewById(R.id.rv_note_edits_creatorLayout);

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

        TextView noteDate, editType, creatorEmail;
        CircleImageView creatorPicture;
        LinearLayout creatorLayout;
        RecyclerView recyclerView;

        public CheckboxViewHolder(View itemView) {
            super(itemView);
            this.noteDate = itemView.findViewById(R.id.rv_note_edits_checkboxNote_dateTv);
            this.recyclerView = itemView.findViewById(R.id.rv_note_edits_checkboxNote_recyclerView);
            this.editType = itemView.findViewById(R.id.rv_note_edits_checkboxNote_editType);
            this.creatorEmail = itemView.findViewById(R.id.rv_note_edits_checkboxNote_creatorEmail);
            this.creatorPicture = itemView.findViewById(R.id.rv_note_edits_checkboxNote_creatorPicture);
            this.creatorLayout = itemView.findViewById(R.id.rv_note_edits_checkboxNote_creatorLayout);

            itemView.setOnClickListener(this);
            recyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
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

        if (currentItem.getNoteText() != null)
            holder.noteText.setText(currentItem.getNoteText());


        if (currentItem.getCreation_date() != null) {
            LastEdit lastEdit = new LastEdit();
            Date date = currentItem.getCreation_date();
            holder.editDate.setText(lastEdit.getLastEdit(date.getTime()));
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

        if (currentItem.getCollaboratorList() != null && currentItem.getLast_edited_by_user() != null && currentItem.getHas_collaborators() != null) {
            if (currentItem.getHas_collaborators()) {
                holder.creatorLayout.setVisibility(View.VISIBLE);
                holder.creatorEmail.setText(currentItem.getLast_edited_by_user());
                String creator_picture = "";
                for (int i = 0; i < currentItem.getCollaboratorList().size(); i++) {
                    if (currentItem.getLast_edited_by_user().equals(currentItem.getCollaboratorList().get(i).getUser_email())) {
                        creator_picture = currentItem.getCollaboratorList().get(i).getUser_picture();
                        break;
                    }
                }
                Glide.with(holder.creatorPicture).load(creator_picture).centerCrop().into(holder.creatorPicture);
            } else {
                holder.creatorLayout.setVisibility(View.GONE);
            }
        } else {
            holder.creatorLayout.setVisibility(View.GONE);
        }

    }

    @SuppressLint("SetTextI18n")
    private void populateCheckBoxViewHolder(CheckboxViewHolder holder, int position) {
        Note currentItem = noteList.get(position);

        LastEdit lastEdit = new LastEdit();
        if (currentItem.getCreation_date() != null) {
            Date date = currentItem.getCreation_date();
            holder.noteDate.setText(lastEdit.getLastEdit(date.getTime()));
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

            //Only show 4 checkboxes Maximum
            if (currentItem.getCheckableItemList().size() <= MAX_HOME_CHECKBOX_NUMBER) {
                checkableItemList.addAll(currentItem.getCheckableItemList());
            } else {
                for (int i = 0; i <= MAX_HOME_CHECKBOX_NUMBER; i++) {
                    checkableItemList.add(currentItem.getCheckableItemList().get(i));
                }
            }
            NonCheckableAdapter nonCheckableAdapter = new NonCheckableAdapter(checkableItemList, position);
            holder.recyclerView.setAdapter(nonCheckableAdapter);
            holder.recyclerView.setHasFixedSize(true);
            holder.recyclerView.suppressLayout(true);
        }

        if (currentItem.getCollaboratorList() != null && currentItem.getLast_edited_by_user() != null && currentItem.getHas_collaborators() != null) {
            if (currentItem.getHas_collaborators()) {
                holder.creatorLayout.setVisibility(View.VISIBLE);
                holder.creatorEmail.setText(currentItem.getLast_edited_by_user());
                String creator_picture = "";
                for (int i = 0; i < currentItem.getCollaboratorList().size(); i++) {
                    if (currentItem.getLast_edited_by_user().equals(currentItem.getCollaboratorList().get(i).getUser_email())) {
                        creator_picture = currentItem.getCollaboratorList().get(i).getUser_picture();
                        break;
                    }
                }
                Glide.with(holder.creatorPicture).load(creator_picture).centerCrop().into(holder.creatorPicture);
            } else {
                holder.creatorLayout.setVisibility(View.GONE);
            }
        } else {
            holder.creatorLayout.setVisibility(View.GONE);
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
