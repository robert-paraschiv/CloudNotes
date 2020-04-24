package com.rokudoz.cloudnotes.Fragments.Trash;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rokudoz.cloudnotes.Adapters.HomePageAdapter;
import com.rokudoz.cloudnotes.Adapters.NoteEditsAdapter;
import com.rokudoz.cloudnotes.Adapters.TrashNotesAdapter;
import com.rokudoz.cloudnotes.Fragments.NoteEditsFragment;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrashFragment extends Fragment implements NoteEditsAdapter.OnItemClickListener, TrashNotesAdapter.OnItemClickListener {
    private static final String TAG = "TrashFragment";

    private MaterialButton backBtn;
    private RecyclerView recyclerView;

    private TrashNotesAdapter noteEditsAdapter;
    private List<Note> noteList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private ListenerRegistration notesListener;
    private View view;


    public TrashFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trash, container, false);

        backBtn = view.findViewById(R.id.trashFragment_backBtn);
        recyclerView = view.findViewById(R.id.trashFragment_recyclerView);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Navigation.findNavController(view).getCurrentDestination().getId() == R.id.trashFragment)
                    Navigation.findNavController(view).navigate(TrashFragmentDirections.actionTrashFragmentToHomeFragment());
            }
        });


        setUpRecyclerView();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (notesListener != null) {
            notesListener.remove();
            notesListener = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            getNotes();
    }

    private void getNotes() {
        noteList.clear();
        noteEditsAdapter.notifyDataSetChanged();
        notesListener = usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes")
                .whereEqualTo("deleted", true)
                .orderBy("creation_date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0 && e == null) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Note note = documentSnapshot.toObject(Note.class);
                                if (note != null) {
                                    note.setNote_doc_ID(documentSnapshot.getId());
                                    if (noteList.contains(note)) {
                                        if (note.getDeleted()) {
                                            noteList.set(noteList.indexOf(note), note);
                                            noteEditsAdapter.notifyItemChanged(noteList.indexOf(note));
                                        } else {
                                            int notePosition = noteList.indexOf(note);
                                            noteList.remove(note);
                                            noteEditsAdapter.notifyItemRemoved(notePosition);
                                        }
                                    } else {
                                        if (note.getDeleted()) {
                                            noteList.add(note);
                                            noteEditsAdapter.notifyItemInserted(noteList.size() - 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void setUpRecyclerView() {
        noteEditsAdapter = new TrashNotesAdapter(getActivity(), noteList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(noteEditsAdapter);
        noteEditsAdapter.setOnItemClickListener(TrashFragment.this);
    }

    @Override
    public void onItemClick(int position) {
        Note note = noteList.get(position);
        if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.trashFragment && note.getNote_doc_ID() != null)
            Navigation.findNavController(view).navigate(TrashFragmentDirections.actionTrashFragmentToViewTrashNote(note.getNote_doc_ID()));
    }
}