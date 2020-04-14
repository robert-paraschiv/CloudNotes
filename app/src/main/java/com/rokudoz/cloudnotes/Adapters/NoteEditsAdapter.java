package com.rokudoz.cloudnotes.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.LastEdit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteEditsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements NonCheckableAdapter.OnItemClickListener {

    private static final String TAG = "NoteEditsAdapter";

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;

    private OnItemClickListener mListener;
    private List<Note> noteList = new ArrayList<>();
    private Context mContext;

    @Override
    public void onItemClick(int position) {
        mListener.onItemClick(position);
    }

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

        TextView editDate, noteText;

        public TextNoteViewHolder(View itemView) {
            super(itemView);
            this.editDate = itemView.findViewById(R.id.rv_note_edits_dateTv);
            this.noteText = itemView.findViewById(R.id.rv_note_edits_text);

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

        TextView noteDate;
        RecyclerView recyclerView;

        public CheckboxViewHolder(View itemView) {
            super(itemView);
            this.noteDate = itemView.findViewById(R.id.rv_note_edits_checkboxNote_dateTv);
            this.recyclerView = itemView.findViewById(R.id.rv_note_edits_checkboxNote_recyclerView);

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

    private void populateTextViewHolder(TextNoteViewHolder holder, int position) {
        if (noteList.get(position).getNoteText() != null)
            holder.noteText.setText(noteList.get(position).getNoteText());

        LastEdit lastEdit = new LastEdit();
        Date date = noteList.get(position).getCreation_date();

        holder.editDate.setText(lastEdit.getLastEdit(date.getTime()));
    }

    private void populateCheckBoxViewHolder(CheckboxViewHolder holder, int position) {


        LastEdit lastEdit = new LastEdit();
        if (noteList.get(position).getCreation_date()!=null){
            Date date = noteList.get(position).getCreation_date();
            holder.noteDate.setText(lastEdit.getLastEdit(date.getTime()));
        }


        if (noteList.get(position).getCheckableItemList() != null && noteList.get(position).getCheckableItemList().size() > 0) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            List<CheckableItem> checkableItemList = new ArrayList<>();

            //Only show 4 checkboxes Maximum
            if (noteList.get(position).getCheckableItemList().size() <= 3) {
                checkableItemList.addAll(noteList.get(position).getCheckableItemList());
            } else {
                for (int i = 0; i <= 3; i++) {
                    checkableItemList.add(noteList.get(position).getCheckableItemList().get(i));
                }
            }
            NonCheckableAdapter homeCheckableAdapter = new NonCheckableAdapter(checkableItemList, position);
            holder.recyclerView.setAdapter(homeCheckableAdapter);
            holder.recyclerView.setHasFixedSize(true);
            homeCheckableAdapter.setOnItemClickListener(this);
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
