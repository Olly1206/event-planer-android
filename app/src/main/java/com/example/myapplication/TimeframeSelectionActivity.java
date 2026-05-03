package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Step 4 of event creation.
 *
 * The user selects a date range with custom in-app controls. Outdoor/mixed
 * events continue to the weather-backed day selection. Indoor events skip the
 * forecast and go straight to the same custom day/time selection view.
 */
public class TimeframeSelectionActivity extends BaseActivity {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");

    private LocalDate startDate;
    private LocalDate endDate;

    private String eventType, eventTitle, locationName, locationCity, locationType;
    private int locationRadiusKm;
    private ArrayList<String> selectedOptions;
    private Long venueOsmId;
    private String venueName;
    private String venueAddress;
    private Double venueLat;
    private Double venueLon;
    private String venueCategory;
    private String venueWebsite;
    private String venuePhone;
    private String venueHours;

    private TextView tvStartDateValue;
    private TextView tvEndDateValue;
    private TextView tvSummary;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeframe_selection);

        readIncomingEventData();

        tvStartDateValue = findViewById(R.id.tvStartDateValue);
        tvEndDateValue = findViewById(R.id.tvEndDateValue);
        tvSummary = findViewById(R.id.textViewSelectedDates);
        btnNext = findViewById(R.id.btnCreateSurvey);

        startDate = LocalDate.now();
        endDate = startDate.plusDays(7);

        findViewById(R.id.btnStartPrevious).setOnClickListener(v -> adjustStartDate(-1));
        findViewById(R.id.btnStartNext).setOnClickListener(v -> adjustStartDate(1));
        findViewById(R.id.btnEndPrevious).setOnClickListener(v -> adjustEndDate(-1));
        findViewById(R.id.btnEndNext).setOnClickListener(v -> adjustEndDate(1));

        boolean indoor = "INDOOR".equalsIgnoreCase(locationType);
        btnNext.setText(indoor ? "Select Day" : "Show Weather Forecast");
        updateSummary();

        btnNext.setOnClickListener(v -> {
            if (endDate.isBefore(startDate)) {
                Toast.makeText(this, "Latest date must be after earliest date", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, WeatherSurveyResultsActivity.class);
            putEventData(intent);
            intent.putExtra("START_DATE", startDate.toString());
            intent.putExtra("END_DATE", endDate.toString());
            intent.putExtra("SKIP_WEATHER", indoor);
            startActivity(intent);
        });
    }

    private void adjustStartDate(int days) {
        startDate = startDate.plusDays(days);
        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }
        updateSummary();
    }

    private void adjustEndDate(int days) {
        LocalDate candidate = endDate.plusDays(days);
        if (candidate.isBefore(startDate)) {
            Toast.makeText(this, "Latest date cannot be before earliest date", Toast.LENGTH_SHORT).show();
            return;
        }
        endDate = candidate;
        updateSummary();
    }

    private void updateSummary() {
        tvStartDateValue.setText(startDate.format(DISPLAY_FORMAT));
        tvEndDateValue.setText(endDate.format(DISPLAY_FORMAT));
        tvSummary.setText("From: " + startDate + "\nTo:   " + endDate);
    }

    private void readIncomingEventData() {
        eventType        = getIntent().getStringExtra("EVENT_TYPE");
        eventTitle       = getIntent().getStringExtra("EVENT_TITLE");
        locationName     = getIntent().getStringExtra("LOCATION_NAME");
        locationCity     = getIntent().getStringExtra("LOCATION_CITY");
        locationType     = getIntent().getStringExtra("LOCATION_TYPE");
        locationRadiusKm = getIntent().getIntExtra("LOCATION_RADIUS_KM", 0);
        selectedOptions  = getIntent().getStringArrayListExtra("SELECTED_OPTIONS");
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
    }

    private void putEventData(Intent intent) {
        intent.putExtra("EVENT_TYPE", eventType);
        intent.putExtra("EVENT_TITLE", eventTitle);
        intent.putExtra("LOCATION_NAME", locationName);
        intent.putExtra("LOCATION_CITY", locationCity);
        intent.putExtra("LOCATION_TYPE", locationType);
        intent.putExtra("LOCATION_RADIUS_KM", locationRadiusKm);
        intent.putStringArrayListExtra("SELECTED_OPTIONS", selectedOptions);
        if (venueOsmId != null) intent.putExtra("VENUE_OSM_ID", venueOsmId);
        intent.putExtra("VENUE_NAME", venueName);
        intent.putExtra("VENUE_ADDRESS", venueAddress);
        if (venueLat != null) intent.putExtra("VENUE_LAT", venueLat);
        if (venueLon != null) intent.putExtra("VENUE_LON", venueLon);
        intent.putExtra("VENUE_CATEGORY", venueCategory);
        intent.putExtra("VENUE_WEBSITE", venueWebsite);
        intent.putExtra("VENUE_PHONE", venuePhone);
        intent.putExtra("VENUE_HOURS", venueHours);
    }
}
