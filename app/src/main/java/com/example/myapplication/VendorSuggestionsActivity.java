package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.VendorAdapter;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.SaveEventVendorRequest;
import com.example.myapplication.network.dto.VendorResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays vendor suggestions from OpenStreetMap based on the event's city
 * and selected options (e.g. catering, photography).
 */
public class VendorSuggestionsActivity extends BaseActivity {

    public static final String EXTRA_CITY    = "vendor_city";
    public static final String EXTRA_OPTIONS = "vendor_options";
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_ALLOW_ADD = "allow_add_vendor";
    public static final String EXTRA_SELECTED_VENDOR_IDS = "selected_vendor_ids";

    private final List<VendorResponse> vendorList = new ArrayList<>();
    private final Set<Long> selectedVendorIds = new HashSet<>();
    private VendorAdapter adapter;
    private ProgressBar progress;
    private TextView tvNoVendors;
    private RecyclerView recycler;
    private long eventId = -1L;
    private boolean allowAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_suggestions);

        progress    = findViewById(R.id.progressVendors);
        tvNoVendors = findViewById(R.id.tvNoVendors);
        recycler    = findViewById(R.id.recyclerVendors);
        MaterialButton btnBackToMainMenu = findViewById(R.id.btnBackToMainMenu);

        eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1L);
        allowAdd = getIntent().getBooleanExtra(EXTRA_ALLOW_ADD, false);

        long[] selectedIds = getIntent().getLongArrayExtra(EXTRA_SELECTED_VENDOR_IDS);
        if (selectedIds != null) {
            for (long selectedId : selectedIds) {
                selectedVendorIds.add(selectedId);
            }
        }

        adapter = new VendorAdapter(vendorList, allowAdd, selectedVendorIds, new VendorAdapter.Listener() {
            @Override
            public void onVendorClicked(VendorResponse vendor) {
                VendorDetailsDialog.show(VendorSuggestionsActivity.this, vendor);
            }

            @Override
            public void onVendorActionClicked(VendorResponse vendor, boolean alreadyAdded) {
                if (alreadyAdded) {
                    removeVendorFromEvent(vendor);
                } else {
                    addVendorToEvent(vendor);
                }
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        btnBackToMainMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        String city = getIntent().getStringExtra(EXTRA_CITY);
        ArrayList<String> options = getIntent().getStringArrayListExtra(EXTRA_OPTIONS);

        if (city == null || city.isEmpty()) {
            Toast.makeText(this, "No city provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (options == null || options.isEmpty()) {
            Toast.makeText(this, "No options provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadVendors(city, options);
    }

    private void addVendorToEvent(VendorResponse vendor) {
        if (!allowAdd || eventId == -1L) {
            return;
        }

        SaveEventVendorRequest request = new SaveEventVendorRequest();
        request.osmId = vendor.osmId;
        request.name = vendor.name;
        request.address = vendor.address;
        request.category = vendor.category;
        request.optionName = vendor.optionName;
        request.website = vendor.website;
        request.email = vendor.email;
        request.phone = vendor.phone;

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.addVendorToEvent(eventId, request).enqueue(new Callback<VendorResponse>() {
            @Override
            public void onResponse(Call<VendorResponse> call, Response<VendorResponse> response) {
                if (response.isSuccessful()) {
                    adapter.markVendorAdded(vendor.osmId);
                    Toast.makeText(VendorSuggestionsActivity.this,
                            "Vendor added to event", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 409 || response.code() == 400) {
                    adapter.markVendorAdded(vendor.osmId);
                    Toast.makeText(VendorSuggestionsActivity.this,
                            "Vendor is already attached to this event", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VendorSuggestionsActivity.this,
                            "Could not add vendor (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VendorResponse> call, Throwable t) {
                Toast.makeText(VendorSuggestionsActivity.this,
                        "Cannot save vendor: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeVendorFromEvent(VendorResponse vendor) {
        if (!allowAdd || eventId == -1L || vendor.osmId == null) {
            return;
        }

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.removeVendorFromEvent(eventId, vendor.osmId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.markVendorRemoved(vendor.osmId);
                    Toast.makeText(VendorSuggestionsActivity.this,
                            "Vendor removed from event", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    adapter.markVendorRemoved(vendor.osmId);
                    Toast.makeText(VendorSuggestionsActivity.this,
                            "Vendor is not attached to this event", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VendorSuggestionsActivity.this,
                            "Could not remove vendor (code " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(VendorSuggestionsActivity.this,
                        "Cannot remove vendor: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadVendors(String city, List<String> options) {
        progress.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
        tvNoVendors.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getVendors(city, 5000, options)
                .enqueue(new Callback<List<VendorResponse>>() {
                    @Override
                    public void onResponse(Call<List<VendorResponse>> call,
                                           Response<List<VendorResponse>> response) {
                        progress.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            vendorList.clear();
                            vendorList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                            recycler.setVisibility(View.VISIBLE);
                        } else {
                            tvNoVendors.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<VendorResponse>> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(VendorSuggestionsActivity.this,
                                "Cannot reach server: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                        tvNoVendors.setVisibility(View.VISIBLE);
                    }
                });
    }
}
