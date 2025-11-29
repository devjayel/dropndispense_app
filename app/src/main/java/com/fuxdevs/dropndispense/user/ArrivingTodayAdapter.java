package com.fuxdevs.dropndispense.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fuxdevs.dropndispense.R;
import com.fuxdevs.dropndispense.user.models.Parcel;

import java.util.ArrayList;
import java.util.List;

public class ArrivingTodayAdapter extends RecyclerView.Adapter<ArrivingTodayAdapter.ViewHolder> {
    private List<Parcel> parcels = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_arriving_today, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Parcel parcel = parcels.get(position);
        holder.productName.setText(parcel.getProductName());
        holder.trackingNumber.setText(parcel.getTrackingNumber());
        holder.platform.setText(parcel.getPlatform());
    }

    @Override
    public int getItemCount() {
        return parcels.size();
    }

    public void updateParcels(List<Parcel> newParcels) {
        parcels.clear();
        parcels.addAll(newParcels);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, trackingNumber, platform;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            trackingNumber = itemView.findViewById(R.id.tracking_number);
            platform = itemView.findViewById(R.id.platform);
        }
    }
}