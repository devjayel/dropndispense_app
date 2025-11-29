package com.fuxdevs.dropndispense.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fuxdevs.dropndispense.R;
import com.fuxdevs.dropndispense.user.UserMainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private TextView forgotPasswordText;
    private TextView registerText;
    private TextView errorText;
    private View formContainer;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordButton);
        registerText = findViewById(R.id.registerButton);
        errorText = findViewById(R.id.errorText);
        formContainer = findViewById(R.id.formContainer);
        loadingIndicator = findViewById(R.id.progressBar);

        // Set click listeners
        loginButton.setOnClickListener(v -> handleLogin());
        forgotPasswordText.setOnClickListener(v -> navigateToForgotPassword());
        registerText.setOnClickListener(v -> navigateToRegister());
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            showError(getString(R.string.error_password_short));
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Attempt login with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Update FCM token after successful login
                        updateFCMToken();
                        // Navigate to main app screen
                        navigateToMain();
                    } else {
                        // Show error
                        showError(getString(R.string.error_login_failed));
                        setLoadingState(false);
                    }
                });
    }

    private void updateFCMToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    String userId = mAuth.getCurrentUser().getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    
                    // Update users collection
                    db.collection("users").document(userId)
                        .update("user_fcm_token", token);
                    
                    // Update hardwares collection
                    db.collection("hardwares")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                String hardwareId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                db.collection("hardwares").document(hardwareId)
                                    .update("user_fcm_token", token);
                            }
                        });
                }
            });
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToMain() {
        // Navigate to main app screen and clear the back stack
        Intent intent = new Intent(this, UserMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void setLoadingState(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        formContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}