package com.rokudoz.onotes.Fragments.Trash;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.material.appbar.MaterialToolbar;
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
import com.rokudoz.onotes.Adapters.HomePageAdapter;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.rokudoz.onotes.App.HIDE_BANNER;

public class TrashFragment extends Fragment implements HomePageAdapter.OnItemClickListener {
    private static final String TAG = "TrashFragment";

    private RecyclerView recyclerView;

    private TextView noNotesTv;
    private HomePageAdapter noteEditsAdapter;
    private List<Note> noteList = new ArrayList<>();
    private ActionMode actionMode;
    private ProgressBar progressBar;
    private Collaborator currentUserCollaborator = new Collaborator();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private ListenerRegistration notesListener;
    private View view;
    private MaterialToolbar materialToolbar;

    public TrashFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_trash, container, false);

        MaterialButton backBtn = view.findViewById(R.id.trashFragment_backBtn);
        MaterialButton emptyTrashBtn = view.findViewById(R.id.trashFragment_emptyTrashBtn);
        recyclerView = view.findViewById(R.id.trashFragment_recyclerView);
        noNotesTv = view.findViewById(R.id.trashFragment_empty);
        progressBar = view.findViewById(R.id.trashFragment_progressBar);
        materialToolbar = view.findViewById(R.id.trashFragment_toolbar);
        //Reset status bar color
        if (getActivity() != null) {
            ColorUtils colorFunctions = new ColorUtils();
            colorFunctions.resetStatus_NavigationBar_Colors(getActivity());
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUserCollaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());

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
                    Toast.makeText(requireContext(), "Your bin is already empty", Toast.LENGTH_SHORT).show();
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

        final WriteBatch batch = db.batch();

        db.collection("Notes")
                .whereEqualTo("creator_user_email", Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()))
                .whereEqualTo("deleted", true)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                    final int notesToDelete = queryDocumentSnapshots.size();
                    final int[] notesDeleted = {0};
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        //Add note to the delete batch
                        batch.delete(documentSnapshot.getReference());
                        documentSnapshot.getReference().collection("Edits").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {

                                    //Add inner note edits to the delete batch
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        batch.delete(documentSnapshot.getReference());
                                    }
                                    notesDeleted[0]++;
                                    if (notesDeleted[0] == notesToDelete) { //finished adding notes to the batch, go commit

                                        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (getContext() != null)
                                                    Toast.makeText(requireContext(), "Emptied trash", Toast.LENGTH_SHORT).show();
                                                //Show No notes Text View
                                                noNotesTv.setVisibility(View.VISIBLE);
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
                                //Hide progress bar
                                progressBar.setVisibility(View.GONE);
                                //Hide No notes Text View
                                noNotesTv.setVisibility(View.GONE);

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
                            } else {
                                //Hide progress bar
                                progressBar.setVisibility(View.GONE);
                                //Show No notes Text View
                                noNotesTv.setVisibility(View.VISIBLE);
                                Log.d(TAG, "onEvent: empty trash bin");
                            }
                        }
                    });
    }

    private void setUpRecyclerView() {
        noteEditsAdapter = new HomePageAdapter(getActivity(), noteList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(noteEditsAdapter);
        noteEditsAdapter.setOnItemClickListener(TrashFragment.this);
    }


    private void deleteSelectedNotes(final List<Note> notesToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomBottomSheetDialogTheme);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_please_wait);
        final AlertDialog dialog = builder.create();
        dialog.show();

        final int[] counter = {0};
        for (final Note note : notesToDelete) {
            db.collection("Notes").document(note.getNote_doc_ID()).collection("Edits").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() > 0) {
                                WriteBatch batch = db.batch();
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    batch.delete(documentSnapshot.getReference());
                                }
                                batch.delete(db.collection("Notes").document(note.getNote_doc_ID()));
                                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Remove note from list
                                        int position = noteList.indexOf(note);
                                        noteList.remove(note);
                                        noteEditsAdapter.notifyItemRemoved(position);

                                        counter[0]++;
                                        //If all selected notes have been deleted
                                        if (counter[0] == notesToDelete.size()) {
                                            Toast.makeText(requireContext(), "Deleted all notes", Toast.LENGTH_SHORT).show();
                                            dialog.cancel();
                                        }
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private void restoreSelectedNotes(final List<Note> notesToRestore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomBottomSheetDialogTheme);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_please_wait);
        final AlertDialog dialog = builder.create();
        dialog.show();

        final int[] counter = {0};
        for (final Note note : notesToRestore) {
            db.collection("Notes").document(note.getNote_doc_ID()).update("deleted", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //Remove note from list
                    int position = noteList.indexOf(note);
                    noteList.remove(note);
                    noteEditsAdapter.notifyItemRemoved(position);

                    counter[0]++;
                    //If all selected notes have been restored
                    if (counter[0] == notesToRestore.size()) {
                        Toast.makeText(requireContext(), "Selected notes have been restored", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                }
            });
        }
    }


    @Override
    public void onItemClick(final int position, final TextView title, final TextView text, final RecyclerView checkboxRv,
                            final RecyclerView collaboratorsRv, final RelativeLayout rootLayout) {
//        Log.d(TAG, "onItemClick: " + position);
        int selected = noteEditsAdapter.getSelected().size();
        if (actionMode == null) {
            Note note = noteList.get(position);
            if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.trashFragment && note.getNote_doc_ID() != null)
                Navigation.findNavController(view).navigate(TrashFragmentDirections.actionTrashFragmentToViewTrashNote(note.getNote_doc_ID()));

        } else {
            if (selected == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle("" + selected);
            }
        }
    }

    @Override
    public void onLongItemClick(int position) {
        int selected = noteEditsAdapter.getSelected().size();
        if (actionMode == null) {
            actionMode = materialToolbar.startActionMode(actionModeCallback);
            if (actionMode != null) {
                actionMode.setTitle("" + selected);
            }
        } else {
            if (selected == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle("" + selected);
            }
        }
    }


    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.trash_action_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete_notes) {
                // Delete btn is clicked, proceed to delete notes
                List<Note> notesToDelete = new ArrayList<>(noteEditsAdapter.getSelected());
                deleteSelectedNotes(notesToDelete);

                mode.finish();
                return true;
            } else if (item.getItemId() == R.id.restore_notes) {
                // Delete btn is clicked, proceed to delete notes
                List<Note> notesToRestore = new ArrayList<>(noteEditsAdapter.getSelected());
                restoreSelectedNotes(notesToRestore);

                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            noteEditsAdapter.clearSelected();
            //Deselect selected items when action bar closes
            for (int i = 0; i < noteList.size(); i++) {
                if (recyclerView.getLayoutManager() != null && recyclerView.getLayoutManager().getChildAt(i) != null) {
                    if (noteList.get(i).getCollaboratorList() != null && noteList.get(i).getCollaboratorList().contains(currentUserCollaborator))
                        if (noteList.get(i).getCollaboratorList().get(noteList.get(i).getCollaboratorList().indexOf(currentUserCollaborator)).getNote_background_color() == null
                                || noteList.get(i).getCollaboratorList().get(noteList.get(i).getCollaboratorList().indexOf(currentUserCollaborator)).getNote_background_color()
                                .equals("")) {
                            Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background);
                        } else {
                            switch (noteList.get(i).getCollaboratorList().get(noteList.get(i).getCollaboratorList().indexOf(currentUserCollaborator)).getNote_background_color()) {
                                case "yellow":
                                    Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_yellow);
                                    break;
                                case "red":
                                    Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_red);
                                    break;
                                case "green":
                                    Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_green);
                                    break;
                                case "blue":
                                    Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_blue);
                                    break;
                                case "orange":
                                    Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_orange);
                                    break;
                                case "purple":
                                    Objects.requireNonNull(recyclerView.getLayoutManager().getChildAt(i)).setBackgroundResource(R.drawable.home_note_background_purple);
                                    break;
                            }
                        }
                }
            }

            actionMode = null;
        }
    };


}