package com.example.myapplication;

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
import com.example.myapplication.network.dto.VendorResponse;

import java.util.ArrayList;
import java.util.List;

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

    private final List<VendorResponse> vendorList = new ArrayList<>();
    private VendorAdapter adapter;
    private ProgressBar progress;
    private TextView tvNoVendors;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_suggestions);

        progress    = findViewById(R.id.progressVendors);
        tvNoVendors = findViewById(R.id.tvNoVendors);
        recycler    = findViewById(R.id.recyclerVendors);

        adapter = new VendorAdapter(vendorList);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

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
