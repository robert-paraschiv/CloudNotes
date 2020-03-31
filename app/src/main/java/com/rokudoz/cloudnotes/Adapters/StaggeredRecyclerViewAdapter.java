package com.rokudoz.cloudnotes.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.List;

public class StaggeredRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "StaggeredRecyclerViewAd";
    private OnItemClickListener mListener;
    private List<Note> noteList = new ArrayList<>();
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public StaggeredRecyclerViewAdapter(Context context, List<Note> notesList) {
        noteList = notesList;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView noteTitle, noteText;

        public ViewHolder(View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_note_TitleTV);
            this.noteText = itemView.findViewById(R.id.rv_home_note_textTv);

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_home_note_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.noteText.setText(noteList.get(position).getNoteText());
        holder.noteTitle.setText(noteList.get(position).getNoteTitle());

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }


}