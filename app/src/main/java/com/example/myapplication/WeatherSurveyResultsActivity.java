package com.example.myapplication;

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

import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.CreateEventRequest;
import com.example.myapplication.network.dto.EventResponse;
import com.example.myapplication.network.dto.NamedItemResponse;
import com.example.myapplication.network.dto.WeatherData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Step 5 of event creation.
 *
 * Outdoor/mixed events show weather-backed day cards. Indoor events skip the
 * forecast request and show the same custom day picker without weather details.
 * Time selection is also custom to match the app instead of using Android's
 * built-in picker dialogs.
 */
public class WeatherSurveyResultsActivity extends BaseActivity {

    private static final DateTimeFormatter DAY_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private static final int MAX_DAYS_SHOWN = 31;

    private String eventType, eventTitle, locationName, locationCity, locationType;
    private int locationRadiusKm;
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
    private boolean skipWeather;

    private LinearLayout llDayContainer;
    private ProgressBar progressBar;
    private TextView tvError;
    private Button btnSaveEvent;
    private View timePanel;
    private TextView tvSelectedDay;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private Button btnCreateEvent;
    private String selectedDate;
    private int startMinutes = 9 * 60;
    private int endMinutes = 18 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_survey_results);

        readIncomingEventData();

        llDayContainer = findViewById(R.id.llDayContainer);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        timePanel = findViewById(R.id.timePanel);
        tvSelectedDay = findViewById(R.id.tvSelectedDay);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        wireTimeControls();

        String city = (locationCity != null && !locationCity.isEmpty()) ? locationCity
                : (locationName != null && !locationName.isEmpty()) ? locationName
                : "London";

        TextView tvTitle = findViewById(R.id.tvWeatherTitle);
        TextView tvSubtitle = findViewById(R.id.tvWeatherSubtitle);

        if (skipWeather) {
            tvTitle.setText("Select your event day");
            tvSubtitle.setText("Indoor event - no weather forecast needed");
            populatePlainDays();
        } else {
            tvTitle.setText("Choose your event date");
            tvSubtitle.setText("Forecast for " + city + "  ·  " + startDate + " to " + endDate);
            fetchWeatherRange(city);
        }

        btnSaveEvent.setOnClickListener(v -> populatePlainDays());
    }

    private void readIncomingEventData() {
        eventType = getIntent().getStringExtra("EVENT_TYPE");
        eventTitle = getIntent().getStringExtra("EVENT_TITLE");
        locationName = getIntent().getStringExtra("LOCATION_NAME");
        locationCity = getIntent().getStringExtra("LOCATION_CITY");
        locationType = getIntent().getStringExtra("LOCATION_TYPE");
        locationRadiusKm = getIntent().getIntExtra("LOCATION_RADIUS_KM", 0);
        selectedOptions = getIntent().getStringArrayListExtra("SELECTED_OPTIONS");
        startDate = getIntent().getStringExtra("START_DATE");
        endDate = getIntent().getStringExtra("END_DATE");
        skipWeather = getIntent().getBooleanExtra("SKIP_WEATHER", false)
                || "INDOOR".equalsIgnoreCase(locationType);
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

    private void fetchWeatherRange(String city) {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        btnSaveEvent.setVisibility(View.GONE);
        llDayContainer.removeAllViews();

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        api.getWeatherRange(city, startDate, endDate).enqueue(new Callback<List<WeatherData>>() {
            @Override
            public void onResponse(@NonNull Call<List<WeatherData>> call,
                                   @NonNull Response<List<WeatherData>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    populateWeatherDays(response.body());
                } else {
                    showError("No forecast available for this period.\n"
                            + "The forecast service covers a limited range.\n"
                            + "You can still select a day manually.");
                    btnSaveEvent.setVisibility(View.VISIBLE);
                    btnSaveEvent.setText("Select Day Without Forecast");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<WeatherData>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network error: " + t.getMessage());
                btnSaveEvent.setVisibility(View.VISIBLE);
                btnSaveEvent.setText("Select Day Without Forecast");
            }
        });
    }

    private void populateWeatherDays(List<WeatherData> days) {
        llDayContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (WeatherData day : days) {
            View card = inflater.inflate(R.layout.item_weather_day, llDayContainer, false);

            ((TextView) card.findViewById(R.id.tvDayDate)).setText(formatDisplayDate(day.date));
            ((TextView) card.findViewById(R.id.tvDayTemp)).setText(
                    String.format("%.1f °C  ·  Rain: %d%%", day.temperature, day.humidity));
            String desc = day.description != null && !day.description.isEmpty()
                    ? day.description.substring(0, 1).toUpperCase() + day.description.substring(1)
                    : "Forecast available";
            ((TextView) card.findViewById(R.id.tvDayDesc)).setText(desc);

            Button btnSelect = card.findViewById(R.id.btnSelectDay);
            btnSelect.setText("Select Day");
            btnSelect.setOnClickListener(v -> onDaySelected(day.date));

            llDayContainer.addView(card);
        }
    }

    private void populatePlainDays() {
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnSaveEvent.setVisibility(View.GONE);
        llDayContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (String date : buildDateRange()) {
            View card = inflater.inflate(R.layout.item_weather_day, llDayContainer, false);
            ((TextView) card.findViewById(R.id.tvDayDate)).setText(formatDisplayDate(date));
            ((TextView) card.findViewById(R.id.tvDayTemp)).setText("Indoor event");
            ((TextView) card.findViewById(R.id.tvDayDesc)).setText("Weather forecast skipped");
            Button btnSelect = card.findViewById(R.id.btnSelectDay);
            btnSelect.setText("Select Day");
            btnSelect.setOnClickListener(v -> onDaySelected(date));
            llDayContainer.addView(card);
        }
    }

    private List<String> buildDateRange() {
        List<String> dates = new ArrayList<>();
        LocalDate start = parseDateOrToday(startDate);
        LocalDate end = parseDateOrToday(endDate);
        if (end.isBefore(start)) {
            end = start;
        }
        LocalDate cursor = start;
        while (!cursor.isAfter(end) && dates.size() < MAX_DAYS_SHOWN) {
            dates.add(cursor.toString());
            cursor = cursor.plusDays(1);
        }
        return dates;
    }

    private void onDaySelected(String date) {
        selectedDate = date;
        timePanel.setVisibility(View.VISIBLE);
        tvSelectedDay.setText(formatDisplayDate(date));
        updateTimeLabels();
    }

    private void wireTimeControls() {
        findViewById(R.id.btnStartMinus).setOnClickListener(v -> adjustStartTime(-15));
        findViewById(R.id.btnStartPlus).setOnClickListener(v -> adjustStartTime(15));
        findViewById(R.id.btnEndMinus).setOnClickListener(v -> adjustEndTime(-15));
        findViewById(R.id.btnEndPlus).setOnClickListener(v -> adjustEndTime(15));
        findViewById(R.id.btnCancelTimeSelection).setOnClickListener(v -> timePanel.setVisibility(View.GONE));
        btnCreateEvent.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(this, "Please select a day", Toast.LENGTH_SHORT).show();
                return;
            }
            if (endMinutes <= startMinutes) {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }
            String startIso = selectedDate + "T" + formatTime(startMinutes) + ":00";
            String endIso = selectedDate + "T" + formatTime(endMinutes) + ":00";
            lookUpTypesAndCreate(startIso, endIso);
        });
        updateTimeLabels();
    }

    private void adjustStartTime(int deltaMinutes) {
        startMinutes = clampMinutes(startMinutes + deltaMinutes);
        if (endMinutes <= startMinutes) {
            endMinutes = clampMinutes(startMinutes + 60);
        }
        updateTimeLabels();
    }

    private void adjustEndTime(int deltaMinutes) {
        int candidate = clampMinutes(endMinutes + deltaMinutes);
        if (candidate <= startMinutes) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }
        endMinutes = candidate;
        updateTimeLabels();
    }

    private int clampMinutes(int value) {
        return Math.max(0, Math.min(value, 23 * 60 + 45));
    }

    private void updateTimeLabels() {
        tvStartTime.setText(formatTime(startMinutes));
        tvEndTime.setText(formatTime(endMinutes));
    }

    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    private String formatDisplayDate(String isoDate) {
        return parseDateOrToday(isoDate).format(DAY_DISPLAY_FORMAT);
    }

    private LocalDate parseDateOrToday(String isoDate) {
        try {
            return LocalDate.parse(isoDate);
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }

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
        request.title = eventTitle;
        request.eventDate = startIso;
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
        request.eventTypeId = typeId;
        request.optionIds = optionIds;

        api.createEvent(request).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(@NonNull Call<EventResponse> call,
                                   @NonNull Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventResponse createdEvent = response.body();
                    Toast.makeText(WeatherSurveyResultsActivity.this,
                            "\"" + eventTitle + "\" created!", Toast.LENGTH_SHORT).show();

                    if (selectedOptions != null && !selectedOptions.isEmpty()) {
                        Intent intent = new Intent(WeatherSurveyResultsActivity.this, VendorSuggestionsActivity.class);
                        String vendorCity = (locationCity != null && !locationCity.isEmpty()) ? locationCity : locationName;
                        intent.putExtra(VendorSuggestionsActivity.EXTRA_CITY, vendorCity);
                        intent.putExtra(VendorSuggestionsActivity.EXTRA_EVENT_ID, createdEvent.id);
                        intent.putExtra(VendorSuggestionsActivity.EXTRA_ALLOW_ADD, true);
                        intent.putExtra(VendorSuggestionsActivity.EXTRA_RADIUS,
                                locationRadiusKm > 0 ? locationRadiusKm * 1000 : 5000);
                        intent.putStringArrayListExtra(VendorSuggestionsActivity.EXTRA_OPTIONS, selectedOptions);
                        startActivity(intent);
                    } else {
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

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
