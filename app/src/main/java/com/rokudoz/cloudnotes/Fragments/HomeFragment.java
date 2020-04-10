package com.rokudoz.cloudnotes.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.rokudoz.cloudnotes.Adapters.StaggeredRecyclerViewAdapter;
import com.rokudoz.cloudnotes.LoginActivity;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.Models.User;
import com.rokudoz.cloudnotes.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment implements StaggeredRecyclerViewAdapter.OnItemClickListener {
    private static final String TAG = "HomeFragment";

    private View view;

    private StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;
    private List<Note> noteList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ListenerRegistration notesListener;

    private CircleImageView userPicture;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);


        userPicture = view.findViewById(R.id.homeFragment_userImage);

        FloatingActionButton addNewNoteBtn = view.findViewById(R.id.homeFragment_addNoteFab);
        addNewNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToNewNoteFragment());
            }
        });

        buildRecyclerView();
        setupFirebaseAuth();

        return view;
    }

    private void buildRecyclerView() {
        RecyclerView recyclerView = view.findViewById(R.id.homeFragment_recyclerView);
        staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(getActivity(), noteList);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        staggeredRecyclerViewAdapter.setOnItemClickListener(HomeFragment.this);
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    private void getNotes(String uid) {
        usersRef.document(uid).collection("Notes").orderBy("creation_date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null && e == null) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Note note = documentSnapshot.toObject(Note.class);
                                if (note != null) {
                                    note.setNote_doc_ID(documentSnapshot.getId());
                                    if (!noteList.contains(note)) {
                                        noteList.add(note);
                                        staggeredRecyclerViewAdapter.notifyItemInserted(noteList.size() - 1);
                                    } else {
                                        noteList.set(noteList.indexOf(note), note);
                                        staggeredRecyclerViewAdapter.notifyItemChanged(noteList.indexOf(note));
                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    private void getUserInfo() {
        usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e == null && documentSnapshot != null) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getUser_name() != null) {
                        Log.d(TAG, "onEvent: " + user.getUser_name());
                    }
                    if (user != null && user.getUser_profile_picture() != null){
                        Glide.with(userPicture).load(user.getUser_profile_picture()).centerCrop().into(userPicture);
                        userPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getActivity(),
                                        R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered);
                                materialAlertDialogBuilder.setMessage("Are you sure you want to sign out?");
                                materialAlertDialogBuilder.setCancelable(true);
                                materialAlertDialogBuilder.setPositiveButton(
                                        "Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, int id) {
                                                //Delete note
                                                FirebaseAuth.getInstance().signOut();
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }
                                        });

                                materialAlertDialogBuilder.setNegativeButton(
                                        "No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                materialAlertDialogBuilder.show();
                            }
                        });
                    }
                }
            }
        });
    }

    /*
     ----------------------------- Firebase setup ---------------------------------
    */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //check if email is verified
                    if (user.isEmailVerified()) {
                        // DO STUFF
                        getNotes(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        getUserInfo();
                        Log.d(TAG, "onAuthStateChanged: MAIL VERIFIED");
                    } else {
                        Toast.makeText(getActivity(), "Email is not Verified\nCheck your Inbox", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(getActivity(), "Not logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
                // ...
            }
        };
    }

    @Override
    public void onItemClick(int position) {
        Note note = noteList.get(position);
        Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(note.getNote_doc_ID()));
    }
}
