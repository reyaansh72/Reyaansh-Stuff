package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShortsPlayerActivity extends AppCompatActivity {

    private String shortId;
    private String title;
    private String description;
    private String videoUrl;
    private String username;
    private String ownerUserId;

    private PlayerView playerView;
    private ExoPlayer player;

    private ImageButton playerBackButton;
    private TextView playerTopTitle;
    private TextView playerUploader;
    private TextView playerTitle;
    private TextView playerDescription;

    private LinearLayout playerLikeLayout;
    private ImageView playerIvLike;
    private TextView playerLikesCount;
    private LinearLayout playerCommentLayout;
    private TextView playerCommentsCount;
    private ImageButton playerBtnShare;
    private ImageButton playerBtnEdit;
    private ImageButton playerBtnDelete;

    private DatabaseReference shortRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shorts_player);

        Intent intent = getIntent();
        shortId = intent.getStringExtra("short_id");
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        videoUrl = intent.getStringExtra("video_url");
        username = intent.getStringExtra("username");

        if (shortId == null) shortId = "";
        if (title == null) title = "Short";
        if (description == null) description = "";
        if (videoUrl == null) videoUrl = "";
        if (username == null) username = "Anonymous";

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        shortRef = FirebaseDatabase.getInstance().getReference("shorts").child(shortId);

        initializeViews();
        setupExoPlayer();
        loadLiveStats();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        playerBackButton = findViewById(R.id.playerBackButton);
        playerTopTitle = findViewById(R.id.playerTopTitle);
        playerUploader = findViewById(R.id.playerUploader);
        playerTitle = findViewById(R.id.playerTitle);
        playerDescription = findViewById(R.id.playerDescription);

        playerLikeLayout = findViewById(R.id.playerLikeLayout);
        playerIvLike = findViewById(R.id.playerIvLike);
        playerLikesCount = findViewById(R.id.playerLikesCount);
        playerCommentLayout = findViewById(R.id.playerCommentLayout);
        playerCommentsCount = findViewById(R.id.playerCommentsCount);
        playerBtnShare = findViewById(R.id.playerBtnShare);
        playerBtnEdit = findViewById(R.id.playerBtnEdit);
        playerBtnDelete = findViewById(R.id.playerBtnDelete);

        playerTopTitle.setText(title);
        playerUploader.setText("@" + username);
        playerUploader.setOnClickListener(v -> {
            if (ownerUserId != null && !ownerUserId.isEmpty()) {
                Intent intent = new Intent(ShortsPlayerActivity.this, UserProfileActivity.class);
                intent.putExtra("profile_user_id", ownerUserId);
                intent.putExtra("profile_username", username);
                startActivity(intent);
            }
        });
        playerTitle.setText(title);
        playerDescription.setText(description);

        playerBackButton.setOnClickListener(v -> finish());

        // Like click
        playerLikeLayout.setOnClickListener(v -> toggleLike());

        // Comment click
        playerCommentLayout.setOnClickListener(v -> showCommentsDialog());

        // Share click
        playerBtnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Watch this short on Reyaansh's Stuff:\n" + title + "\n" + videoUrl);
            startActivity(Intent.createChooser(shareIntent, "Share Short"));
        });

        // Edit click (Owner only)
        playerBtnEdit.setOnClickListener(v -> showEditDialog());

        // Delete click (Owner only)
        playerBtnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void setupExoPlayer() {
        if (videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            player.setMediaItem(mediaItem);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.prepare();
            player.play();

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Toast.makeText(ShortsPlayerActivity.this, "Failed to load video stream: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing player: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void loadLiveStats() {
        if (shortId.isEmpty()) return;

        shortRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ShortItem item = snapshot.getValue(ShortItem.class);
                if (item != null) {
                    ownerUserId = item.getUserId();
                    playerLikesCount.setText(String.valueOf(item.getLikesCount()));
                    playerCommentsCount.setText(String.valueOf(item.getCommentsCount()));

                    playerTitle.setText(item.getTitle());
                    playerDescription.setText(item.getDescription());
                    playerTopTitle.setText(item.getTitle());

                    // Show edit/delete buttons if current user is owner
                    if (currentUserId != null && currentUserId.equals(ownerUserId)) {
                        playerBtnEdit.setVisibility(View.VISIBLE);
                        playerBtnDelete.setVisibility(View.VISIBLE);
                    } else {
                        playerBtnEdit.setVisibility(View.GONE);
                        playerBtnDelete.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        if (!currentUserId.isEmpty()) {
            shortRef.child("likes").child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isLiked = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    if (isLiked) {
                        playerIvLike.setImageResource(R.drawable.ic_favorite_24);
                    } else {
                        playerIvLike.setImageResource(R.drawable.ic_favorite_border_24);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void toggleLike() {
        if (currentUserId.isEmpty() || shortId.isEmpty()) return;

        DatabaseReference userLikeRef = shortRef.child("likes").child(currentUserId);
        userLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean currentlyLiked = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));

                shortRef.child("likesCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot countSnap) {
                        long currentLikes = countSnap.exists() && countSnap.getValue(Long.class) != null ?
                                countSnap.getValue(Long.class) : 0;

                        if (currentlyLiked) {
                            userLikeRef.removeValue();
                            long newLikes = Math.max(0, currentLikes - 1);
                            shortRef.child("likesCount").setValue(newLikes);
                            playerIvLike.setImageResource(R.drawable.ic_favorite_border_24);
                            playerLikesCount.setText(String.valueOf(newLikes));
                        } else {
                            userLikeRef.setValue(true);
                            long newLikes = currentLikes + 1;
                            shortRef.child("likesCount").setValue(newLikes);
                            playerIvLike.setImageResource(R.drawable.ic_favorite_24);
                            playerLikesCount.setText(String.valueOf(newLikes));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Short")
                .setMessage("Are you sure you want to delete this short permanently?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    shortRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Short deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete short", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_short, null);

        TextInputEditText editTitleInput = dialogView.findViewById(R.id.editTitleInput);
        TextInputEditText editDescriptionInput = dialogView.findViewById(R.id.editDescriptionInput);
        TextInputEditText editVideoUrlInput = dialogView.findViewById(R.id.editVideoUrlInput);
        RadioButton editRadioPublic = dialogView.findViewById(R.id.editRadioPublic);
        RadioButton editRadioPrivate = dialogView.findViewById(R.id.editRadioPrivate);
        MaterialButton btnUpdateShort = dialogView.findViewById(R.id.btnUpdateShort);

        // Pre-fill existing data
        editTitleInput.setText(title);
        editDescriptionInput.setText(description);
        editVideoUrlInput.setText(videoUrl);

        shortRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ShortItem item = snapshot.getValue(ShortItem.class);
                if (item != null) {
                    if (item.isPublic()) {
                        editRadioPublic.setChecked(true);
                    } else {
                        editRadioPrivate.setChecked(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnUpdateShort.setOnClickListener(v -> {
            String newTitle = editTitleInput.getText() != null ? editTitleInput.getText().toString().trim() : "";
            String newDesc = editDescriptionInput.getText() != null ? editDescriptionInput.getText().toString().trim() : "";
            String newUrl = editVideoUrlInput.getText() != null ? editVideoUrlInput.getText().toString().trim() : "";
            boolean newIsPublic = editRadioPublic.isChecked();

            if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newUrl)) {
                Toast.makeText(this, "Title and Video URL are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            shortRef.child("title").setValue(newTitle);
            shortRef.child("description").setValue(newDesc);
            shortRef.child("videoUrl").setValue(newUrl);
            shortRef.child("public").setValue(newIsPublic);

            title = newTitle;
            description = newDesc;
            videoUrl = newUrl;
            playerTitle.setText(newTitle);
            playerDescription.setText(newDesc);
            playerTopTitle.setText(newTitle);

            // Re-prepare player if video url changed
            if (player != null) {
                player.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)));
                player.prepare();
                player.play();
            }

            Toast.makeText(this, "Short updated successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showCommentsDialog() {
        if (shortId.isEmpty()) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comments, null);
        RecyclerView commentsRecyclerView = dialogView.findViewById(R.id.commentsRecyclerView);
        TextInputEditText commentEditText = dialogView.findViewById(R.id.commentEditText);
        MaterialButton btnPostComment = dialogView.findViewById(R.id.btnPostComment);

        List<CommentItem> commentList = new ArrayList<>();
        CommentsAdapter commentsAdapter = new CommentsAdapter(commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(dialogView);

        DatabaseReference commentsRef = shortRef.child("comments");

        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot commentSnap : snapshot.getChildren()) {
                    CommentItem comment = commentSnap.getValue(CommentItem.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                commentsAdapter.notifyDataSetChanged();
                if (!commentList.isEmpty()) {
                    commentsRecyclerView.smoothScrollToPosition(commentList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btnPostComment.setOnClickListener(v -> {
            String commentText = commentEditText.getText() != null ? commentEditText.getText().toString().trim() : "";
            if (TextUtils.isEmpty(commentText)) {
                Toast.makeText(this, "Enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String commentUsername = "Anonymous";
            String userId = "unknown";

            if (currentUser != null) {
                userId = currentUser.getUid();
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().trim().isEmpty()) {
                    commentUsername = currentUser.getDisplayName();
                } else if (currentUser.getEmail() != null) {
                    commentUsername = currentUser.getEmail().split("@")[0];
                }
            }

            String commentId = commentsRef.push().getKey();
            if (commentId == null) {
                commentId = String.valueOf(System.currentTimeMillis());
            }

            CommentItem commentItem = new CommentItem(
                    commentId,
                    shortId,
                    commentUsername,
                    userId,
                    commentText,
                    System.currentTimeMillis()
            );

            commentsRef.child(commentId).setValue(commentItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    commentEditText.setText("");
                    shortRef.child("commentsCount").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long count = snapshot.exists() && snapshot.getValue(Long.class) != null ? snapshot.getValue(Long.class) : 0;
                            shortRef.child("commentsCount").setValue(count + 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            });
        });

        bottomSheetDialog.show();
    }
}