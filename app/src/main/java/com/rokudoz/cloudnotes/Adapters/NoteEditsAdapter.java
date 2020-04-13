package com.rokudoz.cloudnotes.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.LastEdit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteEditsAdapter extends RecyclerView.Adapter<NoteEditsAdapter.ViewHolder> {

    private static final String TAG = "NoteEditsAdapter";

    private OnItemClickListener mListener;
    private List<Note> noteList = new ArrayList<>();
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView editDate, noteText;

        public ViewHolder(View itemView) {
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_note_edits_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        LastEdit lastEdit = new LastEdit();
        Date date = noteList.get(position).getCreation_date();

        holder.editDate.setText(lastEdit.getLastEdit(date.getTime()));
        holder.noteText.setText(noteList.get(position).getNoteText());

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
