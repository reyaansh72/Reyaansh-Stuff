package com.reyaansh72.reyaanshsstuff;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    private MaterialButton registerButton;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_register);

        initializeLogic();
    }

    private void initializeLogic() {
        usernameInput = findViewById(R.id.registerUsernameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.backToLoginButton);

        registerButton.setOnClickListener(v -> registerUser());

        loginButton.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = usernameInput.getText() != null ? usernameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        String confirmPassword = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString() : "";

        // Check for empty fields
        if (username.isEmpty() || email.isEmpty()
                || password.trim().isEmpty()
                || confirmPassword.trim().isEmpty()) {

            Toast.makeText(
                    RegistrationActivity.this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validate email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            passwordInput.setError(
                    "Password must be at least 6 characters"
            );
            passwordInput.requestFocus();
            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        registerButton.setEnabled(false);
        Toast.makeText(RegistrationActivity.this, "Creating account...", Toast.LENGTH_SHORT).show();

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    registerButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String userId = user.getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                            userRef.child("username").setValue(username);
                            userRef.child("email").setValue(email);
                            userRef.child("uid").setValue(userId);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates);
                        }

                        Toast.makeText(
                                RegistrationActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed.";

                        Toast.makeText(
                                RegistrationActivity.this,
                                "Error: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}