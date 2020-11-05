package com.rokudoz.onotes.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.rokudoz.onotes.App.MAX_HOME_CHECKBOX_NUMBER;
import static com.rokudoz.onotes.App.MAX_HOME_COLLABORATORS_PICTURES;

public class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final String TAG = "StaggeredRecyclerViewAd";
    private OnItemClickListener mListener;
    private List<Note> noteList;
    private List<Note> selected = new ArrayList<>();
    private List<Note> noteListFull = new ArrayList<>();
    private Context mContext;

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;


    public interface OnItemClickListener {
        void onItemClick(int position, TextView title, TextView text, RecyclerView checkboxRv, RecyclerView collaboratorsRv, RelativeLayout rootLayout);

        void onLongItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public HomePageAdapter(Context context, List<Note> notesList) {
        this.noteList = notesList;
//        this.noteListFull = notesList;

        noteListFull = notesList;

        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView noteTitle, noteText;
        RecyclerView collaboratorsRv;
        RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_note_TitleTV);
            this.noteText = itemView.findViewById(R.id.rv_home_note_textTv);
            this.collaboratorsRv = itemView.findViewById(R.id.rv_home_note_collaboratorsRV);
            this.relativeLayout = itemView.findViewById(R.id.rv_home_note_rootLayout);

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
                            unhighlightView(itemView, position);
                        } else {
                            selected.add(noteList.get(position));
                            highlightView(itemView, position);
                        }
                    }

                    mListener.onItemClick(position, noteTitle, noteText, null, collaboratorsRv, relativeLayout);
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
                        unhighlightView(itemView, position);
                    } else {
                        selected.add(noteList.get(position));
                        highlightView(itemView, position);
                    }
                    mListener.onLongItemClick(position);
                }
            }
            return true;
        }
    }

    public class CheckboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView noteTitle;
        RecyclerView recyclerView, collaboratorsRv;
        RelativeLayout relativeLayout;

        public CheckboxViewHolder(final View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_checkboxNote_TitleTV);
            this.recyclerView = itemView.findViewById(R.id.rv_home_checkboxNote_recyclerView);
            this.collaboratorsRv = itemView.findViewById(R.id.rv_home_checkboxNote_collaboratorsRV);
            this.relativeLayout = itemView.findViewById(R.id.rv_home_note_rootLayout);


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
                                    unhighlightView(itemView, position);
                                } else {
                                    selected.add(noteList.get(position));
                                    highlightView(itemView, position);
                                }
                            }

                            mListener.onItemClick(position, noteTitle, null, recyclerView, collaboratorsRv, relativeLayout);
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
                                unhighlightView(itemView, position);
                            } else {
                                selected.add(noteList.get(position));
                                highlightView(itemView, position);
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
                            unhighlightView(itemView, position);
                        } else {
                            selected.add(noteList.get(position));
                            highlightView(itemView, position);
                        }
                    }
                    mListener.onItemClick(position, noteTitle, null, recyclerView, collaboratorsRv, relativeLayout);
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
                        unhighlightView(itemView, position);
                    } else {
                        selected.add(noteList.get(position));
                        highlightView(itemView, position);
                    }
                    mListener.onLongItemClick(position);
                }
            }
            return true;
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
        Note currentItem = noteList.get(position);

        final Collaborator currentUserCollaborator = new Collaborator();
        currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        holder.relativeLayout.setTransitionName(currentItem.getNote_doc_ID());

        if (currentItem.getNoteText() != null)
            holder.noteText.setText(currentItem.getNoteText());
        if (currentItem.getNoteTitle() != null)
            holder.noteTitle.setText(currentItem.getNoteTitle());

        //Setup note background
        if (selected.contains(currentItem)) {
            if (currentItem.getNote_background_color() == null) {
                highlightViewHolder(holder, null);
            } else {
                highlightViewHolder(holder, currentItem.getNote_background_color());
            }

        } else {
            if (currentItem.getNote_background_color() == null) {
                unhighlightViewHolder(holder, null);
            } else {
                unhighlightViewHolder(holder, currentItem.getNote_background_color());
            }
        }

        //Setup collaborators
        if (currentItem.getCollaboratorList() != null && currentItem.getCollaboratorList().size() > 1) {
            holder.collaboratorsRv.setVisibility(View.VISIBLE);
            CollaboratorHomeAdapter collaboratorHomeAdapter = new CollaboratorHomeAdapter(currentItem.getCollaboratorList());
            holder.collaboratorsRv.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));
            holder.collaboratorsRv.setAdapter(collaboratorHomeAdapter);
            holder.collaboratorsRv.setHasFixedSize(true);
            holder.collaboratorsRv.suppressLayout(true);
        } else {
            holder.collaboratorsRv.setVisibility(View.GONE);
        }
    }

    private void populateCheckBoxViewHolder(CheckboxViewHolder holder, int position) {
        Note currentItem = noteList.get(position);

        final Collaborator currentUserCollaborator = new Collaborator();
        currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        holder.relativeLayout.setTransitionName(currentItem.getNote_doc_ID());

        //Setup note background
        if (selected.contains(currentItem)) {
            if (currentItem.getNote_background_color() == null) {
                highlightViewHolder(holder, null);
            } else {
                highlightViewHolder(holder, currentItem.getNote_background_color());
            }

        } else {
            if (currentItem.getNote_background_color() == null) {
                unhighlightViewHolder(holder, null);
            } else {
                unhighlightViewHolder(holder, currentItem.getNote_background_color());
            }
        }

        if (currentItem.getNoteTitle() != null)
            holder.noteTitle.setText(currentItem.getNoteTitle());

        //Setup Checkboxes
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

        //Setup collaborators
        if (currentItem.getCollaboratorList() != null && currentItem.getCollaboratorList().size() > 1) {
            List<Collaborator> collaborators = new ArrayList<>();
            if (currentItem.getCollaboratorList().size() < MAX_HOME_COLLABORATORS_PICTURES) {
                collaborators.addAll(currentItem.getCollaboratorList());
            } else {
                for (int i = 0; i < MAX_HOME_COLLABORATORS_PICTURES; i++) {
                    collaborators.add(currentItem.getCollaboratorList().get(i));
                }
            }

            holder.collaboratorsRv.setVisibility(View.VISIBLE);
            CollaboratorHomeAdapter collaboratorHomeAdapter = new CollaboratorHomeAdapter(collaborators);
            holder.collaboratorsRv.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));
            holder.collaboratorsRv.setAdapter(collaboratorHomeAdapter);
            holder.collaboratorsRv.setHasFixedSize(true);
            holder.collaboratorsRv.suppressLayout(true);

        } else {
            holder.collaboratorsRv.setVisibility(View.GONE);
        }
    }

    public void highlightViewHolder(RecyclerView.ViewHolder holder, String color) {
        if (color == null || color.equals("")) {
            holder.itemView.setBackgroundResource(R.drawable.home_note_selected_note_background);
        } else
            switch (color) {
                case "yellow":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_selected_background_yellow);
                    break;
                case "red":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_selected_background_red);
                    break;
                case "green":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_selected_background_green);
                    break;
                case "blue":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_selected_background_blue);
                    break;
                case "orange":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_selected_background_orange);
                    break;
                case "purple":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_selected_background_purple);
                    break;
            }
    }

    public void unhighlightViewHolder(RecyclerView.ViewHolder holder, String color) {
        if (color == null || color.equals("")) {
            holder.itemView.setBackgroundResource(R.drawable.home_note_background);
        } else
            switch (color) {
                case "yellow":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_background_yellow);
                    break;
                case "red":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_background_red);
                    break;
                case "green":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_background_green);
                    break;
                case "blue":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_background_blue);
                    break;
                case "orange":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_background_orange);
                    break;
                case "purple":
                    holder.itemView.setBackgroundResource(R.drawable.home_note_background_purple);
                    break;
            }
    }

    private void highlightView(View view, int position) {

        //Get current user collaborator background color details for current note
        Collaborator currentUserCollaborator = new Collaborator();
        currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        String color = noteList.get(position).getNote_background_color();

        if (color == null || color.equals("")) {
            view.setBackgroundResource(R.drawable.home_note_selected_note_background);
        } else
            switch (color) {
                case "yellow":
                    view.setBackgroundResource(R.drawable.home_note_selected_background_yellow);
                    break;
                case "red":
                    view.setBackgroundResource(R.drawable.home_note_selected_background_red);
                    break;
                case "green":
                    view.setBackgroundResource(R.drawable.home_note_selected_background_green);
                    break;
                case "blue":
                    view.setBackgroundResource(R.drawable.home_note_selected_background_blue);
                    break;
                case "orange":
                    view.setBackgroundResource(R.drawable.home_note_selected_background_orange);
                    break;
                case "purple":
                    view.setBackgroundResource(R.drawable.home_note_selected_background_purple);
                    break;
            }
    }

    private void unhighlightView(View view, int position) {

        //Get current user collaborator background color details for current note
        Collaborator currentUserCollaborator = new Collaborator();
        currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        String color = noteList.get(position).getNote_background_color();

        if (color == null || color.equals("")) {
            view.setBackgroundResource(R.drawable.home_note_background);
        } else
            switch (color) {
                case "yellow":
                    view.setBackgroundResource(R.drawable.home_note_background_yellow);
                    break;
                case "red":
                    view.setBackgroundResource(R.drawable.home_note_background_red);
                    break;
                case "green":
                    view.setBackgroundResource(R.drawable.home_note_background_green);
                    break;
                case "blue":
                    view.setBackgroundResource(R.drawable.home_note_background_blue);
                    break;
                case "orange":
                    view.setBackgroundResource(R.drawable.home_note_background_orange);
                    break;
                case "purple":
                    view.setBackgroundResource(R.drawable.home_note_background_purple);
                    break;
            }
    }


    public void clearSelected() {
        selected.clear();
//        notifyDataSetChanged();
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charSequenceString = constraint.toString();
                if (charSequenceString.isEmpty()) {
                    noteList = noteListFull;
                } else {
                    List<Note> filteredList = new ArrayList<>();
                    for (Note name : noteListFull) {
                        if (name.getNoteTitle().toLowerCase().contains(charSequenceString.toLowerCase())) {
                            filteredList.add(name);
                        }
                        noteList = filteredList;
                    }

                }
                FilterResults results = new FilterResults();
                results.values = noteList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                noteList = (List<Note>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    @Override
    public long getItemId(int position) {
        return position;
    }
}