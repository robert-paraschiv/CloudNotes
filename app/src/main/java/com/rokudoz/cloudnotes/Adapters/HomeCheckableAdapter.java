package com.rokudoz.cloudnotes.Adapters;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.List;

public class HomeCheckableAdapter extends RecyclerView.Adapter<HomeCheckableAdapter.ViewHolder> {
    private static final String TAG = "HomeCheckableItemAdapter";
    private OnItemClickListener mListener;
    int position = 0;
    private List<CheckableItem> checkableItemList = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public HomeCheckableAdapter(List<CheckableItem> checkableItemList, int position) {
        this.checkableItemList = checkableItemList;
        this.position = position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView text;
        MaterialCheckBox checkBox;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.checkBox = itemView.findViewById(R.id.homeCheckableItem_checkbox);
            this.text = itemView.findViewById(R.id.homeCheckableItem_textView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_home_checkbox_item, parent, false);
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
