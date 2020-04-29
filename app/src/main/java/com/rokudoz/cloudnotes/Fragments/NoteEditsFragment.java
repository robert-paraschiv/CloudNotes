package com.rokudoz.cloudnotes.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rokudoz.cloudnotes.Adapters.NoteEditsAdapter;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;
import com.rokudoz.cloudnotes.Utils.BannerAdManager;
import com.rokudoz.cloudnotes.Utils.ColorFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rokudoz.cloudnotes.App.HIDE_BANNER;

public class NoteEditsFragment extends Fragment implements NoteEditsAdapter.OnItemClickListener {
    private static final String TAG = "NoteEditsFragment";

    private String noteID = "";
    private String noteColor = "";
    private View view;

    private NoteEditsAdapter noteEditsAdapter;
    private RecyclerView recyclerView;
    private List<Note> noteList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    public NoteEditsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_note_edits, container, false);

        if (getArguments() != null) {
            NoteEditsFragmentArgs noteEditsFragmentArgs = NoteEditsFragmentArgs.fromBundle(getArguments());
            noteID = noteEditsFragmentArgs.getNoteID();
            noteColor = noteEditsFragmentArgs.getNoteColor();
            getNotes(noteID);
        }
        recyclerView = view.findViewById(R.id.noteEditsFragment_recyclerView);

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
            params.setMargins(0
                    , 0
                    , 0
                    , bannerAdManager.convertDpToPixel(requireActivity(), 50));
        }

        MaterialButton backBtn = view.findViewById(R.id.noteEditsFragment_backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.noteEditsFragment)
                    Navigation.findNavController(view).navigate(NoteEditsFragmentDirections.actionNoteEditsFragmentToEditNoteFragment(noteID, noteColor));
            }
        });


        setUpRecyclerView();

        return view;
    }

    private void setUpRecyclerView() {
        noteEditsAdapter = new NoteEditsAdapter(getActivity(), noteList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(noteEditsAdapter);
        noteEditsAdapter.setOnItemClickListener(NoteEditsFragment.this);
    }

    private void getNotes(String noteID) {
        usersRef.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("Notes").document(noteID).collection("Edits")
                .orderBy("creation_date", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            note.setNote_doc_ID(documentSnapshot.getId());
                            if (noteList.contains(note)) {
                                noteList.set(noteList.indexOf(note), note);
                                noteEditsAdapter.notifyItemChanged(noteList.indexOf(note));
                            } else {
                                noteList.add(note);
                                noteEditsAdapter.notifyItemInserted(noteList.size() - 1);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Note note = noteList.get(position);
        if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.noteEditsFragment)
            Navigation.findNavController(view).navigate(NoteEditsFragmentDirections
                    .actionNoteEditsFragmentToViewNoteEditFragment(noteID, note.getNote_doc_ID(), noteColor));
    }
}