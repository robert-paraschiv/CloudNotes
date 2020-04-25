package com.rokudoz.cloudnotes.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

public class CheckableItemAdapter extends RecyclerView.Adapter<CheckableItemAdapter.ViewHolder> {
    private static final String TAG = "CheckableItemAdapter";

    private boolean onBind;

    private OnStartDragListener onStartDragListener;
    private OnItemClickListener onItemClickListener;
    private List<CheckableItem> checkableItemList = new ArrayList<>();


    public interface OnStartDragListener {
        void onStartDrag(int position);
    }

    public interface OnItemClickListener {
        void onCheckClick(int position, boolean isChecked);

        void onTextChanged(int position, String text);

        void onDeleteClick(int position);

        void onEnterPressed(int position);
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public CheckableItemAdapter(List<CheckableItem> checkableItemList) {
        this.checkableItemList = checkableItemList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextInputEditText text;
        MaterialCheckBox checkBox;
        MaterialButton deleteBtn;
        ImageView dragHandle;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(final View itemView) {
            super(itemView);
            this.checkBox = itemView.findViewById(R.id.checkableItem_checkbox);
            this.text = itemView.findViewById(R.id.checkableItem_textInput);
            this.dragHandle = itemView.findViewById(R.id.checkableItem_drag_handle);
            this.deleteBtn = itemView.findViewById(R.id.checkableItem_deleteBtn);

            this.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            onItemClickListener.onDeleteClick(position);
                    }
                }
            });
            this.text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        if (onItemClickListener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                onItemClickListener.onEnterPressed(position);
                            }
                        }
                        handled = true;
                    }
                    return handled;
                }
            });
            this.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CompoundButton) v).isChecked()) {
                        if (onItemClickListener != null) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                onItemClickListener.onCheckClick(position, true);
                            }
                        }

                    } else {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onCheckClick(position, false);
                        }
                    }
                }
            });
            this.text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!onBind) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION & s != null && !s.toString().equals("") && checkableItemList.size() > position)
                            onItemClickListener.onTextChanged(position, s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            this.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION & MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        onStartDragListener.onStartDrag(position);
                    }
                    return false;
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_checkable_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        CheckableItem currentItem = checkableItemList.get(position);

        onBind = true;

        if (currentItem != null && currentItem.getChecked() != null && currentItem.getText() != null) {
            holder.text.setText(currentItem.getText());
            holder.checkBox.setChecked(currentItem.getChecked());
            onBind = false;
        }

        if (currentItem != null && currentItem.getShouldBeFocused() != null && currentItem.getShouldBeFocused()) {
            holder.text.post(new Runnable() {
                @Override
                public void run() {
                    if (holder.text.requestFocus()) {
                        InputMethodManager inputMethodManager = (InputMethodManager) holder.text.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(holder.text, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
            currentItem.setShouldBeFocused(false);
        }
    }


    @Override
    public int getItemCount() {
        return checkableItemList.size();
    }
}
