package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.EventResponse;
import com.example.myapplication.network.dto.UpdateEventRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Allows the organiser or an admin to edit event fields via PATCH /api/events/{id}.
 * Only non-empty fields are sent, so the backend applies partial updates.
 */
public class EditEventActivity extends BaseActivity {

    private static final String[] STATUSES    = {"PLANNED", "ONGOING", "COMPLETED", "CANCELLED"};
    private static final String[] VISIBILITIES = {"PUBLIC", "PRIVATE"};

    private TextInputEditText etTitle, etDescription, etLocation, etMaxParticipants;
    private Spinner spinnerStatus, spinnerVisibility;
    private long eventId;
    private ArrayList<String> vendorOptions;
    private String vendorCity;
    private long[] selectedVendorIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        etTitle           = findViewById(R.id.etEditTitle);
        etDescription     = findViewById(R.id.etEditDescription);
        etLocation        = findViewById(R.id.etEditLocation);
        etMaxParticipants = findViewById(R.id.etEditMaxParticipants);
        spinnerStatus     = findViewById(R.id.spinnerStatus);
        spinnerVisibility = findViewById(R.id.spinnerVisibility);
        MaterialButton btnManageVendors = findViewById(R.id.btnManageVendors);
        MaterialButton btnSave = findViewById(R.id.btnSaveEvent);

        spinnerStatus.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, STATUSES));
        spinnerVisibility.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, VISIBILITIES));

        eventId = getIntent().getLongExtra("eventId", -1);
        if (eventId == -1) { finish(); return; }
        vendorOptions = getIntent().getStringArrayListExtra(VendorSuggestionsActivity.EXTRA_OPTIONS);
        vendorCity = getIntent().getStringExtra(VendorSuggestionsActivity.EXTRA_CITY);
        selectedVendorIds = getIntent().getLongArrayExtra(VendorSuggestionsActivity.EXTRA_SELECTED_VENDOR_IDS);

        // Pre-populate from intent extras
        String title = getIntent().getStringExtra("title");
        String desc  = getIntent().getStringExtra("description");
        String loc   = getIntent().getStringExtra("locationName");
        String venueName = getIntent().getStringExtra("venueName");
        String status = getIntent().getStringExtra("status");
        String visibility = getIntent().getStringExtra("visibility");
        int maxP = getIntent().getIntExtra("maxParticipants", 0);

        if (title != null) etTitle.setText(title);
        if (desc  != null) etDescription.setText(desc);
        if (venueName != null) {
            etLocation.setText(venueName);
        } else if (loc != null) {
            etLocation.setText(loc);
        }
        if (maxP > 0) etMaxParticipants.setText(String.valueOf(maxP));

        if (status != null) {
            for (int i = 0; i < STATUSES.length; i++) {
                if (STATUSES[i].equalsIgnoreCase(status)) {
                    spinnerStatus.setSelection(i);
                    break;
                }
            }
        }
        if (visibility != null) {
            for (int i = 0; i < VISIBILITIES.length; i++) {
                if (VISIBILITIES[i].equalsIgnoreCase(visibility)) {
                    spinnerVisibility.setSelection(i);
                    break;
                }
            }
        }

        btnManageVendors.setOnClickListener(v -> openVendorSuggestions());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void openVendorSuggestions() {
        if (vendorCity == null || vendorCity.trim().isEmpty()) {
            String location = getText(etLocation);
            if (location != null) {
                vendorCity = location.contains("(")
                        ? location.substring(0, location.indexOf("(")).trim()
                        : location.trim();
            }
        }

        if (vendorCity == null || vendorCity.isEmpty()) {
            Toast.makeText(this, "No city available for vendor search", Toast.LENGTH_SHORT).show();
            return;
        }
        if (vendorOptions == null || vendorOptions.isEmpty()) {
            Toast.makeText(this, "This event has no selected options to match vendors", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, VendorSuggestionsActivity.class);
        intent.putExtra(VendorSuggestionsActivity.EXTRA_EVENT_ID, eventId);
        intent.putExtra(VendorSuggestionsActivity.EXTRA_ALLOW_ADD, true);
        intent.putExtra(VendorSuggestionsActivity.EXTRA_CITY, vendorCity);
        intent.putStringArrayListExtra(VendorSuggestionsActivity.EXTRA_OPTIONS, vendorOptions);
        if (selectedVendorIds != null) {
            intent.putExtra(VendorSuggestionsActivity.EXTRA_SELECTED_VENDOR_IDS, selectedVendorIds);
        }
        startActivity(intent);
    }

    private void saveChanges() {
        UpdateEventRequest req = new UpdateEventRequest();

        String title = getText(etTitle);
        String desc  = getText(etDescription);
        String loc   = getText(etLocation);
        String maxP  = getText(etMaxParticipants);

        if (title != null) req.title = title;
        if (desc  != null) req.description = desc;
        if (loc   != null) req.locationName = loc;
        if (maxP  != null) req.maxParticipants = Integer.parseInt(maxP);
        req.status = (String) spinnerStatus.getSelectedItem();
        req.visibility = (String) spinnerVisibility.getSelectedItem();

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.updateEvent(eventId, req).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditEventActivity.this,
                            "Event updated", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditEventActivity.this,
                            "Update failed (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Toast.makeText(EditEventActivity.this,
                        "Cannot reach server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(TextInputEditText field) {
        String val = field.getText() != null ? field.getText().toString().trim() : "";
        return val.isEmpty() ? null : val;
    }
}
