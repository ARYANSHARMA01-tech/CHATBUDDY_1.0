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
import com.android.chatbuddy.models.User;
import com.android.chatbuddy.utils.FirebaseUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private MaterialButton signupButton;
    private MaterialTextView loginLink;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Samsung device workaround
            if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
                applySamsungWorkarounds();
            }

            setContentView(R.layout.activity_signup);
            logDeviceInfo();

            auth = FirebaseUtils.getAuth();
            usersRef = FirebaseUtils.getUsersRef();

            if (auth == null || usersRef == null) {
                showErrorAndFinish("Authentication service unavailable");
                return;
            }

            initializeViews();
            setupClickListeners();
        } catch (Exception e) {
            Log.e("SignupActivity", "Init error", e);
            showErrorAndFinish("App initialization failed. Please restart.");
        }
    }

    private void applySamsungWorkarounds() {
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (Exception e) {
            Log.w("SamsungFix", "CloseGuard workaround failed");
        }
    }

    private void logDeviceInfo() {
        Log.d("DeviceInfo", "Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        Log.d("DeviceInfo", "Android: " + Build.VERSION.RELEASE);
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        signupButton = findViewById(R.id.signup_button);
        loginLink = findViewById(R.id.login_link);
        progressBar = findViewById(R.id.progress_bar);

        // Make login link properly clickable
        loginLink.setClickable(true);
        loginLink.setFocusable(true);
    }

    private void setupClickListeners() {
        signupButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        try {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (!validateInputs(username, email, password, confirmPassword)) {
                return;
            }

            showProgress(true);
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                saveUserToDatabase(user.getUid(), username, email);
                            } else {
                                throw new RuntimeException("User creation failed");
                            }
                        } else {
                            showSignupError(task.getException());
                        }
                    });
        } catch (Exception e) {
            showSignupError(e);
        }
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Username is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Invalid email format");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords don't match");
            isValid = false;
        }

        return isValid;
    }

    private void saveUserToDatabase(String userId, String username, String email) {
        User user = new User(userId, username, email, "default",
                "Hey there! I'm using ChatBuddy", false, "");

        usersRef.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        FirebaseUtils.updateUserStatus(true);
                        navigateToMainActivity();
                    } else {
                        showSignupError(task.getException());
                    }
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        signupButton.setEnabled(!show);
    }

    private void showSignupError(Exception exception) {
        showProgress(false);
        String error = exception != null ? exception.getMessage() : "Signup failed";
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("Navigation", "MainActivity error", e);
            showErrorAndFinish("Cannot open app");
        }
    }

    private void navigateToLogin() {
        try {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } catch (Exception e) {
            Log.e("Navigation", "LoginActivity error", e);
            Toast.makeText(this, "Cannot open login", Toast.LENGTH_LONG).show();
        }
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}