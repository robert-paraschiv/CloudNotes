package com.rokudoz.cloudnotes.Fragments.Trash;

import android.app.ProgressDialog;
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
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.cloudnotes.Adapters.NonCheckableAdapter;
import com.rokudoz.cloudnotes.Fragments.ViewNoteEditFragmentArgs;
import com.rokudoz.cloudnotes.Fragments.ViewNoteEditFragmentDirections;
import com.rokudoz.cloudnotes.Models.CheckableItem;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.List;
import java.util.Objects;

import static com.rokudoz.cloudnotes.App.HIDE_BANNER;

public class ViewTrashNote extends Fragment implements NonCheckableAdapter.OnItemClickListener {
    private static final String TAG = "ViewTrashNote";

    private View view;

    private TextView titleTv, textTv;
    private MaterialButton restoreBtn, backBtn;
    private RecyclerView recyclerView;
    private NonCheckableAdapter mAdapter;
    private int nrOfEdits = 0;

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
        backBtn = view.findViewById(R.id.viewTrashNoteFragment_backBtn);
        recyclerView = view.findViewById(R.id.viewTrashNoteFragment_recyclerView);

        if (getActivity() != null && !HIDE_BANNER) {
            getActivity().findViewById(R.id.bannerAdCard).setVisibility(View.VISIBLE);
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


        return view;
    }

    private void buildRecyclerView(List<CheckableItem> checkableItemList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new NonCheckableAdapter(checkableItemList, 0);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    private void getNote(final String noteID) {
        usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
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
                                        final ProgressDialog progressDialog = new ProgressDialog(getContext());
                                        progressDialog.setTitle("Please wait...");
                                        progressDialog.show();

                                        usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").document(noteID)
                                                .update("deleted", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.cancel();
                                                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.viewTrashNote)
                                                    Navigation.findNavController(view).navigate(ViewTrashNoteDirections.actionViewTrashNoteToHomeFragment());
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
        //do nothing
    }
}