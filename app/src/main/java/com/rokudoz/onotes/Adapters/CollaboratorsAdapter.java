package com.rokudoz.onotes.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.R;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CollaboratorsAdapter extends RecyclerView.Adapter<CollaboratorsAdapter.ViewHolder> {
    private static final String TAG = "CollaboratorsAdapter";

    private boolean onBind;

    private OnItemClickListener onItemClickListener;
    private final List<Collaborator> collaboratorList;
    private boolean isOwner = false;


    public interface OnItemClickListener {

        void onTextChanged(int position, String text);

        void onDeleteClick(int position);

        void onEnterPressed(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public CollaboratorsAdapter(List<Collaborator> collaboratorList, boolean isOwner) {
        this.collaboratorList = collaboratorList;
        this.isOwner = isOwner;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final TextInputEditText email;
        final TextView emailTEXTVIEW;
        final MaterialButton deleteBtn;
        final CircleImageView picture;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(final View itemView) {
            super(itemView);
            this.email = itemView.findViewById(R.id.rv_collaborator_email);
            this.deleteBtn = itemView.findViewById(R.id.rv_collaborator_remove);
            this.picture = itemView.findViewById(R.id.rv_collaborator_picture);
            this.emailTEXTVIEW = itemView.findViewById(R.id.rv_collaborator_emailTEXTVIEW);

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
            this.email.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            this.email.setRawInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            this.email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
                }
            });
            this.email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!onBind) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION & s != null && !s.toString().equals("") && collaboratorList.size() > position)
                            onItemClickListener.onTextChanged(position, s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_collaborator_dialog_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        Collaborator currentItem = collaboratorList.get(position);

        onBind = true;

        if (currentItem != null) {
            if (currentItem.getUser_email() != null) {
                holder.email.setText(currentItem.getUser_email());

                if (!currentItem.getUser_email().trim().equals("")) {
                    holder.email.setFocusable(false);
                    holder.email.setVisibility(View.INVISIBLE);
                    holder.emailTEXTVIEW.setVisibility(View.VISIBLE);
                    holder.emailTEXTVIEW.setText(currentItem.getUser_email());
                } else {
                    holder.email.setFocusable(true);
                    holder.email.setVisibility(View.VISIBLE);
                    holder.emailTEXTVIEW.setVisibility(View.INVISIBLE);
                    holder.emailTEXTVIEW.setText("");
                }

                onBind = false;
            }

            String currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

            if (currentItem.getCreator() != null && currentItem.getCreator()) {
                holder.deleteBtn.setVisibility(View.GONE);
            } else if (currentItem.getCreator() != null && !currentItem.getCreator() && currentItem.getUser_email().equals(currentUserEmail) || isOwner) {
                holder.deleteBtn.setVisibility(View.VISIBLE);
            } else if (currentItem.getCreator() != null && !currentItem.getCreator() && !currentItem.getUser_email().equals(currentUserEmail)) {
                holder.deleteBtn.setVisibility(View.GONE);
            }

            if (currentItem.getUser_email().trim().equals("")) {
                holder.deleteBtn.setVisibility(View.VISIBLE);
            }

            if (currentItem.getUser_picture() != null)
                if (!currentItem.getUser_picture().trim().equals("") && !currentItem.getUser_picture().trim().equals("add")) {
                    Glide.with(holder.picture).load(currentItem.getUser_picture()).centerCrop().into(holder.picture);
                } else if (currentItem.getUser_picture().equals("add")) {
                    Glide.with(holder.picture).load(R.drawable.ic_outline_person_24).centerCrop().into(holder.picture);
                }

            if (currentItem.getShouldBeFocused() != null && currentItem.getShouldBeFocused()) {
                holder.email.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder.email.requestFocus()) {
                            InputMethodManager inputMethodManager = (InputMethodManager) holder.email.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(holder.email, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });
                currentItem.setShouldBeFocused(false);
            }
        }
    }


    @Override
    public int getItemCount() {
        return collaboratorList.size();
    }
}

