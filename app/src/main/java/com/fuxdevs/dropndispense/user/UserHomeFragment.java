package com.fuxdevs.dropndispense.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fuxdevs.dropndispense.R;
import com.fuxdevs.dropndispense.user.models.Parcel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UserHomeFragment extends Fragment {
    private TextView pendingCount;
    private TextView deliveredCount;
    private com.google.android.material.card.MaterialCardView arrivingTodayCard;
    private TextView arrivingProductName, arrivingTrackingNumber, arrivingPlatform, arrivingPrice;
    private TextView noArrivalsText;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        pendingCount = view.findViewById(R.id.pending_count);
        deliveredCount = view.findViewById(R.id.delivered_count);
        arrivingTodayCard = view.findViewById(R.id.arriving_today_card);
        arrivingProductName = view.findViewById(R.id.arriving_product_name);
        arrivingTrackingNumber = view.findViewById(R.id.arriving_tracking_number);
        arrivingPlatform = view.findViewById(R.id.arriving_platform);
        arrivingPrice = view.findViewById(R.id.arriving_price);
        noArrivalsText = view.findViewById(R.id.no_arrivals_text);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Load data
        loadParcelAnalytics();
        loadArrivingToday();
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

    private void loadArrivingToday() {
        String userId = auth.getCurrentUser().getUid();
        
        // Get today's date in MM/DD/YYYY format
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String todayDate = dateFormatter.format(Calendar.getInstance().getTime());

        // Query parcels arriving today (limit to 1)
        db.collection("parcels")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "pending")
            .whereEqualTo("deliveryDate", todayDate)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    // No parcels arriving today
                    arrivingTodayCard.setVisibility(View.GONE);
                    noArrivalsText.setVisibility(View.VISIBLE);
                } else {
                    // Show the first parcel
                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                    Parcel parcel = doc.toObject(Parcel.class);
                    if (parcel != null) {
                        arrivingProductName.setText(parcel.getProductName());
                        arrivingTrackingNumber.setText("Tracking: " + parcel.getTrackingNumber());
                        arrivingPlatform.setText(parcel.getPlatform());
                        arrivingPrice.setText(String.format("₱%.2f", parcel.getPrice()));
                        
                        arrivingTodayCard.setVisibility(View.VISIBLE);
                        noArrivalsText.setVisibility(View.GONE);
                    }
                }
            });
    }
}