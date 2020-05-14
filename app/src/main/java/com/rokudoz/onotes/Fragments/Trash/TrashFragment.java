package com.rokudoz.onotes.Fragments.Trash;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.onotes.Adapters.NoteEditsAdapter;
import com.rokudoz.onotes.Adapters.TrashNotesAdapter;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rokudoz.onotes.App.HIDE_BANNER;

public class TrashFragment extends Fragment implements NoteEditsAdapter.OnItemClickListener, TrashNotesAdapter.OnItemClickListener {
    private static final String TAG = "TrashFragment";

    private RecyclerView recyclerView;

    private TrashNotesAdapter noteEditsAdapter;
    private List<Note> noteList = new ArrayList<>();

    private ProgressBar progressBar;

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

        MaterialButton backBtn = view.findViewById(R.id.trashFragment_backBtn);
        MaterialButton emptyTrashBtn = view.findViewById(R.id.trashFragment_emptyTrashBtn);
        recyclerView = view.findViewById(R.id.trashFragment_recyclerView);
        progressBar = view.findViewById(R.id.trashFragment_progressBar);

        //Reset status bar color
        if (getActivity() != null) {
            ColorFunctions colorFunctions = new ColorFunctions();
            colorFunctions.resetStatus_NavigationBar_Colors(getActivity());
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.trashFragment)
                    Navigation.findNavController(view).navigate(TrashFragmentDirections.actionTrashFragmentToHomeFragment());
            }
        });

        emptyTrashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteList.size() > 0) {
                    //Dialog for delete note
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_show_ad, (ViewGroup) view, false);
                    final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
                    MaterialButton confirmBtn = dialogView.findViewById(R.id.dialog_ShowAd_confirmBtn);
                    MaterialButton cancelBtn = dialogView.findViewById(R.id.dialog_ShowAd_cancelBtn);
                    TextView title = dialogView.findViewById(R.id.dialog_ShowAd_title);
                    title.setText("You will not be able to recover these notes after you delete them\nAre you sure you want to delete all notes?");
                    dialog.setContentView(dialogView);

                    confirmBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Delete note
                            dialog.cancel();
                            deleteAllNotes();
                        }
                    });
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                } else {
                    Log.d(TAG, "onClick: trash is empty");
                    Toast.makeText(requireContext(), "Already empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Show Banner Ad
        BannerAdManager bannerAdManager = new BannerAdManager();
        if (getActivity() != null && !HIDE_BANNER) {
            bannerAdManager.showBannerAd(getActivity());
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            params.setMargins(bannerAdManager.convertDpToPixel(requireActivity(), 10)
                    , 0
                    , bannerAdManager.convertDpToPixel(requireActivity(), 10)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 50));

        }

        setUpRecyclerView();
        return view;
    }

    private void deleteAllNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomBottomSheetDialogTheme);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_please_wait);
        final AlertDialog dialog = builder.create();
        dialog.show();


        db.collection("Notes")
                .whereEqualTo("creator_user_email", Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()))
                .whereEqualTo("deleted", true)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        final WriteBatch batch = db.batch();
                        batch.delete(documentSnapshot.getReference());
                        documentSnapshot.getReference().collection("Edits").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                    WriteBatch innerBatch = db.batch();
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        innerBatch.delete(documentSnapshot.getReference());
                                    }
                                    innerBatch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: deleted inner batch");
                                        }
                                    });
                                }
                            }
                        });

                        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (getContext() != null)
                                    Toast.makeText(requireContext(), "Emptied trash", Toast.LENGTH_SHORT).show();
                                noteList.clear();
                                noteEditsAdapter.notifyDataSetChanged();
                                dialog.cancel();
                                if (Navigation.findNavController(view) != null)
                                    if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.trashFragment)
                                        Navigation.findNavController(view).navigate(TrashFragmentDirections.actionTrashFragmentToHomeFragment());
                            }
                        });
                    }
                }
            }
        });


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
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null)
            notesListener = db.collection("Notes")
                    .whereEqualTo("creator_user_email", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                    .whereEqualTo("deleted", true)
                    .orderBy("creation_date", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0 && e == null) {

                                //hide progress bar
                                progressBar.setVisibility(View.GONE);

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