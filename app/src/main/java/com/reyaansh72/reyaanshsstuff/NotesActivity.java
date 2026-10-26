package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private DatabaseReference notesRef;
    private RecyclerView notesRecyclerView;
    private NotesAdapter adapter;
    private List<NoteItem> notesList;
    private TextView tvNoNotes;
    private ImageButton notesBackButton;
    private FloatingActionButton fabAddNote;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.notesTopBar), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        notesRef = FirebaseDatabase.getInstance().getReference("notes").child(currentUserId);

        initializeViews();
        setupRecyclerView();
        loadNotes();
    }

    private void initializeViews() {
        notesBackButton = findViewById(R.id.notesBackButton);
        fabAddNote = findViewById(R.id.fabAddNote);
        tvNoNotes = findViewById(R.id.tvNoNotes);
        notesRecyclerView = findViewById(R.id.notesRecyclerView);

        notesBackButton.setOnClickListener(v -> finish());
        fabAddNote.setOnClickListener(v -> showAddNoteDialog(null));
    }

    private void setupRecyclerView() {
        notesList = new ArrayList<>();
        adapter = new NotesAdapter(notesList, this::showAddNoteDialog);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(adapter);
    }

    private void loadNotes() {
        if (currentUserId.isEmpty()) return;

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notesList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    NoteItem note = snap.getValue(NoteItem.class);
                    if (note != null) {
                        if (note.getId() == null) {
                            note.setId(snap.getKey());
                        }
                        notesList.add(note);
                    }
                }
                Collections.reverse(notesList);
                adapter.notifyDataSetChanged();

                if (notesList.isEmpty()) {
                    tvNoNotes.setVisibility(View.VISIBLE);
                    notesRecyclerView.setVisibility(View.GONE);
                } else {
                    tvNoNotes.setVisibility(View.GONE);
                    notesRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showAddNoteDialog(NoteItem existingNote) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvNoteDialogTitle);
        TextInputEditText titleInput = dialogView.findViewById(R.id.noteTitleInput);
        TextInputEditText contentInput = dialogView.findViewById(R.id.noteContentInput);
        MaterialButton btnSaveNote = dialogView.findViewById(R.id.btnSaveNote);
        MaterialButton btnDeleteNote = dialogView.findViewById(R.id.btnDeleteNote);

        if (existingNote != null) {
            tvDialogTitle.setText("Edit Note");
            titleInput.setText(existingNote.getTitle());
            contentInput.setText(existingNote.getContent());
            btnSaveNote.setText("Update Note");
            btnDeleteNote.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSaveNote.setOnClickListener(v -> {
            String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
            String content = contentInput.getText() != null ? contentInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            String noteId = existingNote != null ? existingNote.getId() : notesRef.push().getKey();
            if (noteId == null) noteId = String.valueOf(System.currentTimeMillis());

            NoteItem note = new NoteItem(noteId, title, content, System.currentTimeMillis());

            notesRef.child(noteId).setValue(note).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, existingNote != null ? "Note updated!" : "Note created!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnDeleteNote.setOnClickListener(v -> {
            if (existingNote != null && existingNote.getId() != null) {
                notesRef.child(existingNote.getId()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Note deleted!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }
}