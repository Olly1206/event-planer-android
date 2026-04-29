package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private final List<EventResponse> eventList = new ArrayList<>();
    private final Set<Long> joinedEventIds = new HashSet<>();
    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvNoEvents;
    private TextView tvLoadingEvents;
    private View loadingStateContainer;
    private ProgressBar progressEvents;
    private ChipGroup chipGroupFilter;
    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Guard: redirect to login if not authenticated
        if (!AuthManager.getInstance(this).hasActiveSession()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        recyclerView     = findViewById(R.id.recyclerViewEvents);
        tvNoEvents       = findViewById(R.id.textViewNoEvents);
        tvLoadingEvents   = findViewById(R.id.textViewLoadingEvents);
        loadingStateContainer = findViewById(R.id.loadingStateContainer);
        progressEvents    = findViewById(R.id.progressEvents);
        chipGroupFilter  = findViewById(R.id.chipGroupFilter);
        searchInputLayout = findViewById(R.id.searchInputLayout);
        etSearch          = findViewById(R.id.etSearch);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddEvent);

        adapter = new EventAdapter(eventList, event -> {
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("eventId", event.id);
            startActivity(intent);
        });
        adapter.setJoinedEventIds(joinedEventIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipCreated) {
                    searchInputLayout.setVisibility(View.GONE);
                    adapter.setJoinListener(null);
                    loadCreatedEvents();
                } else if (checkedId == R.id.chipJoined) {
                    searchInputLayout.setVisibility(View.GONE);
                    adapter.setJoinListener(null);
                    loadJoinedEvents();
                } else if (checkedId == R.id.chipDiscover) {
                    searchInputLayout.setVisibility(View.VISIBLE);
                    adapter.setJoinListener(this::joinDiscoverEvent);
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
        if (!AuthManager.getInstance(this).hasActiveSession()) {
            redirectToLogin();
            return;
        }

        int checkedId = chipGroupFilter.getCheckedChipId();
        if (checkedId == R.id.chipJoined) {
            adapter.setJoinListener(null);
            loadJoinedEvents();
        } else if (checkedId == R.id.chipDiscover) {
            adapter.setJoinListener(this::joinDiscoverEvent);
            loadAllPublicEvents();
        } else {
            adapter.setJoinListener(null);
            loadCreatedEvents();
        }
    }

    private void loadCreatedEvents() {
        showLoadingState("Loading your created events…");
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getCreatedEvents().enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    hideLoadingState();
                    updateUI();
                } else {
                    hideLoadingState();
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
                hideLoadingState();
                showNetworkFailure(t);
            }
        });
    }

    private void loadJoinedEvents() {
        showLoadingState("Loading events you joined…");
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getJoinedEvents().enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    joinedEventIds.clear();
                    for (EventResponse event : eventList) {
                        if (event.id != null) {
                            joinedEventIds.add(event.id);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    hideLoadingState();
                    updateUI();
                } else {
                    hideLoadingState();
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
                hideLoadingState();
                showNetworkFailure(t);
            }
        });
    }

    private void loadAllPublicEvents() {
        showLoadingState("Loading discover events…");
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getAllEvents().enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    hideLoadingState();
                    updateUI();
                } else {
                    hideLoadingState();
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
                hideLoadingState();
                showNetworkFailure(t);
            }
        });
    }

    private void searchEvents(String keyword) {
        showLoadingState("Searching public events…");
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.searchEvents(keyword).enqueue(new Callback<List<EventResponse>>() {
            @Override
            public void onResponse(Call<List<EventResponse>> call, Response<List<EventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    hideLoadingState();
                    updateUI();
                } else {
                    hideLoadingState();
                    Toast.makeText(MainActivity.this,
                            "Search failed (code " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventResponse>> call, Throwable t) {
                hideLoadingState();
                showNetworkFailure(t);
            }
        });
    }

    private void joinDiscoverEvent(EventResponse event) {
        if (event == null || event.id == null) {
            Toast.makeText(this, "Cannot join this event right now", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.joinEvent(event.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (event.id != null) {
                        joinedEventIds.add(event.id);
                    }
                    Toast.makeText(MainActivity.this,
                            "Joined event!", Toast.LENGTH_SHORT).show();
                    loadAllPublicEvents();
                } else if (response.code() == 409 || response.code() == 400) {
                    if (event.id != null) {
                        joinedEventIds.add(event.id);
                    }
                    Toast.makeText(MainActivity.this,
                            "You're already a member of this event", Toast.LENGTH_SHORT).show();
                    loadAllPublicEvents();
                } else if (response.code() == 401 || response.code() == 403) {
                    Toast.makeText(MainActivity.this,
                            "Session expired — please log in again", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Failed to join (code " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showNetworkFailure(t);
            }
        });
    }

    private void redirectToLogin() {
        AuthManager.getInstance(this).clearSession();
        RetrofitClient.reset();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showNetworkFailure(Throwable t) {
        String message = t != null && t.getMessage() != null
                ? t.getMessage()
                : "Cannot reach server right now";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showLoadingState(String message) {
        if (tvLoadingEvents != null) {
            tvLoadingEvents.setText(message);
        }
        if (loadingStateContainer != null) {
            loadingStateContainer.setVisibility(View.VISIBLE);
        }
        if (progressEvents != null) {
            progressEvents.setVisibility(View.VISIBLE);
        }
        tvNoEvents.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        if (loadingStateContainer != null) {
            loadingStateContainer.setVisibility(View.GONE);
        }
        if (progressEvents != null) {
            progressEvents.setVisibility(View.GONE);
        }
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
