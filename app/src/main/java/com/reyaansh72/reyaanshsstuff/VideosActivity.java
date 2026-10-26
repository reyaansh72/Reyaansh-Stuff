package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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

public class VideosActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    private RecyclerView videosRecyclerView;
    private VideosAdapter videosAdapter;
    private List<ShortItem> videoList;

    private ImageButton videosBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_videos);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("shorts");

        videosBackButton = findViewById(R.id.videosBackButton);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);

        videosBackButton.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadVideos();
    }

    private void setupRecyclerView() {
        videoList = new ArrayList<>();
        videosAdapter = new VideosAdapter(videoList, this::showCommentsSheet);
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerView.setAdapter(videosAdapter);
    }

    private void loadVideos() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ShortItem item = itemSnapshot.getValue(ShortItem.class);
                    if (item != null) {
                        if (item.getId() == null) {
                            item.setId(itemSnapshot.getKey());
                        }
                        String currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
                        if (item.isPublic() || (item.getUserId() != null && item.getUserId().equals(currentUid))) {
                            videoList.add(item);
                        }
                    }
                }
                Collections.reverse(videoList);
                videosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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