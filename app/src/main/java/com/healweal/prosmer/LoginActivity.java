package com.healweal.prosmer;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException; 
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextInputLayout emailLayout, passwordLayout;
    private MaterialButton loginButton;
    private ProgressBar progressBar;
    private TextView signUpLinkTextView, forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            navigateToMainApp();
            return; // Important to stop further execution
        }

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        signUpLinkTextView = findViewById(R.id.signUpLinkTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        loginButton.setOnClickListener(v -> {
            if (validateInput()) {
                loginUser();
            }
        });

        signUpLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private boolean validateInput() {
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email");
            return false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return false;
        }

        return true;
    }

    private void loginUser() {
        showLoading(true);
        String email = emailLayout.getEditText().getText().toString().trim();
        String password = passwordLayout.getEditText().getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainApp();
                    } else {
                        showLoading(false);
                        // Handle specific login errors for a better UX
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            emailLayout.setError("No account found with this email");
                            emailLayout.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            passwordLayout.setError("Incorrect password. Please try again.");
                            passwordLayout.requestFocus();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send Reset Email", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send reset email.", Toast.LENGTH_LONG).show();
                        }
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setText("");
            loginButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setText("Log In");
            loginButton.setEnabled(true);
        }
    }

    private void navigateToMainApp() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // These flags clear the activity stack, so the user can't go back to the login screen
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}