package com.fuxdevs.dropndispense.user;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fuxdevs.dropndispense.R;
import com.fuxdevs.dropndispense.user.models.Parcel;
import com.fuxdevs.dropndispense.user.repository.ParcelRepository;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserParcelsFragment extends Fragment implements ParcelAdapter.OnParcelClickListener {
    private static final int BARCODE_SCAN_REQUEST = 1001;
    private RecyclerView parcelsRecyclerView;
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextInputEditText productNameInput, productPriceInput, trackingNumberInput, deliveryDateInput;
    private RadioButton radioLazada, radioShopee;
    private MaterialButton addParcelButton, saveParcelButton;
    private TextView bottomSheetTitle;
    private FirebaseAuth auth;
    private ParcelAdapter adapter;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormatter;
    private ParcelRepository parcelRepository;
    private String currentParcelId; // For editing mode

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_parcels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase and Repository
        auth = FirebaseAuth.getInstance();
        parcelRepository = new ParcelRepository();
        dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        // Initialize views
        initializeViews(view);
        setupBottomSheet();
        setupRecyclerView();
        setupDatePicker();
        observeParcels();

        // Set up click listeners
        addParcelButton.setOnClickListener(v -> {
            clearForm(); // Clear form before showing
            bottomSheetTitle.setText("Add New Parcel");
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        saveParcelButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveParcel();
            }
        });
    }

    private void initializeViews(View view) {
        parcelsRecyclerView = view.findViewById(R.id.parcels_recycler_view);
        bottomSheet = view.findViewById(R.id.bottom_sheet);
        addParcelButton = view.findViewById(R.id.add_parcel_button);
        saveParcelButton = view.findViewById(R.id.save_parcel_button);
        productNameInput = view.findViewById(R.id.product_name_input);
        productPriceInput = view.findViewById(R.id.product_price_input);
        trackingNumberInput = view.findViewById(R.id.tracking_number_input);
        deliveryDateInput = view.findViewById(R.id.delivery_date_input);
        radioLazada = view.findViewById(R.id.radio_lazada);
        radioShopee = view.findViewById(R.id.radio_shopee);
        bottomSheetTitle = view.findViewById(R.id.bottom_sheet_title);
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        // Make the bottom sheet interactive
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    clearForm();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Optional: Add slide animations here if needed
            }
        });

        // Set the bottom sheet to be fully expanded when shown
        bottomSheetBehavior.setFitToContents(true);
        bottomSheetBehavior.setSkipCollapsed(true);
        
        // Add click handler for the scrim (background) area
        View parent = (View) bottomSheet.getParent();
        parent.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
        
        // Prevent clicks on the bottom sheet from closing it
        bottomSheet.setOnClickListener(v -> {
            // Consume the click event
        });
    }

    private void setupRecyclerView() {
        adapter = new ParcelAdapter();
        adapter.setOnParcelClickListener(this);
        parcelsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        parcelsRecyclerView.setAdapter(adapter);
    }

    private void setupDatePicker() {
        selectedDate = Calendar.getInstance();
        
        deliveryDateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    deliveryDateInput.setText(dateFormatter.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void saveParcel() {
        String userId = auth.getCurrentUser().getUid();
        String productName = productNameInput.getText().toString().trim();
        String priceStr = productPriceInput.getText().toString().trim();
        String trackingNumber = trackingNumberInput.getText().toString().trim();
        String deliveryDate = deliveryDateInput.getText().toString().trim();
        String platform = radioLazada.isChecked() ? "lazada" : "shopee";

        if (productName.isEmpty() || priceStr.isEmpty() || trackingNumber.isEmpty() || deliveryDate.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        
        Parcel parcel = new Parcel(
            userId,
            productName,
            price,
            trackingNumber,
            deliveryDate, // Now using String in MM/DD/YYYY format
            platform,
            "pending"
        );

        if (currentParcelId != null) {
            // Update existing parcel
            Map<String, Object> updates = new HashMap<>();
            updates.put("productName", productName);
            updates.put("price", price);
            updates.put("trackingNumber", trackingNumber);
            updates.put("deliveryDate", deliveryDate); // String format
            updates.put("platform", platform);

            parcelRepository.updateParcel(currentParcelId, updates, new ParcelRepository.OnParcelCallback() {
                @Override
                public void onSuccess(Parcel parcel) {
                    Toast.makeText(getContext(), "Parcel updated successfully", Toast.LENGTH_SHORT).show();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    clearForm();
                    // Refresh the parcels list
                    observeParcels();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new parcel
            parcelRepository.createParcel(parcel, new ParcelRepository.OnParcelCallback() {
                @Override
                public void onSuccess(Parcel parcel) {
                    Toast.makeText(getContext(), "Parcel added successfully", Toast.LENGTH_SHORT).show();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    clearForm();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void clearForm() {
        currentParcelId = null;
        productNameInput.setText("");
        productPriceInput.setText("");
        trackingNumberInput.setText("");
        deliveryDateInput.setText("");
        radioLazada.setChecked(true);
        selectedDate = Calendar.getInstance();
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        if (productNameInput.getText().toString().trim().isEmpty()) {
            productNameInput.setError("Product name is required");
            isValid = false;
        }
        
        String priceStr = productPriceInput.getText().toString().trim();
        if (priceStr.isEmpty()) {
            productPriceInput.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    productPriceInput.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                productPriceInput.setError("Invalid price format");
                isValid = false;
            }
        }
        
        if (trackingNumberInput.getText().toString().trim().isEmpty()) {
            trackingNumberInput.setError("Tracking number is required");
            isValid = false;
        }
        
        if (deliveryDateInput.getText().toString().trim().isEmpty()) {
            deliveryDateInput.setError("Delivery date is required");
            isValid = false;
        }
        
        return isValid;
    }

    private void observeParcels() {
        String userId = auth.getCurrentUser().getUid();
        parcelRepository.getParcelsForUser(userId).observe(getViewLifecycleOwner(), parcels -> {
            adapter.updateParcels(parcels);
        });
    }

    @Override
    public void onParcelClick(Parcel parcel) {
        View itemView = parcelsRecyclerView.findViewWithTag(parcel.getId());
        PopupMenu popupMenu = new PopupMenu(requireContext(), itemView != null ? itemView : parcelsRecyclerView);
        popupMenu.getMenu().add("Edit");
        popupMenu.getMenu().add("Delete");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Edit":
                    showEditParcel(parcel);
                    return true;
                case "Delete":
                    showDeleteConfirmation(parcel);
                    return true;
                default:
                    return false;
            }
        });
        
        popupMenu.show();
    }

    private void showEditParcel(Parcel parcel) {
        currentParcelId = parcel.getId();
        productNameInput.setText(parcel.getProductName());
        productPriceInput.setText(String.format(Locale.getDefault(), "%.2f", parcel.getPrice()));
        trackingNumberInput.setText(parcel.getTrackingNumber());
        deliveryDateInput.setText(parcel.getDeliveryDate()); // Already in MM/DD/YYYY format
        
        // Parse the date string to set selectedDate for the date picker
        try {
            selectedDate.setTime(dateFormatter.parse(parcel.getDeliveryDate()));
        } catch (Exception e) {
            selectedDate = Calendar.getInstance();
        }
        
        if ("lazada".equals(parcel.getPlatform())) {
            radioLazada.setChecked(true);
        } else {
            radioShopee.setChecked(true);
        }

        bottomSheetTitle.setText("Edit Parcel");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void showDeleteConfirmation(Parcel parcel) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Parcel")
            .setMessage("Are you sure you want to delete this parcel?")
            .setPositiveButton("Delete", (dialog, which) -> {
                parcelRepository.deleteParcel(parcel.getId(), new ParcelRepository.OnParcelCallback() {
                    @Override
                    public void onSuccess(Parcel parcel) {
                        Toast.makeText(getContext(), "Parcel deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null);
            
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.WHITE);
        });
        dialog.show();
    }
}