package com.fuxdevs.dropndispense.user.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fuxdevs.dropndispense.user.models.Parcel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParcelRepository {
    private static final String TAG = "ParcelRepository";
    private final FirebaseFirestore db;
    private final String COLLECTION_NAME = "parcels";

    public ParcelRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // Create Operation
    public void createParcel(Parcel parcel, OnParcelCallback callback) {
        Map<String, Object> parcelData = new HashMap<>();
        parcelData.put("userId", parcel.getUserId());
        parcelData.put("productName", parcel.getProductName());
        parcelData.put("price", parcel.getPrice());
        parcelData.put("trackingNumber", parcel.getTrackingNumber());
        parcelData.put("deliveryDate", parcel.getDeliveryDate());
        parcelData.put("platform", parcel.getPlatform());
        parcelData.put("status", parcel.getStatus());
        parcelData.put("createdAt", parcel.getCreatedAt());

        db.collection(COLLECTION_NAME)
            .add(parcelData)
            .addOnSuccessListener(documentReference -> {
                parcel.setId(documentReference.getId());
                callback.onSuccess(parcel);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating parcel", e);
                callback.onError(e.getMessage());
            });
    }

    // Read Operation - Get All Parcels for User
    public LiveData<List<Parcel>> getParcelsForUser(String userId) {
        MutableLiveData<List<Parcel>> parcelsLiveData = new MutableLiveData<>();

        db.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error getting parcels", error);
                    return;
                }

                List<Parcel> parcels = new ArrayList<>();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Parcel parcel = doc.toObject(Parcel.class);
                        parcel.setId(doc.getId());
                        parcels.add(parcel);
                    }
                }
                parcelsLiveData.setValue(parcels);
            });

        return parcelsLiveData;
    }

    // Read Operation - Get Single Parcel
    public void getParcel(String parcelId, OnParcelCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(parcelId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Parcel parcel = documentSnapshot.toObject(Parcel.class);
                    if (parcel != null) {
                        parcel.setId(documentSnapshot.getId());
                        callback.onSuccess(parcel);
                    }
                } else {
                    callback.onError("Parcel not found");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting parcel", e);
                callback.onError(e.getMessage());
            });
    }

    // Update Operation
    public void updateParcel(String parcelId, Map<String, Object> updates, OnParcelCallback callback) {
        DocumentReference parcelRef = db.collection(COLLECTION_NAME).document(parcelId);
        
        parcelRef.update(updates)
            .addOnSuccessListener(aVoid -> {
                parcelRef.get().addOnSuccessListener(documentSnapshot -> {
                    Parcel updatedParcel = documentSnapshot.toObject(Parcel.class);
                    if (updatedParcel != null) {
                        updatedParcel.setId(documentSnapshot.getId());
                        callback.onSuccess(updatedParcel);
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating parcel", e);
                callback.onError(e.getMessage());
            });
    }

    // Delete Operation
    public void deleteParcel(String parcelId, OnParcelCallback callback) {
        db.collection(COLLECTION_NAME)
            .document(parcelId)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting parcel", e);
                callback.onError(e.getMessage());
            });
    }

    // Update Parcel Status
    public void updateParcelStatus(String parcelId, String newStatus, OnParcelCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        
        updateParcel(parcelId, updates, callback);
    }

    // Interface for callbacks
    public interface OnParcelCallback {
        void onSuccess(Parcel parcel);
        void onError(String errorMessage);
    }
}