package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.network.dto.VenueResponse;

import java.util.List;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.ViewHolder> {

    public interface OnVenueClickListener {
        void onVenueClick(VenueResponse venue);
    }

    private final List<VenueResponse> venues;
    private final OnVenueClickListener listener;

    public VenueAdapter(List<VenueResponse> venues, OnVenueClickListener listener) {
        this.venues = venues;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VenueResponse venue = venues.get(position);
        holder.tvName.setText(venue.name);
        holder.tvAddress.setText(venue.address != null
                ? "📍 " + venue.address
                : "📍 Address not listed");
        holder.tvCategory.setText(venue.category != null
                ? formatCategory(venue.category)
                : "");

        if (venue.phone != null && !venue.phone.isEmpty()) {
            holder.tvPhone.setText("☎ " + venue.phone);
            holder.tvPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhone.setVisibility(View.GONE);
        }

        if (venue.website != null && !venue.website.isEmpty()) {
            holder.tvWebsite.setText("🌐 " + venue.website);
            holder.tvWebsite.setVisibility(View.VISIBLE);
        } else {
            holder.tvWebsite.setVisibility(View.GONE);
        }

        if (venue.openingHours != null && !venue.openingHours.isEmpty()) {
            holder.tvHours.setText("🕒 " + venue.openingHours);
            holder.tvHours.setVisibility(View.VISIBLE);
        } else {
            holder.tvHours.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onVenueClick(venue));
    }

    @Override
    public int getItemCount() {
        return venues.size();
    }

    /** Converts "conference_centre" → "Conference Centre" */
    private String formatCategory(String raw) {
        String spaced = raw.replace('_', ' ');
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvCategory, tvPhone, tvWebsite, tvHours;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvVenueName);
            tvAddress  = itemView.findViewById(R.id.tvVenueAddress);
            tvCategory = itemView.findViewById(R.id.tvVenueCategory);
            tvPhone    = itemView.findViewById(R.id.tvVenuePhone);
            tvWebsite  = itemView.findViewById(R.id.tvVenueWebsite);
            tvHours    = itemView.findViewById(R.id.tvVenueHours);
        }
    }
}
