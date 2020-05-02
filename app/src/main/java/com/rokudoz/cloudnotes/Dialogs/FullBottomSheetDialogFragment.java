package com.rokudoz.cloudnotes.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.rokudoz.cloudnotes.Adapters.CollaboratorsAdapter;
import com.rokudoz.cloudnotes.Fragments.EditNoteFragmentDirections;
import com.rokudoz.cloudnotes.Models.Collaborator;
import com.rokudoz.cloudnotes.Models.User;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FullBottomSheetDialogFragment extends BottomSheetDialogFragment implements CollaboratorsAdapter.OnItemClickListener {
    private static final String TAG = "FullBottomSheetDialogFr";

    private ExampleDialogListener listener;
    private int backgroundColor;
    private List<Collaborator> collaboratorList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CollaboratorsAdapter mAdapter;
    private boolean isOwner = false;

    public FullBottomSheetDialogFragment(int backgroundColor, List<Collaborator> collaboratorList,boolean isOwner) {
        this.backgroundColor = backgroundColor;
        this.collaboratorList = collaboratorList;
        this.isOwner=isOwner;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);

        //Don't allow dialog to dim background (status bar, nav bar etc)
        Objects.requireNonNull(dialog.getWindow()).setDimAmount(0);

        final View view = getLayoutInflater().inflate(R.layout.dialog_collaborators, null);
        recyclerView = view.findViewById(R.id.dialog_collaborators_rv);

        buildRecyclerView();

        view.setBackgroundColor(backgroundColor);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });

        MaterialButton saveBtn = view.findViewById(R.id.dialog_collaborators_saveBtn);
        MaterialButton discardBtn = view.findViewById(R.id.dialog_collaborators_backBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getCollaborators(collaboratorList);
                dialog.dismiss();
            }
        });
        discardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Dialog for discard changes
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, (ViewGroup) view, false);
                final Dialog dialogConfirm = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
                MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
                MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
                TextView title = dialogView.findViewById(R.id.dialog_ShowAd_title);
                title.setText("Are you sure you want to discard changes?");
                title.setTextColor(ContextCompat.getColor(requireContext(), R.color.dialog_please_wait_text_color));
                dialogConfirm.setContentView(dialogView);

                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Delete note
                        collaboratorList.clear();
                        dialogConfirm.cancel();
                        dialog.dismiss();

                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogConfirm.cancel();
                    }
                });

                dialogConfirm.show();
            }
        });

        dialog.setContentView(view);

        return dialog;
    }

    private void buildRecyclerView() {
        mAdapter = new CollaboratorsAdapter(collaboratorList,isOwner);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        Log.d(TAG, "buildRecyclerView: " + collaboratorList.toString());

        collaboratorList.add(new Collaborator("", "add", false));
        mAdapter.notifyItemInserted(collaboratorList.size() - 1);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            Fragment fragment = getTargetFragment();
            Activity activity = getActivity();
            if (fragment != null) {
                listener = (ExampleDialogListener) fragment;
            } else {
                listener = (ExampleDialogListener) activity;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

            int windowHeight = getWindowHeight();
            if (layoutParams != null) {
                layoutParams.height = windowHeight;
            }
            bottomSheet.setLayoutParams(layoutParams);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) requireContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    public void onTextChanged(int position, String text) {
        collaboratorList.get(position).setUser_email(text);
    }

    @Override
    public void onDeleteClick(int position) {
        collaboratorList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onEnterPressed(int position) {
        Collaborator collaborator = new Collaborator("", "add", false);
        collaborator.setShouldBeFocused(true);
        collaboratorList.add(position + 1, collaborator);
        mAdapter.notifyItemInserted(position + 1);
    }

    public interface ExampleDialogListener {
        void getCollaborators(List<Collaborator> collaboratorList);
    }
}