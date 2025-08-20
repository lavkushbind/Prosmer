package com.healweal.prosmer;


import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextInputLayout nameLayout, emailLayout, passwordLayout;
    private MaterialButton signUpButton;
    private ProgressBar progressBar;
    private TextView loginLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progressBar);
        loginLinkTextView = findViewById(R.id.loginLinkTextView);

        signUpButton.setOnClickListener(v -> {
            if (validateInput()) {
                registerUser();
            }
        });

        loginLinkTextView.setOnClickListener(v -> {
            // Navigate to LoginActivity
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        });
    }

    private boolean validateInput() {
        // Clear previous errors
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String name = nameLayout.getEditText().getText().toString().trim();
        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        if (name.isEmpty()) {
            nameLayout.setError("Name is required");
            return false;
        }

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            return false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters long");
            return false;
        }

        return true;
    }

    private void registerUser() {
        showLoading(true);
        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String name = nameLayout.getEditText().getText().toString().trim();
                            User user = new User(name, email);

                            // Save user data to Realtime Database
                            mDatabase.child("users").child(firebaseUser.getUid()).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        showLoading(false);
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            // Navigate to the main part of the app
                                            Intent intent = new Intent(SignUpActivity.this, OnboardingTestActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Handle database write error
                                            Toast.makeText(SignUpActivity.this, "Failed to save user data.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        showLoading(false);
                        // Handle specific authentication errors
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            emailLayout.setError("This email address is already in use.");
                            emailLayout.requestFocus();
                        } catch (Exception e) {
                            Toast.makeText(SignUpActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            signUpButton.setText(""); // Hide text when loading
            signUpButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            signUpButton.setText("Sign Up");
            signUpButton.setEnabled(true);
        }
    }
}