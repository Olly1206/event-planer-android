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
    private static final int MAX_RADIUS_METERS = 10_000;

    private final List<VenueResponse> venueList = new ArrayList<>();
    private VenueAdapter adapter;
    private ProgressBar progress;
    private TextView tvNoVenues;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_suggestions);

        progress   = findViewById(R.id.progressVenues);
        tvNoVenues = findViewById(R.id.tvNoVenues);
        recycler   = findViewById(R.id.recyclerVenues);

        adapter = new VenueAdapter(venueList, venue -> {
            // Return the selected venue name back to EventOptionsActivity
            Intent result = new Intent();
            result.putExtra(RESULT_VENUE_NAME, venue.name);
            setResult(RESULT_OK, result);
            finish();
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

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
