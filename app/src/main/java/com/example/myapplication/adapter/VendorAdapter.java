package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.network.dto.VendorResponse;

import java.util.List;

public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.ViewHolder> {

    private final List<VendorResponse> vendors;

    public VendorAdapter(List<VendorResponse> vendors) {
        this.vendors = vendors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vendor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VendorResponse vendor = vendors.get(position);

        holder.tvName.setText(vendor.name != null ? vendor.name : "Unnamed vendor");
        holder.tvAddress.setText(vendor.address != null
                ? "\uD83D\uDCCD " + vendor.address : "\uD83D\uDCCD Address not listed");

        if (vendor.category != null) {
            String label = vendor.optionName != null ? vendor.optionName : formatCategory(vendor.category);
            holder.tvCategory.setText(label);
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        if (vendor.phone != null && !vendor.phone.isEmpty()) {
            holder.tvPhone.setText("\u260E " + vendor.phone);
            holder.tvPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhone.setVisibility(View.GONE);
        }

        if (vendor.website != null && !vendor.website.isEmpty()) {
            holder.tvWebsite.setText("\uD83C\uDF10 " + vendor.website);
            holder.tvWebsite.setVisibility(View.VISIBLE);
        } else {
            holder.tvWebsite.setVisibility(View.GONE);
        }

        if (vendor.openingHours != null && !vendor.openingHours.isEmpty()) {
            holder.tvHours.setText("\uD83D\uDD52 " + vendor.openingHours);
            holder.tvHours.setVisibility(View.VISIBLE);
        } else {
            holder.tvHours.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return vendors.size();
    }

    private String formatCategory(String raw) {
        String spaced = raw.replace('_', ' ');
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvAddress, tvPhone, tvWebsite, tvHours;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvVendorName);
            tvCategory = itemView.findViewById(R.id.tvVendorCategory);
            tvAddress  = itemView.findViewById(R.id.tvVendorAddress);
            tvPhone    = itemView.findViewById(R.id.tvVendorPhone);
            tvWebsite  = itemView.findViewById(R.id.tvVendorWebsite);
            tvHours    = itemView.findViewById(R.id.tvVendorHours);
        }
    }
}
