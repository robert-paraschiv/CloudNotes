package com.rokudoz.onotes.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rokudoz.onotes.Adapters.NoteEditsAdapter;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rokudoz.onotes.App.HIDE_BANNER;

public class NoteEditsFragment extends Fragment implements NoteEditsAdapter.OnItemClickListener {

    private String noteID = "";
    private Integer notePosition;
    private String noteColor = "";
    private Boolean note_has_collaborators = false;
    private View view;

    private ProgressBar progressBar;

    private NoteEditsAdapter noteEditsAdapter;
    private RecyclerView recyclerView;
    private final List<Note> noteList = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentSnapshot mLastQueriedDocument;

    public NoteEditsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_note_edits, container, false);

        progressBar = view.findViewById(R.id.noteEditsFragment_progressBar);

        if (getArguments() != null) {
            NoteEditsFragmentArgs noteEditsFragmentArgs = NoteEditsFragmentArgs.fromBundle(getArguments());
            noteID = noteEditsFragmentArgs.getNoteID();
            noteColor = noteEditsFragmentArgs.getNoteColor();
            note_has_collaborators = noteEditsFragmentArgs.getNoteHasCollaborators();
            notePosition = noteEditsFragmentArgs.getNotePosition();
            getNotes(noteID);
        }
        recyclerView = view.findViewById(R.id.noteEditsFragment_recyclerView);

        //Reset status bar color
        if (getActivity() != null) {
            ColorUtils.resetStatus_NavigationBar_Colors(getActivity());
            hideSoftKeyboard(getActivity());
        }

//        //Show Banner Ad
//        if (getActivity() != null && !HIDE_BANNER) {
//            BannerAdManager bannerAdManager = new BannerAdManager();
//            bannerAdManager.showBannerAd(getActivity());
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
//            params.setMargins(0
//                    , 0
//                    , 0
//                    , bannerAdManager.convertDpToPixel(requireActivity(), 50));
//        }

        MaterialButton backBtn = view.findViewById(R.id.noteEditsFragment_backBtn);
        backBtn.setOnClickListener(v -> {
            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.noteEditsFragment)
                Navigation.findNavController(view).popBackStack();
//                    Navigation.findNavController(view).navigate(NoteEditsFragmentDirections.actionNoteEditsFragmentToEditNoteFragment(noteID, noteColor));
        });

        //If user comes back from another fragment, hide progress bar
        if (noteList != null && noteList.size() > 0)
            progressBar.setVisibility(View.GONE);


        setUpRecyclerView();

        return view;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void setUpRecyclerView() {
        noteEditsAdapter = new NoteEditsAdapter(getActivity(), noteList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(noteEditsAdapter);
        noteEditsAdapter.setOnItemClickListener(NoteEditsFragment.this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1))//Down
                    getNotes(noteID);
            }
        });
    }

    private void getNotes(String noteID) {
        Query notesQuery;
        if (mLastQueriedDocument != null) {
            notesQuery = db.collection("Users")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .collection("Notes")
                    .document(noteID)
                    .collection("Edits")
                    .orderBy("creation_date", Query.Direction.DESCENDING)
                    .startAfter(mLastQueriedDocument).limit(10);
        } else {
            notesQuery = db.collection("Users")
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .collection("Notes")
                    .document(noteID)
                    .collection("Edits")
                    .orderBy("creation_date", Query.Direction.DESCENDING).limit(10);
        }

        notesQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    //hide progress bar
                    progressBar.setVisibility(View.GONE);

                    Note note = documentSnapshot.toObject(Note.class);
                    if (note != null) {
                        note.setNote_doc_ID(documentSnapshot.getId());
                        note.setHas_collaborators(note_has_collaborators);
                        if (noteList.contains(note)) {
                            noteList.set(noteList.indexOf(note), note);
                            noteEditsAdapter.notifyItemChanged(noteList.indexOf(note));
                        } else {
                            noteList.add(note);
                            noteEditsAdapter.notifyItemInserted(noteList.size() - 1);
                        }
                    }
                }

                if (queryDocumentSnapshots.getDocuments().size() != 0) {
                    mLastQueriedDocument = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.getDocuments().size() - 1);
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Note note = noteList.get(position);
        if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.noteEditsFragment)
            Navigation.findNavController(view).navigate(NoteEditsFragmentDirections
                    .actionNoteEditsFragmentToViewNoteEditFragment(noteID, notePosition, note.getNote_doc_ID(), noteColor));
    }
}