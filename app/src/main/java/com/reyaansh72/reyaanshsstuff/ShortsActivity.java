package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
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

public class ShortsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    private RecyclerView shortsRecyclerView;
    private ShortsAdapter shortsAdapter;
    private List<ShortItem> shortsList;

    private ImageButton shortsBackButton;
    private MaterialButton fabUploadShort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shorts);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("shorts");

        initializeViews();
        setupRecyclerView();
        loadShorts();
    }

    private void initializeViews() {
        shortsBackButton = findViewById(R.id.shortsBackButton);
        fabUploadShort = findViewById(R.id.fabUploadShort);
        shortsRecyclerView = findViewById(R.id.shortsRecyclerView);

        shortsBackButton.setOnClickListener(v -> finish());
        fabUploadShort.setOnClickListener(v -> showUploadDialog(null));
    }

    private void setupRecyclerView() {
        shortsList = new ArrayList<>();
        shortsAdapter = new ShortsAdapter(shortsList, this::showCommentsSheet);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        shortsRecyclerView.setLayoutManager(layoutManager);
        shortsRecyclerView.setAdapter(shortsAdapter);

        // Attach PagerSnapHelper for 100% Full-Screen Paging (like YouTube Shorts)
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(shortsRecyclerView);
    }

    private void loadShorts() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shortsList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ShortItem item = itemSnapshot.getValue(ShortItem.class);
                    if (item != null) {
                        if (item.getId() == null) {
                            item.setId(itemSnapshot.getKey());
                        }
                        String currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
                        if (item.isPublic() || (item.getUserId() != null && item.getUserId().equals(currentUid))) {
                            shortsList.add(item);
                        }
                    }
                }
                Collections.reverse(shortsList);
                shortsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showUploadDialog(ShortItem existingItem) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_upload_short, null);
        TextInputEditText titleInput = dialogView.findViewById(R.id.uploadTitleInput);
        TextInputEditText descInput = dialogView.findViewById(R.id.uploadDescriptionInput);
        TextInputEditText urlInput = dialogView.findViewById(R.id.uploadVideoUrlInput);
        TextInputEditText thumbUrlInput = dialogView.findViewById(R.id.uploadThumbnailUrlInput);
        RadioButton radioPublic = dialogView.findViewById(R.id.radioPublic);
        MaterialButton btnSample1 = dialogView.findViewById(R.id.btnSample1);
        MaterialButton btnSample2 = dialogView.findViewById(R.id.btnSample2);
        MaterialButton btnPublish = dialogView.findViewById(R.id.btnPublishShort);

        btnSample1.setOnClickListener(v -> {
            urlInput.setText("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4");
            titleInput.setText("Fire & Blazes Short 🔥");
            descInput.setText("Action sample video short");
        });

        btnSample2.setOnClickListener(v -> {
            urlInput.setText("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
            titleInput.setText("Elephant Dream Short 🐘");
            descInput.setText("Classic animated short film");
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnPublish.setOnClickListener(v -> {
            String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
            String desc = descInput.getText() != null ? descInput.getText().toString().trim() : "";
            String videoUrl = urlInput.getText() != null ? urlInput.getText().toString().trim() : "";
            String thumbUrl = thumbUrlInput.getText() != null ? thumbUrlInput.getText().toString().trim() : "";
            boolean isPublic = radioPublic.isChecked();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(videoUrl)) {
                Toast.makeText(this, "Title and Video URL are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anon";
            String username = mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null ?
                    mAuth.getCurrentUser().getDisplayName() : "User";

            String shortId = existingItem != null ? existingItem.getId() : databaseRef.push().getKey();
            if (shortId == null) shortId = String.valueOf(System.currentTimeMillis());

            ShortItem shortItem = new ShortItem(
                    shortId,
                    title,
                    desc,
                    videoUrl,
                    thumbUrl,
                    username,
                    userId,
                    System.currentTimeMillis(),
                    isPublic
            );

            databaseRef.child(shortId).setValue(shortItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Short published!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to publish short", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showCommentsSheet(ShortItem item) {
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_comments, null);
        RecyclerView commentsRecyclerView = sheetView.findViewById(R.id.commentsRecyclerView);
        TextInputEditText commentInput = sheetView.findViewById(R.id.commentEditText);
        MaterialButton btnPostComment = sheetView.findViewById(R.id.btnPostComment);

        List<CommentItem> commentList = new ArrayList<>();
        CommentsAdapter commentsAdapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheetView);

        DatabaseReference commentsRef = databaseRef.child(item.getId()).child("comments");
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CommentItem comment = snap.getValue(CommentItem.class);
                    if (comment != null) commentList.add(comment);
                }
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnPostComment.setOnClickListener(v -> {
            String text = commentInput.getText() != null ? commentInput.getText().toString().trim() : "";
            if (TextUtils.isEmpty(text)) return;

            String username = mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null ?
                    mAuth.getCurrentUser().getDisplayName() : "User";
            String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

            String commentId = commentsRef.push().getKey();
            if (commentId == null) commentId = String.valueOf(System.currentTimeMillis());

            CommentItem comment = new CommentItem(commentId, item.getId(), username, uid, text, System.currentTimeMillis());
            commentsRef.child(commentId).setValue(comment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    commentInput.setText("");
                    long newCommentsCount = item.getCommentsCount() + 1;
                    databaseRef.child(item.getId()).child("commentsCount").setValue(newCommentsCount);
                }
            });
        });

        dialog.show();
    }
}