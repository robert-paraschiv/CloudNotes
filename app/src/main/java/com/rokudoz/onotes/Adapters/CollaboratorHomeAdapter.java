package com.rokudoz.onotes.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CollaboratorHomeAdapter extends RecyclerView.Adapter<CollaboratorHomeAdapter.ViewHolder> {
    private List<Collaborator> collaboratorList;

    public CollaboratorHomeAdapter(List<Collaborator> collaboratorList) {
        this.collaboratorList = collaboratorList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView picture;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.picture = itemView.findViewById(R.id.rv_collaborator_home_picture);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_collaborator_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Collaborator currentItem = collaboratorList.get(position);

        if (currentItem != null && currentItem.getUser_picture() != null) {
            if (!currentItem.getUser_picture().trim().equals("") && !currentItem.getUser_picture().equals("add")) {
                Glide.with(holder.picture).load(currentItem.getUser_picture()).centerCrop().into(holder.picture);
            }
        }
    }


    @Override
    public int getItemCount() {
        return collaboratorList.size();
    }
}