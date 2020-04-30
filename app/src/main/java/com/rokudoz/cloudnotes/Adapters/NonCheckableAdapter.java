package com.rokudoz.cloudnotes.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.R;

import java.util.List;

public class NonCheckableAdapter extends RecyclerView.Adapter<NonCheckableAdapter.ViewHolder> {
    int position;
    private List<CheckableItem> checkableItemList;

    public NonCheckableAdapter(List<CheckableItem> checkableItemList, int position) {
        this.checkableItemList = checkableItemList;
        this.position = position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView text;
        MaterialCheckBox checkBox;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.checkBox = itemView.findViewById(R.id.homeCheckableItem_checkbox);
            this.text = itemView.findViewById(R.id.homeCheckableItem_textView);
//            itemView.setOnClickListener(this);
        }

//        @Override
//        public void onClick(View v) {
//            mListener.onItemClick(position);
//        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_non_checkable_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        CheckableItem currentItem = checkableItemList.get(position);

        if (currentItem != null && currentItem.getChecked() != null && currentItem.getText() != null) {
            holder.text.setText(currentItem.getText());
            holder.checkBox.setChecked(currentItem.getChecked());
        }
    }


    @Override
    public int getItemCount() {
        return checkableItemList.size();
    }
}
