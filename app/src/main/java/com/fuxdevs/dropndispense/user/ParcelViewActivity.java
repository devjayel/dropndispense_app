package com.fuxdevs.dropndispense.user;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fuxdevs.dropndispense.R;
import com.fuxdevs.dropndispense.user.models.Parcel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class ParcelViewActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private ProgressBar loadingIndicator;
    private MaterialCardView parcelCard;
    private TextView noParcelText;
    private TextView productName;
    private TextView trackingNumber;
    private TextView deliveryDate;
    private TextView price;
    private TextView platform;
    private TextView status;
    
    private String parcelTrackNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_view);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Parcel Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        loadingIndicator = findViewById(R.id.loading_indicator);
        parcelCard = findViewById(R.id.parcel_card);
        noParcelText = findViewById(R.id.no_parcel_text);
        productName = findViewById(R.id.product_name);
        trackingNumber = findViewById(R.id.tracking_number);
        deliveryDate = findViewById(R.id.delivery_date);
        price = findViewById(R.id.price);
        platform = findViewById(R.id.platform);
        status = findViewById(R.id.status);

        // Get parcel tracking number from intent
        parcelTrackNo = getIntent().getStringExtra("parcel_track_no");

        if (parcelTrackNo != null && !parcelTrackNo.isEmpty()) {
            loadParcelDetails(parcelTrackNo);
        } else {
            showError("No tracking number provided");
        }
    }

    private void loadParcelDetails(String trackingNo) {
        loadingIndicator.setVisibility(View.VISIBLE);
        parcelCard.setVisibility(View.GONE);
        noParcelText.setVisibility(View.GONE);

        String userId = auth.getCurrentUser().getUid();

        db.collection("parcels")
            .whereEqualTo("userId", userId)
            .whereEqualTo("trackingNumber", trackingNo)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                loadingIndicator.setVisibility(View.GONE);
                
                if (!queryDocumentSnapshots.isEmpty()) {
                    Parcel parcel = queryDocumentSnapshots.getDocuments().get(0).toObject(Parcel.class);
                    if (parcel != null) {
                        displayParcelDetails(parcel);
                    } else {
                        showError("Parcel not found");
                    }
                } else {
                    showError("Parcel not found");
                }
            })
            .addOnFailureListener(e -> {
                loadingIndicator.setVisibility(View.GONE);
                showError("Error loading parcel: " + e.getMessage());
            });
    }

    private void displayParcelDetails(Parcel parcel) {
        parcelCard.setVisibility(View.VISIBLE);
        noParcelText.setVisibility(View.GONE);

        productName.setText(parcel.getProductName());
        trackingNumber.setText("Tracking: " + parcel.getTrackingNumber());
        deliveryDate.setText("Delivery Date: " + parcel.getDeliveryDate());
        price.setText(String.format(Locale.getDefault(), "₱%.2f", parcel.getPrice()));
        platform.setText("Platform: " + parcel.getPlatform().toUpperCase());
        
        String statusText = parcel.getStatus();
        if (statusText != null && !statusText.isEmpty()) {
            status.setText("Status: " + statusText.substring(0, 1).toUpperCase() + statusText.substring(1).replace("_", " "));
        }
    }

    private void showError(String message) {
        parcelCard.setVisibility(View.GONE);
        noParcelText.setVisibility(View.VISIBLE);
        noParcelText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
