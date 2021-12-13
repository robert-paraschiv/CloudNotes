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
import java.util.List;
import java.util.Objects;

import static com.rokudoz.onotes.App.MAX_HOME_CHECKBOX_NUMBER;

public class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final String TAG = "HomePageAdapter";
    private OnItemClickListener mListener;
    private List<Note> noteList;
    private final List<Note> selected = new ArrayList<>();
    private List<Note> noteListFull;
    private Boolean firstStart = true;
    private final Context mContext;

    private static final int TEXT_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;


    public interface OnItemClickListener {
        void onItemClick(int position, TextView title, TextView text, RecyclerView checkboxRv, RecyclerView collaboratorsRv, View rootLayout);

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

        final TextView noteTitle;
        final TextView noteText;
        final RecyclerView collaboratorsRv;
        final RelativeLayout relativeLayout;

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
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.size() > 0) {
                        if (selected.contains(noteList.get(position))) {
                            selected.remove(noteList.get(position));
                            setItemBackgroundColor(itemView, position, false, null);
                        } else {
                            selected.add(noteList.get(position));
                            setItemBackgroundColor(itemView, position, true, null);
                        }
                    }

                    mListener.onItemClick(position, noteTitle, noteText, null, collaboratorsRv, relativeLayout);
                }
            }


        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.contains(noteList.get(position))) {
                        selected.remove(noteList.get(position));
                        setItemBackgroundColor(itemView, position, false, null);
                    } else {
                        selected.add(noteList.get(position));
                        setItemBackgroundColor(itemView, position, true, null);
                    }
                    mListener.onLongItemClick(position);
                }
            }
            return true;
        }
    }

    public class CheckboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        final TextView noteTitle;
        final RecyclerView recyclerView;
        final RecyclerView collaboratorsRv;
        final RelativeLayout relativeLayout;

        public CheckboxViewHolder(final View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.rv_home_checkboxNote_TitleTV);
            this.recyclerView = itemView.findViewById(R.id.rv_home_checkboxNote_recyclerView);
            this.collaboratorsRv = itemView.findViewById(R.id.rv_home_checkboxNote_collaboratorsRV);
            this.relativeLayout = itemView.findViewById(R.id.rv_home_note_rootLayout);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            recyclerView.setOnClickListener(v -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (selected.size() > 0) {
                            if (selected.contains(noteList.get(position))) {
                                selected.remove(noteList.get(position));
                                setItemBackgroundColor(itemView, position, false, null);
                            } else {
                                selected.add(noteList.get(position));
                                setItemBackgroundColor(itemView, position, true, null);
                            }
                        }

                        mListener.onItemClick(position, noteTitle, null, recyclerView, collaboratorsRv, relativeLayout);
                    }
                }
            });

            recyclerView.setOnLongClickListener(v -> {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (selected.contains(noteList.get(position))) {
                            selected.remove(noteList.get(position));
                            setItemBackgroundColor(itemView, position, false, null);
                        } else {
                            selected.add(noteList.get(position));
                            setItemBackgroundColor(itemView, position, true, null);
                        }
                        mListener.onLongItemClick(position);
                    }
                }
                return true;
            });
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.size() > 0) {
                        if (selected.contains(noteList.get(position))) {
                            selected.remove(noteList.get(position));
                            setItemBackgroundColor(itemView, position, false, null);
                        } else {
                            selected.add(noteList.get(position));
                            setItemBackgroundColor(itemView, position, true, null);
                        }
                    }
                    mListener.onItemClick(position, noteTitle, null, recyclerView, collaboratorsRv, relativeLayout);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null) {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (selected.contains(noteList.get(position))) {
                        selected.remove(noteList.get(position));
                        setItemBackgroundColor(itemView, position, false, null);
                    } else {
                        selected.add(noteList.get(position));
                        setItemBackgroundColor(itemView, position, true, null);
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
        currentUserCollaborator.setUser_email(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        holder.relativeLayout.setTransitionName("note_home_rootLayout" + currentItem.getNote_doc_ID());

        holder.noteTitle.setTransitionName("note_home_title" + currentItem.getNote_doc_ID());
        holder.noteText.setTransitionName("note_home_text" + currentItem.getNote_doc_ID());
        holder.collaboratorsRv.setTransitionName("note_home_collaborators" + currentItem.getNote_doc_ID());

        Log.d(TAG, "populateTextViewHolder: title " + currentItem.getNoteTitle() + " id " + currentItem.getNote_doc_ID());

        if (currentItem.getNoteText() != null)
            holder.noteText.setText(currentItem.getNoteText());
        if (currentItem.getNoteTitle() != null)
            holder.noteTitle.setText(currentItem.getNoteTitle());

        //Setup note background
        setItemBackgroundColor(holder.itemView, position, selected.contains(currentItem), null);

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
        currentUserCollaborator.setUser_email(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        holder.relativeLayout.setTransitionName("note_home_rootLayout" + currentItem.getNote_doc_ID());
        holder.noteTitle.setTransitionName("note_home_title" + currentItem.getNote_doc_ID());
        holder.recyclerView.setTransitionName("note_home_checkbox" + currentItem.getNote_doc_ID());
        holder.collaboratorsRv.setTransitionName("note_home_collaborators" + currentItem.getNote_doc_ID());

        Log.d(TAG, "populateCheckBoxViewHolder: title " + currentItem.getNoteTitle() + " id " + currentItem.getNote_doc_ID());

        //Setup note background
        setItemBackgroundColor(holder.itemView, position, selected.contains(currentItem), null);

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


    public void setItemBackgroundColor(View view, int position, boolean highlight, String color) {
        //Get current user collaborator background color details for current note
        Collaborator currentUserCollaborator = new Collaborator();
        currentUserCollaborator.setUser_email(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        if (color == null)
            color = noteList.get(position).getNote_background_color();

        if (color == null || color.equals("")) {
            if (highlight)
                view.setBackgroundResource(R.drawable.home_note_selected_note_background);
            else
                view.setBackgroundResource(R.drawable.home_note_background);
        } else
            switch (color) {
                case "yellow":
                    if (highlight)
                        view.setBackgroundResource(R.drawable.home_note_selected_background_yellow);
                    else
                        view.setBackgroundResource(R.drawable.home_note_background_yellow);
                    break;
                case "red":
                    if (highlight)
                        view.setBackgroundResource(R.drawable.home_note_selected_background_red);
                    else
                        view.setBackgroundResource(R.drawable.home_note_background_red);
                    break;
                case "green":
                    if (highlight)
                        view.setBackgroundResource(R.drawable.home_note_selected_background_green);
                    else
                        view.setBackgroundResource(R.drawable.home_note_background_green);
                    break;
                case "blue":
                    if (highlight)
                        view.setBackgroundResource(R.drawable.home_note_selected_background_blue);
                    else
                        view.setBackgroundResource(R.drawable.home_note_background_blue);
                    break;
                case "orange":
                    if (highlight)
                        view.setBackgroundResource(R.drawable.home_note_selected_background_orange);
                    else
                        view.setBackgroundResource(R.drawable.home_note_background_orange);
                    break;
                case "purple":
                    if (highlight)
                        view.setBackgroundResource(R.drawable.home_note_selected_background_purple);
                    else
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
                if (firstStart)
                    firstStart = false;
                else
                    notifyDataSetChanged();
            }
        };
    }

    //This is used in order to get the proper note when the recyclerview list has been filtered by the search view
    public Note getNote(int position) {
        return noteList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }
}