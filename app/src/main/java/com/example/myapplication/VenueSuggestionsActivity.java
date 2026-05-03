package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.VenueAdapter;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.VenueResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays OpenStreetMap venue suggestions for the city + location type chosen
 * in EventOptionsActivity.
 *
 * Tapping a venue returns it as an ActivityResult — EventOptionsActivity fills
 * the location field with the selected venue name.
 */
public class VenueSuggestionsActivity extends BaseActivity {

    public static final String EXTRA_CITY          = "venue_city";
    public static final String EXTRA_RADIUS        = "venue_radius";
    public static final String EXTRA_LOCATION_TYPE = "venue_location_type";
    public static final String EXTRA_EVENT_TYPE    = "venue_event_type";
    public static final String RESULT_VENUE_NAME   = "selected_venue_name";
    public static final String RESULT_VENUE_OSM_ID = "selected_venue_osm_id";
    public static final String RESULT_VENUE_ADDRESS = "selected_venue_address";
    public static final String RESULT_VENUE_LAT = "selected_venue_lat";
    public static final String RESULT_VENUE_LON = "selected_venue_lon";
    public static final String RESULT_VENUE_CATEGORY = "selected_venue_category";
    public static final String RESULT_VENUE_WEBSITE = "selected_venue_website";
    public static final String RESULT_VENUE_PHONE = "selected_venue_phone";
    public static final String RESULT_VENUE_HOURS = "selected_venue_hours";
    private static final int MAX_RADIUS_METERS = 10_000;

    private final List<VenueResponse> venueList = new ArrayList<>();
    private VenueAdapter adapter;
    private ProgressBar progress;
    private TextView tvNoVenues;
    private RecyclerView recycler;
    private MaterialButton btnContinueWithoutVenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_suggestions);

        progress   = findViewById(R.id.progressVenues);
        tvNoVenues = findViewById(R.id.tvNoVenues);
        recycler   = findViewById(R.id.recyclerVenues);
        btnContinueWithoutVenue = findViewById(R.id.btnContinueWithoutVenue);

        adapter = new VenueAdapter(venueList, this::continueWithVenue);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        btnContinueWithoutVenue.setOnClickListener(v -> continueWithVenue(null));

        String city         = getIntent().getStringExtra(EXTRA_CITY);
        int    radius       = getIntent().getIntExtra(EXTRA_RADIUS, 5000);
        int    safeRadius   = Math.min(radius, MAX_RADIUS_METERS);
        String locationType = getIntent().getStringExtra(EXTRA_LOCATION_TYPE);
        String eventType    = getIntent().getStringExtra(EXTRA_EVENT_TYPE);

        if (city == null || city.isEmpty()) {
            Toast.makeText(this, "No city provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadVenues(city, safeRadius, locationType, eventType);
    }

    private void continueWithVenue(VenueResponse venue) {
        Intent intent = new Intent(this, TimeframeSelectionActivity.class);
        intent.putExtra("EVENT_TYPE", getIntent().getStringExtra("EVENT_TYPE"));
        intent.putExtra("EVENT_TITLE", getIntent().getStringExtra("EVENT_TITLE"));
        intent.putExtra("LOCATION_CITY", getIntent().getStringExtra("LOCATION_CITY"));
        intent.putExtra("LOCATION_TYPE", getIntent().getStringExtra("LOCATION_TYPE"));
        intent.putExtra("LOCATION_RADIUS_KM", getIntent().getIntExtra("LOCATION_RADIUS_KM", 0));
        intent.putStringArrayListExtra(
                "SELECTED_OPTIONS",
                getIntent().getStringArrayListExtra("SELECTED_OPTIONS"));

        String locationName = getIntent().getStringExtra("LOCATION_NAME");
        if (venue != null && venue.name != null && !venue.name.isEmpty()) {
            locationName = venue.name;
            if (venue.osmId != null) intent.putExtra("VENUE_OSM_ID", venue.osmId);
            intent.putExtra("VENUE_NAME", venue.name);
            intent.putExtra("VENUE_ADDRESS", venue.address);
            if (venue.lat != null) intent.putExtra("VENUE_LAT", venue.lat);
            if (venue.lon != null) intent.putExtra("VENUE_LON", venue.lon);
            intent.putExtra("VENUE_CATEGORY", venue.category);
            intent.putExtra("VENUE_WEBSITE", venue.website);
            intent.putExtra("VENUE_PHONE", venue.phone);
            intent.putExtra("VENUE_HOURS", venue.openingHours);
        }
        intent.putExtra("LOCATION_NAME", locationName);
        startActivity(intent);
    }

    private void loadVenues(String city, int radius,
                             String locationType, String eventType) {
        progress.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
        tvNoVenues.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getVenues(city, radius, locationType, eventType)
                .enqueue(new Callback<List<VenueResponse>>() {
                    @Override
                    public void onResponse(Call<List<VenueResponse>> call,
                                           Response<List<VenueResponse>> response) {
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            venueList.clear();
                            venueList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            recycler.setVisibility(View.VISIBLE);
                        } else {
                            tvNoVenues.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<VenueResponse>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(VenueSuggestionsActivity.this,
                                "Cannot reach server: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        tvNoVenues.setVisibility(View.VISIBLE);
                    }
                });
    }
}
