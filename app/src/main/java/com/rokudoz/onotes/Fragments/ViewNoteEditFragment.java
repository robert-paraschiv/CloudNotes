package com.rokudoz.onotes.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.onotes.Adapters.NonCheckableAdapter;
import com.rokudoz.onotes.Models.CheckableItem;
import com.rokudoz.onotes.Models.Collaborator;
import com.rokudoz.onotes.Models.Note;
import com.rokudoz.onotes.R;
import com.rokudoz.onotes.Utils.BannerAdManager;
import com.rokudoz.onotes.Utils.ColorUtils;
import com.rokudoz.onotes.Utils.LastEdit;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.rokudoz.onotes.App.HIDE_BANNER;

public class ViewNoteEditFragment extends Fragment {

    private View view;

    private TextView titleTv, textTv;
    private MaterialButton restoreBtn;
    private RecyclerView recyclerView;
    private int nrOfEdits = 0;

    private ProgressBar progressBar;
    ScrollView textScrollView;
    //Creator layout
    LinearLayout creatorLayout;
    CircleImageView creatorPicture;
    TextView creatorEmail, createdTimestamp;
    boolean hasCollaborators = false;


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

        textScrollView = view.findViewById(R.id.viewNoteEditFragment_textScrollView);
        titleTv = view.findViewById(R.id.viewNoteEditFragment_title);
        textTv = view.findViewById(R.id.viewNoteEditFragment_text);
        restoreBtn = view.findViewById(R.id.viewNoteEditFragment_restoreBtn);
        MaterialButton backBtn = view.findViewById(R.id.viewNoteEditFragment_backBtn);
        recyclerView = view.findViewById(R.id.viewNoteEditFragment_recyclerView);

        progressBar = view.findViewById(R.id.viewNoteEditFragment_progressBar);

        createdTimestamp = view.findViewById(R.id.viewNoteEditFragment_createdTimestamp);
        creatorLayout = view.findViewById(R.id.viewNoteEditFragment_creatorLayout);
        creatorPicture = view.findViewById(R.id.viewNoteEditFragment_creatorPicture);
        creatorEmail = view.findViewById(R.id.viewNoteEditFragment_creatorEmail);

        //Reset status bar color
        if (getActivity() != null) {
            ColorUtils.resetStatus_NavigationBar_Colors(getActivity());
        }

        //Show Banner Ad
        if (getActivity() != null && !HIDE_BANNER) {
            BannerAdManager bannerAdManager = new BannerAdManager();
            bannerAdManager.showBannerAd(getActivity());
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
            params.setMargins(bannerAdManager.convertDpToPixel(requireActivity(), 8)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 8)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 8)
                    , bannerAdManager.convertDpToPixel(requireActivity(), 50));

            LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) textScrollView.getLayoutParams();
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

                        //hide progress bar
                        progressBar.setVisibility(View.GONE);
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
                                            textScrollView.setVisibility(View.GONE);
                                            if (note.getCheckableItemList() != null)
                                                buildRecyclerView(note.getCheckableItemList());
                                        } else {
                                            recyclerView.setVisibility(View.GONE);
                                        }

                                        if (note.getCollaboratorList() != null && note.getCollaboratorList().size() > 1) {
                                            hasCollaborators = true;
                                            creatorLayout.setVisibility(View.VISIBLE);

                                            String userPictureUrl = "";
                                            String userName = "";
                                            for (int i = 0; i < note.getCollaboratorList().size(); i++) {
                                                if (note.getCollaboratorList().get(i).getUser_email().equals(note.getLast_edited_by_user())) {
                                                    userPictureUrl = note.getCollaboratorList().get(i).getUser_picture();
                                                    Glide.with(creatorPicture).load(userPictureUrl).centerCrop().into(creatorPicture);
                                                    if (note.getCollaboratorList().get(i).getUser_name() == null) {
                                                        creatorEmail.setText(note.getLast_edited_by_user());
                                                    } else {
                                                        userName = note.getCollaboratorList().get(i).getUser_name();
                                                        creatorEmail.setText(userName);
                                                    }
                                                    break;
                                                }
                                            }

                                            if (note.getCreation_date() != null) {
                                                LastEdit lastEdit = new LastEdit();
                                                Date date = note.getCreation_date();
                                                createdTimestamp.setText(lastEdit.getLastEdit(date.getTime()));
                                            }

                                        } else {
                                            hasCollaborators = false;
                                            creatorLayout.setVisibility(View.GONE);
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
                                                        if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId()
                                                                == R.id.viewNoteEditFragment) {
                                                            Collaborator collaborator = new Collaborator();
                                                            collaborator.setUser_email(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                                            Navigation.findNavController(view).navigate(ViewNoteEditFragmentDirections.
                                                                    actionViewNoteEditFragmentToEditNoteFragment(noteID,
                                                                            originalNote.getNote_background_color(),
                                                                            originalNote.getNote_position(),
                                                                            null
                                                                    ));
                                                        }

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