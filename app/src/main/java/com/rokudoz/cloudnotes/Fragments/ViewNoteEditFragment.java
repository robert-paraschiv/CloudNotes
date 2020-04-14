package com.rokudoz.cloudnotes.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.rokudoz.cloudnotes.Adapters.HomeCheckableAdapter;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.List;

public class ViewNoteEditFragment extends Fragment implements HomeCheckableAdapter.OnItemClickListener {
    private static final String TAG = "ViewNoteEditFragment";

    private View view;

    private TextView titleTv, textTv;
    private MaterialButton restoreBtn, backBtn;
    private RecyclerView recyclerView;
    private HomeCheckableAdapter mAdapter;

    String noteID = "";
    String note_edit_ID = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");


    public ViewNoteEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_note_edit, container, false);

        titleTv = view.findViewById(R.id.viewNoteEditFragment_title);
        textTv = view.findViewById(R.id.viewNoteEditFragment_text);
        restoreBtn = view.findViewById(R.id.viewNoteEditFragment_restoreBtn);
        backBtn = view.findViewById(R.id.viewNoteEditFragment_backBtn);
        recyclerView = view.findViewById(R.id.viewNoteEditFragment_recyclerView);

        if (getArguments() != null) {
            ViewNoteEditFragmentArgs viewNoteEditFragmentArgs = ViewNoteEditFragmentArgs.fromBundle(getArguments());
            noteID = viewNoteEditFragmentArgs.getNoteID();
            note_edit_ID = viewNoteEditFragmentArgs.getNoteEditID();
            getNote(noteID);
        }


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(ViewNoteEditFragmentDirections.actionViewNoteEditFragmentToNoteEditsFragment(noteID));
            }
        });


        return view;
    }

    private void buildRecyclerView(List<CheckableItem> checkableItemList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new HomeCheckableAdapter(checkableItemList, 0);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    private void getNote(final String noteID) {
        usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
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
                                note.setEdited(true);
                                usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes")
                                        .document(noteID).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Restored note successfully", Toast.LENGTH_SHORT).show();
                                        Navigation.findNavController(view).navigate(ViewNoteEditFragmentDirections
                                                .actionViewNoteEditFragmentToEditNoteFragment(noteID));
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        //do nothing, but crashes if not included
    }
}