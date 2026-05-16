package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.EventDashboardItemResponse;
import com.example.myapplication.network.dto.EventDashboardResponse;
import com.google.android.material.button.MaterialButton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrganizerDashboardActivity extends BaseActivity {

    private TextView tvSubtitle;
    private TextView tvMetrics;
    private TextView tvEvents;
    private MaterialButton btnShareCalendar;
    private long organiserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_dashboard);

        tvSubtitle = findViewById(R.id.tvDashboardSubtitle);
        tvMetrics = findViewById(R.id.tvDashboardMetrics);
        tvEvents = findViewById(R.id.tvDashboardEvents);
        btnShareCalendar = findViewById(R.id.btnShareOrganiserCalendar);

        organiserId = AuthManager.getInstance(this).getUserId();
        tvSubtitle.setText(AuthManager.getInstance(this).getUsername());
        btnShareCalendar.setOnClickListener(v -> shareOrganiserCalendar());
        loadDashboard();
    }

    private void loadDashboard() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getOrganiserDashboard(organiserId).enqueue(new Callback<EventDashboardResponse>() {
            @Override
            public void onResponse(Call<EventDashboardResponse> call,
                                   Response<EventDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    renderDashboard(response.body());
                } else {
                    Toast.makeText(OrganizerDashboardActivity.this,
                            "Could not load dashboard (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventDashboardResponse> call, Throwable t) {
                Toast.makeText(OrganizerDashboardActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderDashboard(EventDashboardResponse dashboard) {
        tvMetrics.setText(
                "Followers: " + dashboard.followerCount + "\n"
                        + "Created events: " + dashboard.totalCreatedEvents + "\n"
                        + "Upcoming events: " + dashboard.upcomingEvents + "\n"
                        + "Draft events: " + dashboard.draftEvents + "\n"
                        + "Total participants: " + dashboard.totalParticipantCount);

        StringBuilder events = new StringBuilder("Upcoming and created events\n\n");
        if (dashboard.events == null || dashboard.events.isEmpty()) {
            events.append("No events yet.");
        } else {
            for (EventDashboardItemResponse event : dashboard.events) {
                events.append(event.title != null ? event.title : "Untitled event")
                        .append("\n")
                        .append(event.eventDate != null ? event.eventDate : "No date")
                        .append(" · ")
                        .append(event.currentParticipantCount);
                if (event.maxParticipants != null) {
                    events.append("/").append(event.maxParticipants);
                }
                events.append(" participants\n\n");
            }
        }
        tvEvents.setText(events.toString());
    }

    private void shareOrganiserCalendar() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.exportOrganiserCalendar(organiserId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/calendar");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "organiser-" + organiserId + ".ics");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, response.body().string());
                        startActivity(Intent.createChooser(shareIntent, "Share public calendar via"));
                    } catch (Exception e) {
                        Toast.makeText(OrganizerDashboardActivity.this,
                                "Could not prepare calendar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OrganizerDashboardActivity.this,
                            "Could not export calendar (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(OrganizerDashboardActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
