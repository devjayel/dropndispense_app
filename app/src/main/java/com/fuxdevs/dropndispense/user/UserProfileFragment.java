package com.fuxdevs.dropndispense.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fuxdevs.dropndispense.R;
import com.fuxdevs.dropndispense.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileFragment extends Fragment {
    private TextView userName, userEmail;
    private TextInputEditText nameInput, phoneInput, emailInput, currentPasswordInput, newPasswordInput;
    private MaterialButton saveButton, logoutButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        userName = view.findViewById(R.id.user_name);
        userEmail = view.findViewById(R.id.user_email);
        nameInput = view.findViewById(R.id.name_input);
        phoneInput = view.findViewById(R.id.phone_input);
        emailInput = view.findViewById(R.id.email_input);
        currentPasswordInput = view.findViewById(R.id.current_password_input);
        newPasswordInput = view.findViewById(R.id.new_password_input);
        saveButton = view.findViewById(R.id.save_button);
        logoutButton = view.findViewById(R.id.logout_button);

        // Load user data
        loadUserData();

        // Set up click listeners
        saveButton.setOnClickListener(v -> saveUserData());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String currentEmail = user.getEmail();
            userEmail.setText(currentEmail);
            emailInput.setText(currentEmail);

            // Get additional user data from Firestore
            db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");

                        userName.setText(name);
                        nameInput.setText(name);
                        phoneInput.setText(phone);
                    }
                });
        }
    }

    private void saveUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String currentPassword = currentPasswordInput.getText().toString();
        String newPassword = newPasswordInput.getText().toString();

        // Update email if changed
        if (!email.equals(user.getEmail())) {
            user.updateEmail(email)
                .addOnSuccessListener(aVoid -> {
                    userEmail.setText(email);
                    Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // Update profile data in Firestore
        db.collection("users").document(user.getUid())
            .update("name", name, "phone", phone)
            .addOnSuccessListener(aVoid -> {
                // If new password is provided, update it
                if (!newPassword.isEmpty() && !currentPassword.isEmpty()) {
                    // Reauthenticate before changing password
                    user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            currentPasswordInput.setText("");
                            newPasswordInput.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update password", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        auth.signOut();
        // Navigate to login screen
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}