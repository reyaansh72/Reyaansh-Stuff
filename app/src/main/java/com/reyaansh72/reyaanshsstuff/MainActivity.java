package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    private MaterialButton loginButton;
    private MaterialButton createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // If user is already logged in, skip login screen
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
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

        initializeLogic();
    }

    private void initializeLogic() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Login button
        loginButton.setOnClickListener(v -> loginUser());

        // Create account button
        createAccountButton.setOnClickListener(v -> {
            Intent registerIntent = new Intent(
                    MainActivity.this,
                    RegistrationActivity.class
            );
            startActivity(registerIntent);
        });
    }

    private void loginUser() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (email.isEmpty() || password.trim().isEmpty()) {
            Toast.makeText(
                    MainActivity.this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        Toast.makeText(MainActivity.this, "Logging In...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                            userRef.child("uid").setValue(userId);
                            if (user.getEmail() != null) userRef.child("email").setValue(user.getEmail());
                            if (user.getDisplayName() != null) userRef.child("username").setValue(user.getDisplayName());
                        }

                        Toast.makeText(
                                MainActivity.this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                        ).show();

                        navigateToHome();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Authentication failed.";

                        Toast.makeText(
                                MainActivity.this,
                                "Error: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}