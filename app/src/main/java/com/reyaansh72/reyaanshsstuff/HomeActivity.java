package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton menuButton;

    private TextView tvWelcomeUser;
    private TextView homeUserDisplayName;
    private TextView homeUserEmail;

    private MaterialButton btnOpenDrawer;
    private MaterialButton homeSignOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.homeMainContent), (v, insets) -> {
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
        setupNavigationDrawer();
        updateUserHeader();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuButton = findViewById(R.id.menuButton);

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        homeUserDisplayName = findViewById(R.id.homeUserDisplayName);
        homeUserEmail = findViewById(R.id.homeUserEmail);

        btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        homeSignOutButton = findViewById(R.id.homeSignOutButton);

        // Open Drawer
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnOpenDrawer.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Sign Out
        homeSignOutButton.setOnClickListener(v -> signOutUser());
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_shorts) {
                Intent intent = new Intent(HomeActivity.this, ShortsActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_videos) {
                Intent intent = new Intent(HomeActivity.this, VideosActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_ai_chat) {
                Intent intent = new Intent(HomeActivity.this, AiChatActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                if (mAuth.getCurrentUser() != null) {
                    intent.putExtra("profile_user_id", mAuth.getCurrentUser().getUid());
                    intent.putExtra("profile_username", mAuth.getCurrentUser().getDisplayName());
                }
                startActivity(intent);
            } else if (itemId == R.id.nav_messages) {
                Intent intent = new Intent(HomeActivity.this, ChatListActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_notes) {
                Intent intent = new Intent(HomeActivity.this, NotesActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_calculator) {
                Intent intent = new Intent(HomeActivity.this, CalculatorActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_weather) {
                Intent intent = new Intent(HomeActivity.this, WeatherActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_qrcode) {
                Intent intent = new Intent(HomeActivity.this, QrCodeActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_converter) {
                Intent intent = new Intent(HomeActivity.this, ConverterActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_games) {
                Intent intent = new Intent(HomeActivity.this, GamesActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_developer_info) {
                Intent intent = new Intent(HomeActivity.this, DeveloperInfoActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.nav_sign_out) {
                signOutUser();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void updateUserHeader() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            if (user.getEmail() != null) {
                displayName = user.getEmail().split("@")[0];
            } else {
                displayName = "User";
            }
        }

        String email = user.getEmail() != null ? user.getEmail() : "No email linked";

        tvWelcomeUser.setText("Welcome back, " + displayName + "!");
        if (homeUserDisplayName != null) homeUserDisplayName.setText(displayName);
        if (homeUserEmail != null) homeUserEmail.setText(email);

        // Update Drawer Header
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView navName = headerView.findViewById(R.id.navHeaderName);
            TextView navEmail = headerView.findViewById(R.id.navHeaderEmail);

            if (navName != null) navName.setText(displayName);
            if (navEmail != null) navEmail.setText(email);
        }
    }

    private void signOutUser() {
        mAuth.signOut();
        Toast.makeText(HomeActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}