package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;

import com.example.myapplication.network.NominatimService;
import com.example.myapplication.network.dto.NominatimResult;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventOptionsActivity extends BaseActivity {

    private static final String[] RADIUS_LABELS = {
            "No preference", "5 km", "10 km"
    };
    private static final int[] RADIUS_VALUES = { 0, 5, 10 };

    /** Delay (ms) after the user stops typing before firing a Nominatim request. */
    private static final long SEARCH_DEBOUNCE_MS = 400;

    private String eventType;
    /** Short city name used for weather geocoding (first part of display name). */
    private String selectedCityName = "";
    /** Full display name shown in the field, e.g. "Paris, France" */
    private String selectedCityDisplay = "";

    private TextInputEditText etLocation;
    private ListPopupWindow listPopupWindow;
    private List<NominatimResult> currentSuggestions = new ArrayList<>();
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    /** True while setText() is being called after the user picks a suggestion — prevents re-fetching. */
    private boolean suppressNextSearch = false;

    private NominatimService nominatimService;
    private Button btnBrowseVenues;
    private ActivityResultLauncher<Intent> venueLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_options);

        // Initialize ActivityResultLauncher
        venueLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result from VenueSuggestionsActivity if needed
                }
        );

        // Build a dedicated Retrofit instance for Nominatim (separate host from backend)
        OkHttpClient nominatimClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                // Nominatim policy requires a non-empty User-Agent
                                .header("User-Agent", "EventPlannerApp/1.0")
                                .build()))
                .build();
        nominatimService = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(nominatimClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NominatimService.class);

        eventType = getIntent().getStringExtra("EVENT_TYPE");

        TextInputEditText etTitle = findViewById(R.id.etEventTitle);
        etLocation = findViewById(R.id.etLocationName);
        Spinner spinnerRadius = findViewById(R.id.spinnerRadius);
        btnBrowseVenues = findViewById(R.id.btnBrowseVenues);

        // Populate radius spinner
        ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, RADIUS_LABELS);
        radiusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRadius.setAdapter(radiusAdapter);
        spinnerRadius.setSelection(2); // default: 10 km

        // Dropdown that shows city suggestions below the text field
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAnchorView(etLocation);
        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            NominatimResult result = currentSuggestions.get(position);
            selectedCityDisplay = result.toString();
            // Extract the first comma-separated token as the short city name
            selectedCityName = result.displayName != null
                    ? result.displayName.split(",")[0].trim()
                    : selectedCityDisplay;
            suppressNextSearch = true;          // prevent afterTextChanged from re-fetching
            etLocation.setText(selectedCityDisplay);
            etLocation.setSelection(selectedCityDisplay.length());
            listPopupWindow.dismiss();
            btnBrowseVenues.setEnabled(true);
        });

        etLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // If the user is editing after a selection, clear the saved city
                String typed = s.toString().trim();
                if (!typed.equals(selectedCityDisplay)) {
                    selectedCityName    = "";
                    selectedCityDisplay = "";
                    btnBrowseVenues.setEnabled(false);
                }
                if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
                // Text was set programmatically after picking a suggestion — skip the search
                if (suppressNextSearch) {
                    suppressNextSearch = false;
                    return;
                }
                if (typed.length() < 2) {
                    listPopupWindow.dismiss();
                    return;
                }
                debounceRunnable = () -> fetchCitySuggestions(typed);
                debounceHandler.postDelayed(debounceRunnable, SEARCH_DEBOUNCE_MS);
            }
        });

        btnBrowseVenues.setOnClickListener(v -> {
            String locType = "BOTH";
            // Peek at current checkbox state for the query
            CheckBox tmpOutside = findViewById(R.id.cbOutside);
            CheckBox tmpInside  = findViewById(R.id.cbInside);
            if (tmpOutside.isChecked()) locType = "OUTDOOR";
            else if (tmpInside.isChecked()) locType = "INDOOR";

            int radius = RADIUS_VALUES[spinnerRadius.getSelectedItemPosition()];
            int radiusMeters = radius > 0 ? radius * 1000 : 5000;

            Intent intent = new Intent(this, VenueSuggestionsActivity.class);
            intent.putExtra(VenueSuggestionsActivity.EXTRA_CITY,          selectedCityName);
            intent.putExtra(VenueSuggestionsActivity.EXTRA_RADIUS,        radiusMeters);
            intent.putExtra(VenueSuggestionsActivity.EXTRA_LOCATION_TYPE, locType);
            intent.putExtra(VenueSuggestionsActivity.EXTRA_EVENT_TYPE,    eventType);
            venueLauncher.launch(intent);
        });

        Button btnFinalize = findViewById(R.id.btnFinalize);

        CheckBox cbOutside       = findViewById(R.id.cbOutside);
        CheckBox cbInside        = findViewById(R.id.cbInside);
        CheckBox cbInternal      = findViewById(R.id.cbInternal);
        CheckBox cbExternal      = findViewById(R.id.cbExternal);
        CheckBox cbCatering      = findViewById(R.id.cbCatering);
        CheckBox cbMusic         = findViewById(R.id.cbMusic);
        CheckBox cbGuestSpeakers = findViewById(R.id.cbGuestSpeakers);
        CheckBox cbSecurity      = findViewById(R.id.cbSecurity);
        CheckBox cbEquipment     = findViewById(R.id.cbEquipment);

        cbOutside.setOnCheckedChangeListener((b, checked) -> {
            if (checked) cbInside.setChecked(false);
        });
        cbInside.setOnCheckedChangeListener((b, checked) -> {
            if (checked) cbOutside.setChecked(false);
        });
        cbInternal.setOnCheckedChangeListener((b, checked) -> {
            if (checked) cbExternal.setChecked(false);
        });
        cbExternal.setOnCheckedChangeListener((b, checked) -> {
            if (checked) cbInternal.setChecked(false);
        });

        btnFinalize.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter an event title", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCityName.isEmpty()) {
                Toast.makeText(this, "Please select a city from the suggestions", Toast.LENGTH_SHORT).show();
                return;
            }

            String locationType;
            if (cbOutside.isChecked()) {
                locationType = "OUTDOOR";
            } else if (cbInside.isChecked()) {
                locationType = "INDOOR";
            } else {
                locationType = "BOTH";
            }

            int radiusKm = RADIUS_VALUES[spinnerRadius.getSelectedItemPosition()];
            String locationName = radiusKm > 0
                    ? selectedCityDisplay + " (" + radiusKm + " km radius)"
                    : selectedCityDisplay;

            ArrayList<String> selectedOptions = new ArrayList<>();
            if (cbCatering.isChecked())      selectedOptions.add("Catering");
            if (cbMusic.isChecked())         selectedOptions.add("Live Music");
            if (cbGuestSpeakers.isChecked()) selectedOptions.add("Photography");
            if (cbSecurity.isChecked())      selectedOptions.add("Security Staff");
            if (cbEquipment.isChecked())     selectedOptions.add("AV Equipment");

            Intent intent = new Intent(EventOptionsActivity.this, TimeframeSelectionActivity.class);
            intent.putExtra("EVENT_TYPE",        eventType);
            intent.putExtra("EVENT_TITLE",        title);
            intent.putExtra("LOCATION_NAME",      locationName);     // stored in DB (with radius)
            intent.putExtra("LOCATION_CITY",      selectedCityName);  // used for weather API
            intent.putExtra("LOCATION_TYPE",      locationType);
            intent.putExtra("LOCATION_RADIUS_KM", radiusKm);
            intent.putStringArrayListExtra("SELECTED_OPTIONS", selectedOptions);
            startActivity(intent);
        });
    }

    private void fetchCitySuggestions(String query) {
        nominatimService.searchCities(query, "city", "json", 0, 5)
                .enqueue(new Callback<List<NominatimResult>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<NominatimResult>> call,
                                           @NonNull Response<List<NominatimResult>> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().isEmpty()) {
                            listPopupWindow.dismiss();
                            return;
                        }
                        currentSuggestions = response.body();
                        List<String> labels = new ArrayList<>();
                        for (NominatimResult r : currentSuggestions) labels.add(r.toString());

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                EventOptionsActivity.this,
                                android.R.layout.simple_list_item_1, labels);
                        listPopupWindow.setAdapter(adapter);
                        listPopupWindow.show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<NominatimResult>> call,
                                          @NonNull Throwable t) {
                        listPopupWindow.dismiss();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
    }
}
