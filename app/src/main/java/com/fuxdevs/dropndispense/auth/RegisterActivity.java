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
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputEditText serialNumberInput;
    private Button registerButton;
    private TextView loginText;
    private TextView errorText;
    private View formContainer;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        serialNumberInput = findViewById(R.id.hardwareSerialInput);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginButton);
        errorText = findViewById(R.id.errorText);
        formContainer = findViewById(R.id.formContainer);
        loadingIndicator = findViewById(R.id.progressBar);

        // Set click listeners
        registerButton.setOnClickListener(v -> handleRegistration());
        loginText.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String serialNumber = serialNumberInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            showError("Name is required");
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.error_invalid_email));
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            showError(getString(R.string.error_password_short));
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(getString(R.string.error_passwords_dont_match));
            return;
        }

        if (serialNumber.isEmpty()) {
            showError("Hardware serial number is required");
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Check if hardware serial exists and is not used
        verifyHardwareSerial(serialNumber, name, email, password);
    }

    private void verifyHardwareSerial(String serialNumber, String name, String email, String password) {
        db.collection("hardwares")
                .whereEqualTo("serial", serialNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Check if hardware is already used
                        boolean isUsed = task.getResult().getDocuments().get(0).getBoolean("isUsed");
                        if (!isUsed) {
                            // Hardware exists and is not used, proceed with registration
                            createAccount(name, email, password, serialNumber);
                        } else {
                            showError(getString(R.string.error_hardware_serial_used));
                            setLoadingState(false);
                        }
                    } else {
                        showError(getString(R.string.error_hardware_serial_invalid));
                        setLoadingState(false);
                    }
                });
    }

    private void createAccount(String name, String email, String password, String serialNumber) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save user data to Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        saveUserData(userId, name, email, serialNumber);
                    } else {
                        showError(getString(R.string.error_registration_failed));
                        setLoadingState(false);
                    }
                });
    }

    private void saveUserData(String userId, String name, String email, String serialNumber) {
        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                String fcmToken = "";
                if (task.isSuccessful() && task.getResult() != null) {
                    fcmToken = task.getResult();
                }
                
                Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("email", email);
                user.put("hardwareSerial", serialNumber);
                user.put("user_fcm_token", fcmToken);

                // Update user data in Firestore
                String finalFcmToken = fcmToken;
                db.collection("users").document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        // Update hardware status
                        updateHardwareStatus(serialNumber, finalFcmToken);
                    })
                    .addOnFailureListener(e -> {
                        showError(getString(R.string.error_registration_failed));
                        setLoadingState(false);
                    });
            });
    }

    private void updateHardwareStatus(String serialNumber, String fcmToken) {
        db.collection("hardwares")
                .whereEqualTo("serial", serialNumber)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String hardwareId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("isUsed", true);
                    updates.put("user_fcm_token", fcmToken);
                    
                    db.collection("hardwares").document(hardwareId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> navigateToMain())
                            .addOnFailureListener(e -> {
                                showError(getString(R.string.error_registration_failed));
                                setLoadingState(false);
                            });
                });
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