package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private String profileUserId;
    private String profileUsername;
    private String currentUserId;

    private ImageButton profileBackButton;
    private TextView profileTopTitle;
    private TextView profileUsernameText;
    private TextView profileEmailText;
    private TextView profileBioText;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;
    private TextView tvProfileNotesCount;
    private TextView tvProfileShortsCount;
    private MaterialButton btnFollow;
    private MaterialButton btnEditBio;
    private RecyclerView userShortsRecyclerView;

    private DatabaseReference databaseRef;
    private DatabaseReference usersRef;
    private DatabaseReference followersRef;
    private DatabaseReference followingRef;

    private List<ShortItem> userShortsList;
    private ShortsAdapter shortsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();
        profileUserId = intent.getStringExtra("profile_user_id");
        profileUsername = intent.getStringExtra("profile_username");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        if (profileUserId == null || profileUserId.isEmpty()) {
            profileUserId = currentUserId;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("shorts");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        followersRef = FirebaseDatabase.getInstance().getReference("followers");
        followingRef = FirebaseDatabase.getInstance().getReference("following");

        initializeViews();
        setupRecyclerView();
        loadProfileData();
        loadUserShorts();
        loadPersonalNotesCount();
    }

    private void initializeViews() {
        profileBackButton = findViewById(R.id.profileBackButton);
        profileTopTitle = findViewById(R.id.profileTopTitle);
        profileUsernameText = findViewById(R.id.profileUsername);
        profileEmailText = findViewById(R.id.profileEmail);
        profileBioText = findViewById(R.id.profileBioText);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        tvProfileNotesCount = findViewById(R.id.tvProfileNotesCount);
        tvProfileShortsCount = findViewById(R.id.tvProfileShortsCount);
        btnFollow = findViewById(R.id.btnFollow);
        btnEditBio = findViewById(R.id.btnEditBio);
        userShortsRecyclerView = findViewById(R.id.userShortsRecyclerView);

        profileBackButton.setOnClickListener(v -> finish());

        if (profileUserId.equals(currentUserId)) {
            btnFollow.setVisibility(View.GONE);
            btnEditBio.setVisibility(View.VISIBLE);
            profileTopTitle.setText("My Profile");
        } else {
            btnFollow.setVisibility(View.VISIBLE);
            btnEditBio.setVisibility(View.GONE);
            if (profileUsername != null && !profileUsername.isEmpty()) {
                profileTopTitle.setText(profileUsername + "'s Profile");
            }
        }

        btnEditBio.setOnClickListener(v -> showEditBioDialog());
    }

    private void setupRecyclerView() {
        userShortsList = new ArrayList<>();
        shortsAdapter = new ShortsAdapter(userShortsList, item -> {
            // Comment click
        });
        userShortsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userShortsRecyclerView.setAdapter(shortsAdapter);
    }

    private void loadProfileData() {
        if (profileUserId.isEmpty()) return;

        if (profileUsername != null && !profileUsername.isEmpty()) {
            profileUsernameText.setText("@" + profileUsername);
        } else {
            profileUsernameText.setText("@User");
        }

        // Load Bio and Email from RTDB /users/{profileUserId}
        usersRef.child(profileUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String bio = snapshot.child("bio").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String uname = snapshot.child("username").getValue(String.class);

                if (bio != null && !bio.trim().isEmpty()) {
                    profileBioText.setText(bio);
                } else {
                    profileBioText.setText("No bio yet.");
                }

                if (email != null && !email.trim().isEmpty()) {
                    profileEmailText.setText(email);
                } else {
                    profileEmailText.setText("");
                }

                if (uname != null && !uname.trim().isEmpty()) {
                    profileUsernameText.setText("@" + uname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Followers count
        followersRef.child(profileUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvFollowersCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Following count
        followingRef.child(profileUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvFollowingCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Check if current user is following this profile
        if (!currentUserId.isEmpty() && !profileUserId.equals(currentUserId)) {
            followersRef.child(profileUserId).child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFollowing = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    if (isFollowing) {
                        btnFollow.setText("Following");
                    } else {
                        btnFollow.setText("Follow");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            btnFollow.setOnClickListener(v -> toggleFollow());
        }
    }

    private void toggleFollow() {
        if (currentUserId.isEmpty() || profileUserId.isEmpty()) return;

        DatabaseReference followerRef = followersRef.child(profileUserId).child(currentUserId);
        DatabaseReference followingUserRef = followingRef.child(currentUserId).child(profileUserId);

        followerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFollowing = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (isFollowing) {
                    followerRef.removeValue();
                    followingUserRef.removeValue();
                    Toast.makeText(UserProfileActivity.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                } else {
                    followerRef.setValue(true);
                    followingUserRef.setValue(true);
                    Toast.makeText(UserProfileActivity.this, "Following!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showEditBioDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_bio, null);
        TextInputEditText editBioInput = dialogView.findViewById(R.id.editBioInput);
        MaterialButton btnSaveBio = dialogView.findViewById(R.id.btnSaveBio);

        if (!profileBioText.getText().toString().equals("No bio yet.")) {
            editBioInput.setText(profileBioText.getText().toString());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnSaveBio.setOnClickListener(v -> {
            String newBio = editBioInput.getText() != null ? editBioInput.getText().toString().trim() : "";
            usersRef.child(currentUserId).child("bio").setValue(newBio).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Bio updated successfully!", Toast.LENGTH_SHORT).show();
                    profileBioText.setText(TextUtils.isEmpty(newBio) ? "No bio yet." : newBio);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to update bio", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void loadPersonalNotesCount() {
        if (profileUserId.isEmpty()) return;

        FirebaseDatabase.getInstance().getReference("notes").child(profileUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        tvProfileNotesCount.setText("📝 Personal Notes: " + count + " Notes");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadUserShorts() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userShortsList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ShortItem item = itemSnapshot.getValue(ShortItem.class);
                    if (item != null) {
                        if (item.getId() == null) {
                            item.setId(itemSnapshot.getKey());
                        }

                        if (item.getUserId() != null && item.getUserId().equals(profileUserId)) {
                            if (item.isPublic() || profileUserId.equals(currentUserId)) {
                                userShortsList.add(item);
                            }
                        }
                    }
                }
                shortsAdapter.notifyDataSetChanged();
                tvProfileShortsCount.setText("🎥 Posted Shorts: " + userShortsList.size() + " Videos");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}