package com.rokudoz.cloudnotes.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.List;

import static com.rokudoz.cloudnotes.App.MAX_HOME_CHECKBOX_NUMBER;

public class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "StaggeredRecyclerViewAd";
    private OnItemClickListener mListener;
    private List<Note> noteList = new ArrayList<>();
    private List<Note> selected = new ArrayList<>();
    private Context mContext;

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;


    public interface OnItemClickListener {
        void onItemClick(int position);

        void onLongItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public HomePageAdapter(Context context, List<Note> notesList) {
        noteList = notesList;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView noteTitle, noteText;

        public ViewHolder(View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_note_TitleTV);
            this.noteText = itemView.findViewById(R.id.rv_home_note_textTv);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.size() > 0) {
                        if (selected.contains(noteList.get(position))) {
                            selected.remove(noteList.get(position));
                            unhighlightView(itemView);
                        } else {
                            selected.add(noteList.get(position));
                            highlightView(itemView);
                        }
                    }

                    mListener.onItemClick(position);
                }
            }


        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.contains(noteList.get(position))) {
                        selected.remove(noteList.get(position));
                        unhighlightView(itemView);
                    } else {
                        selected.add(noteList.get(position));
                        highlightView(itemView);
                    }
                    mListener.onLongItemClick(position);
                }
            }
            return true;
        }
    }

    public class CheckboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView noteTitle;
        RecyclerView recyclerView;

        public CheckboxViewHolder(final View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_checkboxNote_TitleTV);
            this.recyclerView = itemView.findViewById(R.id.rv_home_checkboxNote_recyclerView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            recyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (selected.size() > 0) {
                                if (selected.contains(noteList.get(position))) {
                                    selected.remove(noteList.get(position));
                                    unhighlightView(itemView);
                                } else {
                                    selected.add(noteList.get(position));
                                    highlightView(itemView);
                                }
                            }

                            mListener.onItemClick(position);
                        }
                    }
                }
            });

            recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (selected.contains(noteList.get(position))) {
                                selected.remove(noteList.get(position));
                                unhighlightView(itemView);
                            } else {
                                selected.add(noteList.get(position));
                                highlightView(itemView);
                            }
                            mListener.onLongItemClick(position);
                        }
                    }
                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.size() > 0) {
                        if (selected.contains(noteList.get(position))) {
                            selected.remove(noteList.get(position));
                            unhighlightView(itemView);
                        } else {
                            selected.add(noteList.get(position));
                            highlightView(itemView);
                        }
                    }
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.contains(noteList.get(position))) {
                        selected.remove(noteList.get(position));
                        unhighlightView(itemView);
                    } else {
                        selected.add(noteList.get(position));
                        highlightView(itemView);
                    }
                    mListener.onLongItemClick(position);
                }
            }
            return true;
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

        if (selected.contains(noteList.get(position)))
            highlightViewHolder(holder);
        else
            unhighlightViewHolder(holder);
    }

    private void populateCheckBoxViewHolder(CheckboxViewHolder holder, int position) {

        if (selected.contains(noteList.get(position)))
            highlightViewHolder(holder);
        else
            unhighlightViewHolder(holder);

        if (noteList.get(position).getNoteTitle() != null)
            holder.noteTitle.setText(noteList.get(position).getNoteTitle());
        if (noteList.get(position).getCheckableItemList() != null && noteList.get(position).getCheckableItemList().size() > 0) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            List<CheckableItem> checkableItemList = new ArrayList<>();

            //Only show 4 checkboxes Maximum
            if (noteList.get(position).getCheckableItemList().size() <= MAX_HOME_CHECKBOX_NUMBER) {
                checkableItemList.addAll(noteList.get(position).getCheckableItemList());
            } else {
                for (int i = 0; i <= MAX_HOME_CHECKBOX_NUMBER; i++) {
                    checkableItemList.add(noteList.get(position).getCheckableItemList().get(i));
                }
            }
            NonCheckableAdapter nonCheckableAdapter = new NonCheckableAdapter(checkableItemList, position);
            holder.recyclerView.setAdapter(nonCheckableAdapter);
            holder.recyclerView.setHasFixedSize(true);
            holder.recyclerView.suppressLayout(true);
        }
    }

    private void highlightViewHolder(RecyclerView.ViewHolder holder) {
        holder.itemView.setBackgroundResource(R.drawable.home_note_selected_note_background);
    }

    private void unhighlightViewHolder(RecyclerView.ViewHolder holder) {
        holder.itemView.setBackgroundResource(R.drawable.home_note_background);
    }

    private void highlightView(View view) {
        view.setBackgroundResource(R.drawable.home_note_selected_note_background);
    }

    private void unhighlightView(View view) {
        view.setBackgroundResource(R.drawable.home_note_background);
    }

    public void addAll(List<Note> items) {
        clearAll(false);
        this.noteList = items;
        notifyDataSetChanged();
    }

    public void clearAll(boolean isNotify) {
        noteList.clear();
        selected.clear();
        if (isNotify) notifyDataSetChanged();
    }

    public void clearSelected() {
        selected.clear();
//        notifyDataSetChanged();
    }

    public void selectAll() {
        selected.clear();
        selected.addAll(noteList);
        notifyDataSetChanged();
    }

    public List<Note> getSelected() {
        return selected;
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