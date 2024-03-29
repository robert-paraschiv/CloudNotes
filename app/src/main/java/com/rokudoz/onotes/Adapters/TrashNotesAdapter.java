package com.rokudoz.onotes.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.R;

import java.util.ArrayList;
import java.util.List;

public class TrashNotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "StaggeredRecyclerViewAd";
    private OnItemClickListener mListener;
    private final List<Note> noteList;
    private final Context mContext;

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public TrashNotesAdapter(Context context, List<Note> notesList) {
        noteList = notesList;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView noteTitle;
        final TextView noteText;

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

    public class CheckboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView noteTitle;
        final RecyclerView recyclerView;

        public CheckboxViewHolder(final View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_checkboxNote_TitleTV);
            this.recyclerView = itemView.findViewById(R.id.rv_home_checkboxNote_recyclerView);

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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_home_checkbox_note_item, parent, false);
                return new CheckboxViewHolder(view);
            case TEXT_TYPE:

            default:
                View textView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_home_note_item, parent, false);
                return new ViewHolder(textView);

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
            NonCheckableAdapter homeCheckableAdapter = new NonCheckableAdapter(checkableItemList, position);
            holder.recyclerView.setAdapter(homeCheckableAdapter);
            holder.recyclerView.setHasFixedSize(true);
            holder.recyclerView.suppressLayout(true);
//            homeCheckableAdapter.setOnItemClickListener(this);
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

