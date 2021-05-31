package com.rokudoz.onotes.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.R;

import java.util.List;

public class CheckableItemAdapter extends RecyclerView.Adapter<CheckableItemAdapter.ViewHolder> {
    private static final String TAG = "CheckableItemAdapter";

    private boolean onBind;

    private OnStartDragListener onStartDragListener;
    private OnItemClickListener onItemClickListener;
    private final List<CheckableItem> checkableItemList;


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

        final TextInputEditText text;
        final MaterialCheckBox checkBox;
        final MaterialButton deleteBtn;
        final ImageView dragHandle;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(final View itemView) {
            super(itemView);
            this.checkBox = itemView.findViewById(R.id.checkableItem_checkbox);
            this.text = itemView.findViewById(R.id.checkableItem_textInput);
            this.dragHandle = itemView.findViewById(R.id.checkableItem_drag_handle);
            this.deleteBtn = itemView.findViewById(R.id.checkableItem_deleteBtn);

            this.deleteBtn.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        onItemClickListener.onDeleteClick(position);
                }
            });
            this.text.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            this.text.setRawInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            this.text.setOnEditorActionListener((v, actionId, event) -> {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onEnterPressed(position);
                        }
                    }
                    handled = true;
                }
                return handled;
            });
            this.checkBox.setOnClickListener(v -> {
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

            this.dragHandle.setOnTouchListener((v, event) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION & event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    onStartDragListener.onStartDrag(position);
                }
                return false;
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        CheckableItem currentItem = checkableItemList.get(position);

        onBind = true;

        if (currentItem != null && currentItem.getChecked() != null && currentItem.getText() != null) {
            holder.text.setText(currentItem.getText());
            holder.checkBox.setChecked(currentItem.getChecked());
            onBind = false;
        }

        if (currentItem != null && currentItem.getShouldBeFocused() != null && currentItem.getShouldBeFocused()) {
            holder.text.post(() -> {
                if (holder.text.requestFocus()) {
                    InputMethodManager inputMethodManager = (InputMethodManager) holder.text.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(holder.text, InputMethodManager.SHOW_IMPLICIT);
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
