package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Step 3 of event creation.
 *
 * The user picks a date RANGE (earliest and latest acceptable date for their event).
 * No times are needed here — the specific date is chosen on the next screen based
 * on the weather forecast. Tapping "Show Weather Forecast" navigates to
 * WeatherSurveyResultsActivity, passing the range and all previously collected
 * event metadata.
 */
public class TimeframeSelectionActivity extends BaseActivity {

    private int startYear, startMonth, startDay;
    private int endYear,   endMonth,   endDay;

    private boolean startDatePicked = false;
    private boolean endDatePicked   = false;

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

    private Button btnStartDate, btnEndDate, btnNext;
    private TextView tvSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeframe_selection);

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

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate   = findViewById(R.id.btnEndDate);
        btnNext      = findViewById(R.id.btnCreateSurvey);
        tvSummary    = findViewById(R.id.textViewSelectedDates);

        btnNext.setText("Show Weather Forecast");

        // Seed with today's date
        Calendar now = Calendar.getInstance();
        startYear  = endYear  = now.get(Calendar.YEAR);
        startMonth = endMonth = now.get(Calendar.MONTH);
        startDay   = endDay   = now.get(Calendar.DAY_OF_MONTH);

        btnStartDate.setOnClickListener(v -> openDatePicker(true));
        btnEndDate.setOnClickListener(v   -> openDatePicker(false));

        btnNext.setOnClickListener(v -> {
            if (!startDatePicked) {
                Toast.makeText(this, "Please pick the earliest possible date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!endDatePicked) {
                Toast.makeText(this, "Please pick the latest possible date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build ISO date strings (yyyy-MM-dd) for the backend
            String startIso = String.format("%d-%02d-%02d", startYear, startMonth + 1, startDay);
            String endIso   = String.format("%d-%02d-%02d", endYear,   endMonth   + 1, endDay);

            Intent intent = new Intent(this, WeatherSurveyResultsActivity.class);
            intent.putExtra("EVENT_TYPE",         eventType);
            intent.putExtra("EVENT_TITLE",         eventTitle);
            intent.putExtra("LOCATION_NAME",       locationName);
            intent.putExtra("LOCATION_CITY",       locationCity);
            intent.putExtra("LOCATION_TYPE",       locationType);
            intent.putExtra("LOCATION_RADIUS_KM",  locationRadiusKm);
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
            intent.putExtra("START_DATE",           startIso);
            intent.putExtra("END_DATE",             endIso);
            startActivity(intent);
        });
    }

    private void openDatePicker(boolean isStart) {
        int y = isStart ? startYear  : endYear;
        int m = isStart ? startMonth : endMonth;
        int d = isStart ? startDay   : endDay;

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            if (isStart) {
                startYear = year; startMonth = month; startDay = dayOfMonth;
                startDatePicked = true;
                btnStartDate.setText(formatDate(year, month, dayOfMonth));
            } else {
                endYear = year; endMonth = month; endDay = dayOfMonth;
                endDatePicked = true;
                btnEndDate.setText(formatDate(year, month, dayOfMonth));
            }
            updateSummary();
        }, y, m, d).show();
    }

    private void updateSummary() {
        String start = startDatePicked ? formatDate(startYear, startMonth, startDay) : "not set";
        String end   = endDatePicked   ? formatDate(endYear,   endMonth,   endDay)   : "not set";
        tvSummary.setText("From: " + start + "\nTo:   " + end);
    }

    private String formatDate(int year, int month, int dayOfMonth) {
        return String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
    }
}