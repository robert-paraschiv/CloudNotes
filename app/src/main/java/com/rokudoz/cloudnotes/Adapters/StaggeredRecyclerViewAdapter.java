package com.rokudoz.cloudnotes.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.List;

public class StaggeredRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HomeCheckableAdapter.OnItemClickListener {

    private static final String TAG = "StaggeredRecyclerViewAd";
    private OnItemClickListener mListener;
    private List<Note> noteList = new ArrayList<>();
    private Context mContext;

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;

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

    public class CheckboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView noteTitle;
        RecyclerView recyclerView;

        public CheckboxViewHolder(View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_checkboxNote_TitleTV);
            this.recyclerView = itemView.findViewById(R.id.rv_home_checkboxNote_recyclerView);

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case CHECKBOX_TYPE:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_home_checkbox_note_item, parent, false);
                return new CheckboxViewHolder(view);
            case TEXT_TYPE:

            default:
                View textView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_home_note_item, parent, false);
                return new ViewHolder(textView);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        int viewType = getItemViewType(position);
        switch (viewType) {
            case CHECKBOX_TYPE:
                CheckboxViewHolder checkboxViewHolder = (CheckboxViewHolder) holder;
                populateCheckBoxViewHolder(checkboxViewHolder, position);
                break;
            case TEXT_TYPE:
                ViewHolder viewHolder = (ViewHolder) holder;
                populateTextViewHolder(viewHolder, position);
                break;
        }
    }

    private void populateTextViewHolder(ViewHolder holder, int position) {
        if (noteList.get(position).getNoteText() != null)
            holder.noteText.setText(noteList.get(position).getNoteText());
        if (noteList.get(position).getNoteTitle() != null)
            holder.noteTitle.setText(noteList.get(position).getNoteTitle());
    }

    private void populateCheckBoxViewHolder(CheckboxViewHolder holder, int position) {
        if (noteList.get(position).getNoteTitle() != null)
            holder.noteTitle.setText(noteList.get(position).getNoteTitle());
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
            HomeCheckableAdapter homeCheckableAdapter = new HomeCheckableAdapter(checkableItemList, position);
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