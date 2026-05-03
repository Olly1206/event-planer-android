package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.CreateEventRequest;
import com.example.myapplication.network.dto.EventResponse;
import com.example.myapplication.network.dto.NamedItemResponse;
import com.example.myapplication.network.dto.WeatherData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Step 4 of event creation.
 *
 * Shows the weather forecast for each day in the requested date range so the
 * user can choose the best day for their event. Tapping "Select" on a day card
 * opens two time pickers (start time, end time), then submits the full event
 * creation request to the backend.
 */
public class WeatherSurveyResultsActivity extends BaseActivity {

    private String eventType, eventTitle, locationName, locationCity, locationType;
    private ArrayList<String> selectedOptions;
    private String startDate, endDate;
    private Long venueOsmId;
    private String venueName;
    private String venueAddress;
    private Double venueLat;
    private Double venueLon;
    private String venueCategory;
    private String venueWebsite;
    private String venuePhone;
    private String venueHours;

    private LinearLayout llDayContainer;
    private ProgressBar progressBar;
    private TextView tvError;
    private Button btnSaveEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_survey_results);

        // Receive all event metadata forwarded from TimeframeSelectionActivity
        eventType       = getIntent().getStringExtra("EVENT_TYPE");
        eventTitle      = getIntent().getStringExtra("EVENT_TITLE");
        locationName    = getIntent().getStringExtra("LOCATION_NAME");
        locationCity    = getIntent().getStringExtra("LOCATION_CITY");
        locationType    = getIntent().getStringExtra("LOCATION_TYPE");
        selectedOptions = getIntent().getStringArrayListExtra("SELECTED_OPTIONS");
        startDate       = getIntent().getStringExtra("START_DATE");
        endDate         = getIntent().getStringExtra("END_DATE");
        if (getIntent().hasExtra("VENUE_OSM_ID")) venueOsmId = getIntent().getLongExtra("VENUE_OSM_ID", 0);
        venueName = getIntent().getStringExtra("VENUE_NAME");
        venueAddress = getIntent().getStringExtra("VENUE_ADDRESS");
        if (getIntent().hasExtra("VENUE_LAT")) venueLat = getIntent().getDoubleExtra("VENUE_LAT", 0);
        if (getIntent().hasExtra("VENUE_LON")) venueLon = getIntent().getDoubleExtra("VENUE_LON", 0);
        venueCategory = getIntent().getStringExtra("VENUE_CATEGORY");
        venueWebsite = getIntent().getStringExtra("VENUE_WEBSITE");
        venuePhone = getIntent().getStringExtra("VENUE_PHONE");
        venueHours = getIntent().getStringExtra("VENUE_HOURS");
        if (selectedOptions == null) selectedOptions = new ArrayList<>();

        llDayContainer = findViewById(R.id.llDayContainer);
        progressBar    = findViewById(R.id.progressBar);
        tvError        = findViewById(R.id.tvError);

        // Use the raw city name for geocoding; fall back to the full locationName
        String city = (locationCity != null && !locationCity.isEmpty()) ? locationCity
                    : (locationName != null && !locationName.isEmpty()) ? locationName
                    : "London";

        TextView tvSubtitle = findViewById(R.id.tvWeatherSubtitle);
        tvSubtitle.setText("Forecast for " + city + "  ·  " + startDate + " → " + endDate);

        // Fallback save button — wired up here, but kept hidden until weather fails
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnSaveEvent.setOnClickListener(v -> pickDateAndSave());

        fetchWeatherRange(city);
    }

    // ── Save without weather ───────────────────────────────────────────────────

    /** Opens a date picker (defaulting to startDate) then delegates to the existing time-pick flow. */
    private void pickDateAndSave() {
        int y = 2026, m = 0, d = 1;
        try {
            String[] parts = (startDate != null ? startDate : "2026-01-01").split("-");
            y = Integer.parseInt(parts[0]);
            m = Integer.parseInt(parts[1]) - 1;
            d = Integer.parseInt(parts[2]);
        } catch (Exception ignored) {}

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String pickedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
            onDaySelected(pickedDate);
        }, y, m, d).show();
    }

    // ── Weather fetch ──────────────────────────────────────────────────────────

    private void fetchWeatherRange(String city) {
        progressBar.setVisibility(View.VISIBLE);
        llDayContainer.removeAllViews();

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        api.getWeatherRange(city, startDate, endDate).enqueue(new Callback<List<WeatherData>>() {
            @Override
            public void onResponse(@NonNull Call<List<WeatherData>> call,
                                   @NonNull Response<List<WeatherData>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    populateDays(response.body());
                } else {
                    showError("No forecast available for this period.\n"
                            + "The forecast service covers up to 16 days from today.\n"
                            + "Please check your date range, or use the button below to save anyway.");
                    btnSaveEvent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<WeatherData>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network error: " + t.getMessage());
                btnSaveEvent.setVisibility(View.VISIBLE);
            }
        });
    }

    // ── Build per-day cards ────────────────────────────────────────────────────

    private void populateDays(List<WeatherData> days) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (WeatherData day : days) {
            View card = inflater.inflate(R.layout.item_weather_day, llDayContainer, false);

            ((TextView) card.findViewById(R.id.tvDayDate)).setText(day.date);
            ((TextView) card.findViewById(R.id.tvDayTemp)).setText(
                    String.format("%.1f °C  ·  Rain: %d%%", day.temperature, day.humidity));
            String desc = day.description != null ? day.description : "";
            ((TextView) card.findViewById(R.id.tvDayDesc)).setText(
                    desc.substring(0, 1).toUpperCase() + desc.substring(1));

            Button btnSelect = card.findViewById(R.id.btnSelectDay);
            btnSelect.setOnClickListener(v -> onDaySelected(day.date));

            llDayContainer.addView(card);
        }
    }

    // ── Date selected → pick times → create event ─────────────────────────────

    private void onDaySelected(String date) {
        // Pick start time
        new TimePickerDialog(this, (view, startH, startMin) ->
            // Then pick end time
            new TimePickerDialog(this, (view2, endH, endMin) ->
                confirmAndCreate(date, startH, startMin, endH, endMin),
            18, 0, true).show(),
        9, 0, true).show();
    }

    private void confirmAndCreate(String date, int startH, int startMin, int endH, int endMin) {
        String startIso = String.format("%sT%02d:%02d:00", date, startH, startMin);
        String endIso   = String.format("%sT%02d:%02d:00", date, endH, endMin);

        new AlertDialog.Builder(this)
                .setTitle("Create event?")
                .setMessage(eventTitle + "\n" + date
                        + "  " + String.format("%02d:%02d", startH, startMin)
                        + " – " + String.format("%02d:%02d", endH, endMin))
                .setPositiveButton("Create", (d, w) -> lookUpTypesAndCreate(startIso, endIso))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Backend submission chain ───────────────────────────────────────────────

    private void lookUpTypesAndCreate(String startIso, String endIso) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        api.getEventTypes().enqueue(new Callback<List<NamedItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<NamedItemResponse>> call,
                                   @NonNull Response<List<NamedItemResponse>> response) {
                Long typeId = null;
                if (response.isSuccessful() && response.body() != null) {
                    for (NamedItemResponse item : response.body()) {
                        if (item.name != null && item.name.equalsIgnoreCase(eventType)) {
                            typeId = item.id;
                            break;
                        }
                    }
                }
                lookUpOptionsAndCreate(api, typeId, startIso, endIso);
            }

            @Override
            public void onFailure(@NonNull Call<List<NamedItemResponse>> call, @NonNull Throwable t) {
                lookUpOptionsAndCreate(api, null, startIso, endIso);
            }
        });
    }

    private void lookUpOptionsAndCreate(ApiService api, Long typeId, String startIso, String endIso) {
        api.getEventOptions().enqueue(new Callback<List<NamedItemResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<NamedItemResponse>> call,
                                   @NonNull Response<List<NamedItemResponse>> response) {
                Set<Long> optionIds = new HashSet<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (NamedItemResponse item : response.body()) {
                        if (item.name != null && selectedOptions.contains(item.name)) {
                            optionIds.add(item.id);
                        }
                    }
                }
                submitEvent(api, typeId, optionIds, startIso, endIso);
            }

            @Override
            public void onFailure(@NonNull Call<List<NamedItemResponse>> call, @NonNull Throwable t) {
                submitEvent(api, typeId, new HashSet<>(), startIso, endIso);
            }
        });
    }

    private void submitEvent(ApiService api, Long typeId, Set<Long> optionIds,
                              String startIso, String endIso) {
        CreateEventRequest request = new CreateEventRequest();
        request.title        = eventTitle;
        request.eventDate    = startIso;
        request.eventEndDate = endIso;
        request.locationName = locationName;
        request.locationType = locationType;
        request.venueOsmId = venueOsmId;
        request.venueName = venueName;
        request.venueAddress = venueAddress;
        request.venueLat = venueLat;
        request.venueLon = venueLon;
        request.venueCategory = venueCategory;
        request.venueWebsite = venueWebsite;
        request.venuePhone = venuePhone;
        request.venueOpeningHours = venueHours;
        request.eventTypeId  = typeId;
        request.optionIds    = optionIds;

        api.createEvent(request).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(@NonNull Call<EventResponse> call,
                                   @NonNull Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventResponse createdEvent = response.body();
                    Toast.makeText(WeatherSurveyResultsActivity.this,
                            "\"" + eventTitle + "\" created!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to vendor selection if event has options
                    if (selectedOptions != null && !selectedOptions.isEmpty()) {
                        Intent intent = new Intent(WeatherSurveyResultsActivity.this, VendorSuggestionsActivity.class);
                        String vendorCity = (locationCity != null && !locationCity.isEmpty()) ? locationCity : locationName;
                        intent.putExtra(VendorSuggestionsActivity.EXTRA_CITY, vendorCity);
                        intent.putExtra(VendorSuggestionsActivity.EXTRA_EVENT_ID, createdEvent.id);
                        intent.putExtra(VendorSuggestionsActivity.EXTRA_ALLOW_ADD, true);
                        intent.putStringArrayListExtra(VendorSuggestionsActivity.EXTRA_OPTIONS, selectedOptions);
                        startActivity(intent);
                    } else {
                        // No options selected, go back to event list
                        Intent intent = new Intent(WeatherSurveyResultsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } else {
                    String errorDetail = response.message();
                    if (response.errorBody() != null) {
                        try { errorDetail = response.errorBody().string(); } catch (IOException ignored) {}
                    }
                    Toast.makeText(WeatherSurveyResultsActivity.this,
                            "Failed (" + response.code() + "): " + errorDetail,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventResponse> call, @NonNull Throwable t) {
                Toast.makeText(WeatherSurveyResultsActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
