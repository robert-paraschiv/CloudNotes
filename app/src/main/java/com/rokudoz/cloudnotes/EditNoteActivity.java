package com.rokudoz.cloudnotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class EditNoteActivity extends AppCompatActivity {
    private static final String TAG = "EditNoteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        Log.d(TAG, "onCreate: ");
        postponeEnterTransition();

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().hasExtra("transition_name")) {
                findViewById(R.id.editNoteActivity_rootLayout).setTransitionName(getIntent().getStringExtra("transition_name"));
                TextView text = findViewById(R.id.editNoteActivity_textEditText);
                text.setText("");

                setupBackgroundColor(getIntent().getStringExtra("backgroundColor"));

                startPostponedEnterTransition();
                MaterialButton back = findViewById(R.id.editNoteActivity_backBtn);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    private void setupBackgroundColor(String color) {
        if (color != null) {
            switch (color) {
                case "yellow":
                    findViewById(R.id.editNoteActivity_rootLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.note_background_color_yellow));
                    break;
                case "red":
                    findViewById(R.id.editNoteActivity_rootLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.note_background_color_red));
                    break;
                case "green":
                    findViewById(R.id.editNoteActivity_rootLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.note_background_color_green));
                    break;
                case "blue":
                    findViewById(R.id.editNoteActivity_rootLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.note_background_color_blue));
                    break;
                case "orange":
                    findViewById(R.id.editNoteActivity_rootLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.note_background_color_orange));
                    break;
                case "purple":
                    findViewById(R.id.editNoteActivity_rootLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.note_background_color_purple));
                    break;
            }
        } else {
            findViewById(R.id.editNoteActivity_rootLayout).setBackgroundColor(ContextCompat.getColor(this, R.color.fragments_background));
        }
    }
}