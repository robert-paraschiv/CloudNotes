package com.rokudoz.cloudnotes.Fragments.Trash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.cloudnotes.Adapters.NonCheckableAdapter;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.BannerAdManager;
import com.rokudoz.cloudnotes.Utils.ColorFunctions;

import java.util.List;
import java.util.Objects;

import static com.rokudoz.cloudnotes.App.HIDE_BANNER;

public class ViewTrashNote extends Fragment {
    private static final String TAG = "ViewTrashNote";

    private View view;

    private TextView titleTv, textTv;
    private MaterialButton restoreBtn;
    private RecyclerView recyclerView;

    String noteID = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");


    public ViewTrashNote() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_trash_note, container, false);

        titleTv = view.findViewById(R.id.viewTrashNoteFragment_title);
        textTv = view.findViewById(R.id.viewTrashNoteFragment_text);
        restoreBtn = view.findViewById(R.id.viewTrashNoteFragment_restoreBtn);
        MaterialButton backBtn = view.findViewById(R.id.viewTrashNoteFragment_backBtn);
        MaterialButton deleteBtn = view.findViewById(R.id.viewTrashNoteFragment_deleteBtn);
        recyclerView = view.findViewById(R.id.viewTrashNoteFragment_recyclerView);

        //Reset status bar color
        if (getActivity() != null) {
            ColorFunctions colorFunctions = new ColorFunctions();
            colorFunctions.resetStatus_NavigationBar_Colors(getActivity());
        }

        //Show Banner Ad
        if (getActivity() != null && !HIDE_BANNER) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.showBannerAd(getActivity());
        }

        if (getArguments() != null) {
            ViewTrashNoteArgs viewTrashNoteArgs = ViewTrashNoteArgs.fromBundle(getArguments());
            noteID = viewTrashNoteArgs.getNoteID();
            getNote(noteID);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.viewTrashNote)
                    Navigation.findNavController(view).navigate(ViewTrashNoteDirections.actionViewTrashNoteToTrashFragment());
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                //Dialog for delete note
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, (ViewGroup) view, false);
                final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
                MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
                MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
                TextView title = dialogView.findViewById(R.id.dialog_ShowAd_title);
                title.setText("Are you sure you want to delete this note forever?");
                dialog.setContentView(dialogView);

                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Delete note
                        final WriteBatch batch = db.batch();
                        usersRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection("Notes").document(noteID)
                                .collection("Edits").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        batch.delete(documentSnapshot.getReference());
                                    }
                                    batch.delete(usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Notes").document(noteID));
                                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), "Deleted note", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "onSuccess: Deleted note");
                                            hideSoftKeyboard(requireActivity());
                                            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId()
                                                    == R.id.viewTrashNote)
                                                Navigation.findNavController(view).navigate(ViewTrashNoteDirections.actionViewTrashNoteToTrashFragment());
                                        }
                                    });
                                }
                            }
                        });

                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                dialog.show();

            }
        });


        return view;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void buildRecyclerView(List<CheckableItem> checkableItemList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        NonCheckableAdapter mAdapter = new NonCheckableAdapter(checkableItemList, 0);
        recyclerView.setAdapter(mAdapter);
    }

    private void getNote(final String noteID) {
        usersRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).collection("Notes").document(noteID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && e == null) {
                            final Note note = documentSnapshot.toObject(Note.class);
                            if (note != null) {
                                note.setNote_doc_ID(documentSnapshot.getId());
                                if (note.getNoteTitle() != null)
                                    titleTv.setText(note.getNoteTitle());
                                if (note.getNoteText() != null)
                                    textTv.setText(note.getNoteText());

                                if (note.getNoteType() != null && note.getNoteType().equals("checkbox")) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    textTv.setVisibility(View.INVISIBLE);
                                    if (note.getCheckableItemList() != null)
                                        buildRecyclerView(note.getCheckableItemList());
                                } else {
                                    recyclerView.setVisibility(View.INVISIBLE);
                                }

                                restoreBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View v) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomBottomSheetDialogTheme);
                                        builder.setCancelable(false);
                                        builder.setView(R.layout.dialog_please_wait);
                                        final AlertDialog dialog = builder.create();
                                        dialog.show();

                                        usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
                                                .update("deleted", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.cancel();
                                                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.viewTrashNote)
                                                    Navigation.findNavController(view).navigate(ViewTrashNoteDirections.actionViewTrashNoteToTrashFragment());
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    }
                });

    }

}