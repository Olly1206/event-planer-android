package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.EventAdapter;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.EventResponse;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private final List<EventResponse> eventList = new ArrayList<>();
    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvNoEvents;
    private ChipGroup chipGroupFilter;
    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Guard: redirect to login if not authenticated
        if (!AuthManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        recyclerView     = findViewById(R.id.recyclerViewEvents);
        tvNoEvents       = findViewById(R.id.textViewNoEvents);
        chipGroupFilter  = findViewById(R.id.chipGroupFilter);
        searchInputLayout = findViewById(R.id.searchInputLayout);
        etSearch          = findViewById(R.id.etSearch);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddEvent);

        adapter = new EventAdapter(eventList, event -> {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId", event.id);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipCreated) {
                    searchInputLayout.setVisibility(View.GONE);
                    loadCreatedEvents();
                } else if (checkedId == R.id.chipJoined) {
                    searchInputLayout.setVisibility(View.GONE);
                    loadJoinedEvents();
                } else if (checkedId == R.id.chipDiscover) {
                    searchInputLayout.setVisibility(View.VISIBLE);
                    loadAllPublicEvents();
                }
            }
        });

        // Search on keyboard "search" action
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText() != null
                        ? etSearch.getText().toString().trim() : "";
                if (!query.isEmpty()) {
                    searchEvents(query);
                } else {
                    loadAllPublicEvents();
                }
                return true;
            }
            return false;
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, SelectEventTypeActivity.class))
        );

        // Logout button in header
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            AuthManager.getInstance(this).clearSession();
            RetrofitClient.reset();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AuthManager.getInstance(this).isLoggedIn()) {
            int checkedId = chipGroupFilter.getCheckedChipId();
            if (checkedId == R.id.chipJoined) {
                loadJoinedEvents();
            } else if (checkedId == R.id.chipDiscover) {
                loadAllPublicEvents();
            } else {
                loadCreatedEvents();
            }
        }
    }

    private void loadCreatedEvents() {
        Long userId = AuthManager.getInstance(this).getUserId();
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getMyEvents(userId).enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateUI();
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin();
                        return;
                    }
                    Toast.makeText(MainActivity.this,
                            "Failed to load events (code " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadJoinedEvents() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getJoinedEvents().enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateUI();
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin();
                        return;
                    }
                    Toast.makeText(MainActivity.this,
                            "Failed to load joined events (code " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadAllPublicEvents() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getAllEvents().enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateUI();
                } else {
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin();
                        return;
                    }
                    Toast.makeText(MainActivity.this,
                            "Failed to load events (code " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchEvents(String keyword) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.searchEvents(keyword).enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateUI();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Search failed (code " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void redirectToLogin() {
        AuthManager.getInstance(this).clearSession();
        RetrofitClient.reset();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void updateUI() {
        if (eventList.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Logout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            AuthManager.getInstance(this).clearSession();
            RetrofitClient.reset();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}