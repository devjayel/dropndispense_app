package com.fuxdevs.dropndispense.user;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fuxdevs.dropndispense.R;
import com.fuxdevs.dropndispense.user.models.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ParcelViewHolder> {
    private List<Parcel> parcels = new ArrayList<>();
    private OnParcelClickListener listener;

    public interface OnParcelClickListener {
        void onParcelClick(Parcel parcel);
    }

    public void setOnParcelClickListener(OnParcelClickListener listener) {
        this.listener = listener;
    }

    public void updateParcels(List<Parcel> newParcels) {
        parcels = newParcels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParcelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parcel, parent, false);
        return new ParcelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcelViewHolder holder, int position) {
        Parcel parcel = parcels.get(position);
        
        // Set views tag for popup menu
        holder.itemView.setTag(parcel.getId());
        
        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onParcelClick(parcel);
            }
        });
        
        // Set product name
        holder.productName.setText(parcel.getProductName());
        
        // Set tracking number
        holder.trackingNumber.setText(parcel.getTrackingNumber());
        
        // Set delivery date
        if (parcel.getDeliveryDate() != null) {
            holder.deliveryDate.setText("Est. " + parcel.getDeliveryDate()); // Already in MM/DD/YYYY format
        }
        
        // Set price
        holder.price.setText(String.format(Locale.getDefault(), "₱%.2f", parcel.getPrice()));
        
        // Set platform icon
        String platform = parcel.getPlatform();
        if ("lazada".equals(platform)) {
            holder.platformIcon.setImageResource(R.drawable.ic_lazada);
        } else {
            holder.platformIcon.setImageResource(R.drawable.ic_shopee);
        }
        
        // Set status badge
        String status = parcel.getStatus();
        holder.statusBadge.setText(status != null ? status.substring(0, 1).toUpperCase() + status.substring(1) : "");
        
        int statusColor;
        int statusBgColor;
        switch (status != null ? status : "") {
            case "pending":
                statusColor = R.color.warning_dark;
                statusBgColor = R.color.warning_light;
                break;
            case "delivered":
                statusColor = R.color.success_dark;
                statusBgColor = R.color.success_light;
                break;
            default:
                statusColor = R.color.gray_600;
                statusBgColor = R.color.gray_100;
                break;
        }
        
        holder.statusBadge.setTextColor(holder.itemView.getContext().getColor(statusColor));
        holder.statusBadge.setBackgroundTintList(holder.itemView.getContext().getColorStateList(statusBgColor));
    }

    @Override
    public int getItemCount() {
        return parcels.size();
    }

    class ParcelViewHolder extends RecyclerView.ViewHolder {
        ImageView platformIcon;
        TextView productName;
        TextView trackingNumber;
        TextView statusBadge;
        TextView deliveryDate;
        TextView price;

        ParcelViewHolder(@NonNull View itemView) {
            super(itemView);
            platformIcon = itemView.findViewById(R.id.platform_icon);
            productName = itemView.findViewById(R.id.product_name);
            trackingNumber = itemView.findViewById(R.id.tracking_number);
            statusBadge = itemView.findViewById(R.id.status_badge);
            deliveryDate = itemView.findViewById(R.id.delivery_date);
            price = itemView.findViewById(R.id.price);
        }
    }
}