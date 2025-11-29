package com.fuxdevs.dropndispense.user;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.fuxdevs.dropndispense.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import android.widget.TextView;

public class UserHomeActivity extends AppCompatActivity {
    private TextView pendingCount;

    private TextView deliveredCount;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_home);
        
        // Initialize views
        pendingCount = findViewById(R.id.pending_count);
        deliveredCount = findViewById(R.id.delivered_count);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Load parcel analytics
        loadParcelAnalytics();
    }

    private void loadParcelAnalytics() {
        String userId = auth.getCurrentUser().getUid();
        
        // Query pending parcels
        db.collection("parcels")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                pendingCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });

        // Query delivered parcels
        db.collection("parcels")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "delivered")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                deliveredCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });
    }
}