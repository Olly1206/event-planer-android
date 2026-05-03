package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.network.dto.EventResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Set;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(EventResponse event);
    }

    public interface OnJoinEventListener {
        void onJoinClick(EventResponse event);
    }

    private final List<EventResponse> events;
    private final OnEventClickListener listener;
    private OnJoinEventListener joinListener;
    private Set<Long> joinedEventIds;

    public EventAdapter(List<EventResponse> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
        this.joinListener = null;
    }

    public EventAdapter(List<EventResponse> events, OnEventClickListener listener, OnJoinEventListener joinListener) {
        this.events = events;
        this.listener = listener;
        this.joinListener = joinListener;
    }

    public void setJoinListener(OnJoinEventListener joinListener) {
        this.joinListener = joinListener;
    }

    public void setJoinedEventIds(Set<Long> joinedEventIds) {
        this.joinedEventIds = joinedEventIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventResponse event = events.get(position);

        holder.tvTitle.setText(event.title != null ? event.title : "Untitled");
        holder.tvStatus.setText(event.status != null ? event.status : "");
        holder.tvEventType.setText(event.eventTypeName != null ? event.eventTypeName : "General");
        holder.tvOrganiser.setText("Organised by: " + (event.organiserUsername != null ? event.organiserUsername : "Unknown"));

        // Format the date — backend sends ISO-8601, trim to just the date part for display
        if (event.eventDate != null) {
            String displayDate = event.eventDate.length() > 10
                    ? event.eventDate.substring(0, 10)
                    : event.eventDate;
            holder.tvDate.setText("📅 " + displayDate);
        } else {
            holder.tvDate.setText("📅 No date set");
        }

        String locationLabel = event.venueName != null ? event.venueName
            : event.locationName != null ? event.locationName
            : "No location";
        String locationDetail = event.venueAddress != null && !event.venueAddress.isEmpty()
            ? "\n" + event.venueAddress
            : "";
        holder.tvLocation.setText("📍 " + locationLabel + locationDetail);

        holder.tvParticipants.setText("👥 " + event.currentParticipantCount
                + (event.maxParticipants != null ? "/" + event.maxParticipants : ""));

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));

        if (joinListener != null) {
            boolean alreadyJoined = event.id != null
                    && joinedEventIds != null
                    && joinedEventIds.contains(event.id);

            holder.btnJoinEvent.setVisibility(View.VISIBLE);
            holder.btnJoinEvent.setEnabled(!alreadyJoined);
            holder.btnJoinEvent.setText(alreadyJoined ? "Joined" : "Join Event");
            holder.btnJoinEvent.setOnClickListener(alreadyJoined ? null : v -> joinListener.onJoinClick(event));
        } else {
            holder.btnJoinEvent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvEventType, tvDate, tvLocation, tvParticipants, tvOrganiser;
        MaterialButton btnJoinEvent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tvEventTitle);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
            tvEventType    = itemView.findViewById(R.id.tvEventType);
            tvDate         = itemView.findViewById(R.id.tvEventDate);
            tvLocation     = itemView.findViewById(R.id.tvLocation);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            tvOrganiser    = itemView.findViewById(R.id.tvOrganiser);
            btnJoinEvent   = itemView.findViewById(R.id.btnJoinEvent);
        }
    }
}
