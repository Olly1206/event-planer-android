package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.VendorAdapter;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.EventResponse;
import com.example.myapplication.network.dto.OrganizerSubscriptionResponse;
import com.example.myapplication.network.dto.ShortCodeResponse;
import com.example.myapplication.network.dto.SubscriptionPreferenceRequest;
import com.example.myapplication.network.dto.VendorResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows the full details of a single event.
 *
 * Receives extras:
 *   - "eventId" (long) — the event to display
 *
 * The "Share Invite Link" button is shown only when the backend returns an
 * inviteToken in the response (i.e. the requesting user is the organiser or
 * an admin).  Tapping it opens the system share sheet with a deep link.
 */
public class EventDetailActivity extends BaseActivity {

    private TextView tvTitle, tvOrganiser, tvDate, tvLocation, tvType,
                     tvStatus, tvParticipants, tvOptions, tvDescription, tvNoSelectedVendors;
    private MaterialButton btnShareInvite, btnEditEvent, btnDeleteEvent,
                           btnLeaveEvent, btnVendorSuggestions, btnShareCalendar,
                           btnExportParticipants, btnFollowOrganiser;
    private RecyclerView recyclerSelectedVendors;
    private final List<VendorResponse> selectedVendors = new ArrayList<>();
    private VendorAdapter selectedVendorAdapter;
    private long eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        tvTitle        = findViewById(R.id.tvDetailTitle);
        tvOrganiser    = findViewById(R.id.tvDetailOrganiser);
        tvDate         = findViewById(R.id.tvDetailDate);
        tvLocation     = findViewById(R.id.tvDetailLocation);
        tvType         = findViewById(R.id.tvDetailType);
        tvStatus       = findViewById(R.id.tvDetailStatus);
        tvParticipants = findViewById(R.id.tvDetailParticipants);
        tvOptions      = findViewById(R.id.tvDetailOptions);
        tvDescription  = findViewById(R.id.tvDetailDescription);
        btnShareInvite = findViewById(R.id.btnShareInvite);
        btnEditEvent   = findViewById(R.id.btnEditEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        btnLeaveEvent  = findViewById(R.id.btnLeaveEvent);
        btnVendorSuggestions = findViewById(R.id.btnVendorSuggestions);
        btnShareCalendar = findViewById(R.id.btnShareCalendar);
        btnExportParticipants = findViewById(R.id.btnExportParticipants);
        btnFollowOrganiser = findViewById(R.id.btnFollowOrganiser);
        tvNoSelectedVendors = findViewById(R.id.tvNoSelectedVendors);
        recyclerSelectedVendors = findViewById(R.id.recyclerSelectedVendors);

        selectedVendorAdapter = new VendorAdapter(selectedVendors, new VendorAdapter.Listener() {
            @Override
            public void onVendorClicked(VendorResponse vendor) {
                VendorDetailsDialog.show(EventDetailActivity.this, vendor);
            }

            @Override
            public void onVendorActionClicked(VendorResponse vendor, boolean alreadyAdded) {
            }
        });
        recyclerSelectedVendors.setLayoutManager(new LinearLayoutManager(this));
        recyclerSelectedVendors.setNestedScrollingEnabled(false);
        recyclerSelectedVendors.setAdapter(selectedVendorAdapter);

        eventId = getIntent().getLongExtra("eventId", -1);
        if (eventId == -1) {
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventId != -1) {
            loadEvent(eventId);
        }
    }

    private void loadEvent(long eventId) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getEventById(eventId).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateUI(response.body());
                } else {
                    Toast.makeText(EventDetailActivity.this,
                            "Failed to load event (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void populateUI(EventResponse event) {
        tvTitle.setText(event.title);
        tvOrganiser.setText("Organiser: " + event.organiserUsername);
        tvDate.setText("Date: " + event.eventDate
                + (event.eventEndDate != null ? " → " + event.eventEndDate : ""));
        String locationLabel = event.venueName != null ? event.venueName
            : event.locationName != null ? event.locationName
            : "—";
        String locationDetails = event.venueAddress != null ? "\n" + event.venueAddress : "";
        tvLocation.setText("Location: " + locationLabel + locationDetails
            + " (" + event.locationType + ")");
        tvType.setText("Type: " + (event.eventTypeName != null ? event.eventTypeName : "—"));
        tvStatus.setText("Status: " + event.status);

        String capacityText = event.currentParticipantCount
                + (event.maxParticipants != null ? "/" + event.maxParticipants : "") + " participants";
        tvParticipants.setText(capacityText);

        if (event.selectedOptions != null && !event.selectedOptions.isEmpty()) {
            tvOptions.setText("Options: " + String.join(", ", event.selectedOptions));
            tvOptions.setVisibility(View.VISIBLE);
        } else {
            tvOptions.setVisibility(View.GONE);
        }

        selectedVendors.clear();
        if (event.selectedVendors != null) {
            selectedVendors.addAll(event.selectedVendors);
        }
        selectedVendorAdapter.notifyDataSetChanged();
        if (selectedVendors.isEmpty()) {
            recyclerSelectedVendors.setVisibility(View.GONE);
            tvNoSelectedVendors.setVisibility(View.VISIBLE);
        } else {
            recyclerSelectedVendors.setVisibility(View.VISIBLE);
            tvNoSelectedVendors.setVisibility(View.GONE);
        }

        if (event.description != null && !event.description.isEmpty()) {
            tvDescription.setText(event.description);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        Long currentUserId = AuthManager.getInstance(this).getUserId();
        boolean isOrganiser = event.organiserId != null
                && event.organiserId.equals(currentUserId);
        boolean isAdmin = event.isAdmin != null && event.isAdmin;
        boolean canManage = isOrganiser || isAdmin;

        // Share invite button — shown when backend granted invite token (organiser/admin)
        if (event.inviteToken != null) {
            btnShareInvite.setVisibility(View.VISIBLE);
            btnShareInvite.setOnClickListener(v -> shareInviteLink(event.id));
        } else {
            btnShareInvite.setVisibility(View.GONE);
        }

        btnShareCalendar.setOnClickListener(v -> shareEventCalendar(event.id));

        if (!isOrganiser && event.organiserId != null) {
            btnFollowOrganiser.setVisibility(View.VISIBLE);
            btnFollowOrganiser.setText("Follow " + event.organiserUsername);
            btnFollowOrganiser.setOnClickListener(v -> followOrganiser(event.organiserId));
        } else {
            btnFollowOrganiser.setVisibility(View.GONE);
        }

        // Edit button — organiser or admin
        if (canManage) {
            btnEditEvent.setVisibility(View.VISIBLE);
            btnExportParticipants.setVisibility(View.VISIBLE);
            btnExportParticipants.setOnClickListener(v -> shareParticipantCsv(event.id));
            btnEditEvent.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditEventActivity.class);
                intent.putExtra("eventId", eventId);
                intent.putExtra("title", event.title);
                intent.putExtra("description", event.description);
                intent.putExtra("eventDate", event.eventDate);
                intent.putExtra("eventEndDate", event.eventEndDate);
                intent.putExtra("locationName", event.locationName);
                intent.putExtra("venueName", event.venueName);
                intent.putExtra("locationType", event.locationType);
                intent.putExtra("visibility", event.visibility);
                intent.putExtra("status", event.status);
                if (event.maxParticipants != null)
                    intent.putExtra("maxParticipants", event.maxParticipants.intValue());
                ArrayList<String> selectedOptions = event.selectedOptions != null
                        ? new ArrayList<>(event.selectedOptions)
                        : new ArrayList<>();
                intent.putStringArrayListExtra(VendorSuggestionsActivity.EXTRA_OPTIONS, selectedOptions);

                String cityName = event.locationName != null ? event.locationName : "";
                if (cityName.contains("(")) {
                    cityName = cityName.substring(0, cityName.indexOf("(")).trim();
                }
                intent.putExtra(VendorSuggestionsActivity.EXTRA_CITY, cityName);

                long[] selectedIds = selectedVendors.stream()
                        .filter(vendor -> vendor.osmId != null)
                        .mapToLong(vendor -> vendor.osmId)
                        .toArray();
                intent.putExtra(VendorSuggestionsActivity.EXTRA_SELECTED_VENDOR_IDS, selectedIds);
                startActivity(intent);
            });
        }

        // Vendor suggestions — when event has options and a location
        if (event.selectedOptions != null && !event.selectedOptions.isEmpty()
                && event.locationName != null && !event.locationName.isEmpty()) {
            btnVendorSuggestions.setVisibility(View.VISIBLE);
            btnVendorSuggestions.setText(canManage ? "Add Vendors" : "Vendor Suggestions");
            btnVendorSuggestions.setOnClickListener(v -> {
                Intent intent = new Intent(this, VendorSuggestionsActivity.class);
                // Extract clean city name from locationName (before the radius part in parentheses)
                String cityName = event.locationName;
                if (cityName.contains("(")) {
                    cityName = cityName.substring(0, cityName.indexOf("(")).trim();
                }
                intent.putExtra(VendorSuggestionsActivity.EXTRA_CITY, cityName);
                intent.putExtra(VendorSuggestionsActivity.EXTRA_EVENT_ID, event.id);
                intent.putExtra(VendorSuggestionsActivity.EXTRA_ALLOW_ADD, canManage);
                intent.putStringArrayListExtra(VendorSuggestionsActivity.EXTRA_OPTIONS,
                        new ArrayList<>(event.selectedOptions));
                long[] selectedIds = selectedVendors.stream()
                        .filter(vendor -> vendor.osmId != null)
                        .mapToLong(vendor -> vendor.osmId)
                        .toArray();
                intent.putExtra(VendorSuggestionsActivity.EXTRA_SELECTED_VENDOR_IDS, selectedIds);
                startActivity(intent);
            });
        } else {
            btnVendorSuggestions.setVisibility(View.GONE);
        }

        // Delete button — organiser or admin
        if (canManage) {
            btnDeleteEvent.setVisibility(View.VISIBLE);
            btnDeleteEvent.setOnClickListener(v -> confirmDelete());
        } else {
            btnDeleteEvent.setVisibility(View.GONE);
            btnEditEvent.setVisibility(View.GONE);
            btnExportParticipants.setVisibility(View.GONE);
        }

        // Leave button — participant who is NOT the organiser
        if (!isOrganiser) {
            btnLeaveEvent.setVisibility(View.VISIBLE);
            btnLeaveEvent.setOnClickListener(v -> confirmLeave());
        } else {
            btnLeaveEvent.setVisibility(View.GONE);
        }
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .show();
    }

    private void deleteEvent() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.deleteEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailActivity.this,
                            "Event deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EventDetailActivity.this,
                            "Failed to delete (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this,
                        "Cannot reach server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmLeave() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Leave Event")
                .setMessage("Are you sure you want to leave this event?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Leave", (dialog, which) -> leaveEvent())
                .show();
    }

    private void leaveEvent() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.leaveEvent(eventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EventDetailActivity.this,
                            "Left event", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EventDetailActivity.this,
                            "Failed to leave (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this,
                        "Cannot reach server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareInviteLink(Long eventId) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        
        // Fetch the short code for safer sharing (avoids WAF blocks on long UUIDs)
        api.getShortInviteCode(eventId).enqueue(new Callback<ShortCodeResponse>() {
            @Override
            public void onResponse(Call<ShortCodeResponse> call, Response<ShortCodeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String shortCode = response.body().getShortCode();
                    String shortInviteUrl = RetrofitClient.getBaseUrl() + "s/" + shortCode;
                    
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                            "Join my event! Open this link: " + shortInviteUrl);
                    startActivity(Intent.createChooser(shareIntent, "Share invite via"));
                } else {
                    Toast.makeText(EventDetailActivity.this,
                            "Failed to generate share link (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShortCodeResponse> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void followOrganiser(Long organiserId) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        SubscriptionPreferenceRequest request = new SubscriptionPreferenceRequest(true, true, 1440);
        api.subscribeToOrganiser(organiserId, request).enqueue(new Callback<OrganizerSubscriptionResponse>() {
            @Override
            public void onResponse(Call<OrganizerSubscriptionResponse> call,
                                   Response<OrganizerSubscriptionResponse> response) {
                if (response.isSuccessful()) {
                    btnFollowOrganiser.setText("Following");
                    btnFollowOrganiser.setEnabled(false);
                    Toast.makeText(EventDetailActivity.this,
                            "Organiser followed", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 409 || response.code() == 400) {
                    btnFollowOrganiser.setText("Following");
                    btnFollowOrganiser.setEnabled(false);
                    Toast.makeText(EventDetailActivity.this,
                            "Already following this organiser", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401 || response.code() == 403) {
                    Toast.makeText(EventDetailActivity.this,
                            "Session expired — please log in again", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EventDetailActivity.this,
                            "Could not follow organiser (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrganizerSubscriptionResponse> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareEventCalendar(Long eventId) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.exportEventCalendar(eventId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shareResponseBody(response.body(), "text/calendar", "event-" + eventId + ".ics",
                            "Share calendar file via");
                } else {
                    Toast.makeText(EventDetailActivity.this,
                            "Could not export calendar (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareParticipantCsv(Long eventId) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.exportParticipantsCsv(eventId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shareResponseBody(response.body(), "text/csv", "event-" + eventId + "-participants.csv",
                            "Share participant CSV via");
                } else {
                    Toast.makeText(EventDetailActivity.this,
                            "Could not export participants (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareResponseBody(ResponseBody body, String mimeType, String fileName, String chooserTitle) {
        try {
            String content = body.string();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
            shareIntent.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(Intent.createChooser(shareIntent, chooserTitle));
        } catch (Exception e) {
            Toast.makeText(this, "Could not prepare export", Toast.LENGTH_SHORT).show();
        }
    }
}
