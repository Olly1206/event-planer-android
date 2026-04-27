package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.EventResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles the eventplanner://join/{token} deep link.
 *
 * Flow:
 *  1. Parse the token from the Uri.
 *  2. If not logged in — save the token, redirect to LoginActivity.
 *     LoginActivity should call back into this activity (or MainActivity handles
 *     the pending token on login).
 *  3. If logged in — call joinByToken(), then go to MainActivity.
 */
public class JoinEventActivity extends BaseActivity {

    public static final String EXTRA_PENDING_TOKEN = "pending_invite_token";

    private ProgressBar progressJoin;
    private TextView tvEventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event);

        progressJoin = findViewById(R.id.progressJoin);
        tvEventName  = findViewById(R.id.tvJoinEventName);

        Uri data = getIntent().getData();
        if (data == null) {
            Toast.makeText(this, "Invalid invite link", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // URI format: eventplanner://join/{token}
        String token = data.getLastPathSegment();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Invalid invite link", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!AuthManager.getInstance(this).hasActiveSession()) {
            // Save the token so LoginActivity can complete the join after auth
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra(EXTRA_PENDING_TOKEN, token);
            startActivity(loginIntent);
            finish();
            return;
        }

        loadPreviewAndJoin(token);
    }

    private void loadPreviewAndJoin(String token) {
        progressJoin.setVisibility(View.VISIBLE);
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        // First fetch a preview so we can show the event name
        api.previewByToken(token).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvEventName.setText(response.body().title);
                    joinEvent(token);
                } else {
                    progressJoin.setVisibility(View.GONE);
                    Toast.makeText(JoinEventActivity.this,
                            "Invite link not found or expired", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                progressJoin.setVisibility(View.GONE);
                Toast.makeText(JoinEventActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void joinEvent(String token) {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.joinByToken(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressJoin.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(JoinEventActivity.this,
                            "Joined event!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401 || response.code() == 403) {
                    AuthManager.getInstance(JoinEventActivity.this).clearSession();
                    Intent loginIntent = new Intent(JoinEventActivity.this, LoginActivity.class);
                    loginIntent.putExtra(EXTRA_PENDING_TOKEN, token);
                    startActivity(loginIntent);
                    finish();
                    return;
                } else if (response.code() == 409 || response.code() == 400) {
                    // Already a member — that's fine, just navigate
                    Toast.makeText(JoinEventActivity.this,
                            "You're already a member of this event", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(JoinEventActivity.this,
                            "Could not join event (code " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
                startActivity(new Intent(JoinEventActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressJoin.setVisibility(View.GONE);
                Toast.makeText(JoinEventActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
