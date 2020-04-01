package com.rokudoz.cloudnotes.Fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rokudoz.cloudnotes.Models.Note;
import com.rokudoz.cloudnotes.R;

import java.util.Objects;

public class NewNoteFragment extends Fragment {
    private static final String TAG = "NewNoteFragment";

    private View view;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("Users");

    public NewNoteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_note, container, false);

        final TextInputEditText textInputEditText = view.findViewById(R.id.newNoteFragment_textInput);
        final TextInputEditText titleInputEditText = view.findViewById(R.id.newNoteFragment_title_textInput);

        MaterialButton backBtn = view.findViewById(R.id.newNoteFragment_backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textInputEditText.getText().toString().trim().equals("")) {
                    Note note = new Note(Objects.requireNonNull(titleInputEditText.getText()).toString(),
                            Objects.requireNonNull(textInputEditText.getText()).toString(),
                            null,
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                            null);

                    usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").add(note)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getActivity(), "Added note successfully", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());
                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Empty note discarded", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());
                }
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!textInputEditText.getText().toString().trim().equals("")) {
                    Note note = new Note(Objects.requireNonNull(titleInputEditText.getText()).toString(),
                            Objects.requireNonNull(textInputEditText.getText()).toString(),
                            null,
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                            null);

                    usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("Notes").add(note)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(getActivity(), "Added note successfully", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());
                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Empty note discarded", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(NewNoteFragmentDirections.actionNewNoteFragmentToHomeFragment());
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        return view;
    }
}