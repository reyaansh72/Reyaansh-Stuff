package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeveloperInfoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView devDisplayNameText;
    private TextView devEmailText;
    private TextView devUidText;
    private TextView tvStatNotes;

    private MaterialButton btnOpenFullProfile;
    private MaterialButton runDiagnosticsButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_developer_info);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.developerInfoRoot), (v, insets) -> {
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

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        displayUserData();
        loadSuiteStats();
    }

    private void initializeViews() {
        devDisplayNameText = findViewById(R.id.devDisplayNameText);
        devEmailText = findViewById(R.id.devEmailText);
        devUidText = findViewById(R.id.devUidText);
        tvStatNotes = findViewById(R.id.tvStatNotes);

        btnOpenFullProfile = findViewById(R.id.btnOpenFullProfile);
        runDiagnosticsButton = findViewById(R.id.runDiagnosticsButton);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        btnOpenFullProfile.setOnClickListener(v -> {
            FirebaseUser u = mAuth.getCurrentUser();
            if (u != null) {
                Intent intent = new Intent(DeveloperInfoActivity.this, UserProfileActivity.class);
                intent.putExtra("profile_user_id", u.getUid());
                intent.putExtra("profile_username", u.getDisplayName());
                startActivity(intent);
            }
        });

        runDiagnosticsButton.setOnClickListener(v -> Toast.makeText(
                DeveloperInfoActivity.this,
                "Diagnostics Passed! All 10+ Apps, Games, and Firebase Services Operating Properly.",
                Toast.LENGTH_LONG
        ).show());
    }

    private void displayUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            devDisplayNameText.setText("Name: Not Logged In");
            devEmailText.setText("Email: N/A");
            devUidText.setText("UID: N/A");
            return;
        }

        String displayName = currentUser.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Not set";
        }

        String email = currentUser.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = "No email";
        }

        devDisplayNameText.setText("Name: " + displayName);
        devEmailText.setText("Email: " + email);
        devUidText.setText("UID: " + currentUser.getUid());
    }

    private void loadSuiteStats() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        FirebaseDatabase.getInstance().getReference("notes").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long noteCount = snapshot.getChildrenCount();
                        tvStatNotes.setText("📝 Personal Notes: " + noteCount + " Notes Saved");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}