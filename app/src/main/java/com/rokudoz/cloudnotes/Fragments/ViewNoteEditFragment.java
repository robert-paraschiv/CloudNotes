package com.rokudoz.cloudnotes.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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

public class ViewNoteEditFragment extends Fragment {

    private View view;

    private TextView titleTv, textTv;
    private MaterialButton restoreBtn;
    private RecyclerView recyclerView;
    private int nrOfEdits = 0;

    String noteID = "";
    String noteColor = "";
    String note_edit_ID = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");


    public ViewNoteEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_note_edit, container, false);

        ScrollView textScrollView = view.findViewById(R.id.viewNoteEditFragment_textScrollView);
        titleTv = view.findViewById(R.id.viewNoteEditFragment_title);
        textTv = view.findViewById(R.id.viewNoteEditFragment_text);
        restoreBtn = view.findViewById(R.id.viewNoteEditFragment_restoreBtn);
        MaterialButton backBtn = view.findViewById(R.id.viewNoteEditFragment_backBtn);
        recyclerView = view.findViewById(R.id.viewNoteEditFragment_recyclerView);


        //Reset status bar color
        if (getActivity() != null) {
            ColorFunctions colorFunctions = new ColorFunctions();
            colorFunctions.resetStatus_NavigationBar_Colors(getActivity());
        }

        //Show Banner Ad
        if (getActivity() != null && !HIDE_BANNER) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.showBannerAd(getActivity());
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            params.setMargins(bannerAdManager.convertDpToPixel(requireActivity(), 8)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 8)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 8)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 50));

            RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams) textScrollView.getLayoutParams();
            textParams.setMargins(bannerAdManager.convertDpToPixel(requireActivity(), 16)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 8)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 16)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 50));
        }

        if (getArguments() != null) {
            ViewNoteEditFragmentArgs viewNoteEditFragmentArgs = ViewNoteEditFragmentArgs.fromBundle(getArguments());
            noteID = viewNoteEditFragmentArgs.getNoteID();
            note_edit_ID = viewNoteEditFragmentArgs.getNoteEditID();
            noteColor = viewNoteEditFragmentArgs.getNoteColor();
            getNote(noteID);
        }


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.viewNoteEditFragment)
                    Navigation.findNavController(view).popBackStack();
            }
        });


        return view;
    }

    private void buildRecyclerView(List<CheckableItem> checkableItemList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        NonCheckableAdapter mAdapter = new NonCheckableAdapter(checkableItemList, 0);
        recyclerView.setAdapter(mAdapter);
    }

    private void getNote(final String noteID) {
        db.collection("Notes").document(noteID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null && e == null) {
                    final Note originalNote = documentSnapshot.toObject(Note.class);
                    if (originalNote != null) {
                        originalNote.setNote_doc_ID(documentSnapshot.getId());
                        if (originalNote.getNumber_of_edits() != null)
                            nrOfEdits = originalNote.getNumber_of_edits();

                        db.collection("Notes").document(noteID)
                                .collection("Edits").document(note_edit_ID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                                                note.setNumber_of_edits(nrOfEdits + 1);

                                                note.setCollaboratorList(originalNote.getCollaboratorList());
                                                note.setLast_edited_by_user(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
                                                note.setUsers(originalNote.getUsers());
                                                note.setEdited(true);
                                                note.setEdit_type("Restored");
                                                note.setCreation_date(null);
                                                WriteBatch batch = db.batch();
                                                batch.set(db.collection("Notes").document(noteID), note);
                                                batch.set(db.collection("Notes").document(noteID)
                                                        .collection("Edits").document(), note);
                                                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Restored note successfully", Toast.LENGTH_SHORT).show();
                                                        dialog.cancel();
                                                        if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.viewNoteEditFragment)
                                                            Navigation.findNavController(view).navigate(ViewNoteEditFragmentDirections
                                                                    .actionViewNoteEditFragmentToEditNoteFragment(noteID, noteColor));
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
            }
        });

    }

}