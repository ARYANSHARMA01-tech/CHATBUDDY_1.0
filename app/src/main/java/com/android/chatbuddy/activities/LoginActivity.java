package com.android.chatbuddy.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.chatbuddy.R;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private MaterialTextView signupLink, forgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            // Initialize Firebase first
            auth = FirebaseUtils.getAuth();
            if (auth == null) {
                throw new RuntimeException("Firebase initialization failed");
            }

            initializeViews();
            checkCurrentUser();
            setupClickListeners();

            // Debug log
            Log.d("LoginActivity", "Activity initialized successfully");
        } catch (Exception e) {
            Log.e("LoginActivity", "Initialization error", e);
            Toast.makeText(this, "Initialization error. Please restart.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        signupLink = findViewById(R.id.signup_link);
        forgotPassword = findViewById(R.id.forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        // Verify all views are initialized
        if (emailInput == null || passwordInput == null || loginButton == null ||
                signupLink == null || forgotPassword == null) {
            throw new RuntimeException("View initialization failed");
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            Log.d("LoginFlow", "Login button clicked");
            validateAndLogin();
        });

        signupLink.setOnClickListener(v -> {
            Log.d("Navigation", "Signup link clicked");
            navigateToSignup();
        });

        forgotPassword.setOnClickListener(v -> {
            // Implement later
        });
    }

    private void validateAndLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        loginUser(email, password);
    }

    private void loginUser(String email, String password) {
        showProgress(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        Log.d("LoginFlow", "Login successful");
                        navigateToMainActivity();
                    } else {
                        Log.e("LoginFlow", "Login failed", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + (task.getException() != null ?
                                        task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToSignup() {
        try {
            Log.d("Navigation", "Attempting to start SignupActivity");
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Log.e("Navigation", "Signup activity error", e);
            Toast.makeText(this,
                    "Cannot open signup: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
    }
}