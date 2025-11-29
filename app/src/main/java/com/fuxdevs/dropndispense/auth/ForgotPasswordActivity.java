package com.fuxdevs.dropndispense.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fuxdevs.dropndispense.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText emailInput;
    private TextView backToLoginText;
    private TextView errorText;
    private View formContainer;
    private ProgressBar loadingIndicator;

    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        backToLoginText = findViewById(R.id.backToLoginButton);
        errorText = findViewById(R.id.errorText);
        formContainer = findViewById(R.id.formContainer);
        loadingIndicator = findViewById(R.id.progressBar);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        // Set click listeners
        resetPasswordButton.setOnClickListener(v-> handlePasswordReset());
        backToLoginText.setOnClickListener(v -> finish());
    }

    private void handlePasswordReset() {
        String email = emailInput.getText().toString().trim();

        // Validate email
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show success message
                        showSuccess(getString(R.string.success_reset_password));
                        backToLoginText.setEnabled(true);
                    } else {
                        // Show error
                        showError(getString(R.string.error_reset_password_failed));
                    }
                    setLoadingState(false);
                });
    }

    private void showError(String message) {
        errorText.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        errorText.setTextColor(getResources().getColor(android.R.color.white));
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String message) {
        errorText.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        errorText.setTextColor(getResources().getColor(android.R.color.white));
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void setLoadingState(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        formContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}