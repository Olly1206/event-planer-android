package com.example.myapplication.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.BaseActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.AuthResponse;
import com.example.myapplication.network.dto.GuestAuthResponse;
import com.example.myapplication.network.dto.LoginRequest;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister, btnGuestLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already logged in, skip straight to MainActivity
        if (AuthManager.getInstance(this).isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail        = findViewById(R.id.etEmail);
        etPassword     = findViewById(R.id.etPassword);
        btnLogin       = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnGuestLogin  = findViewById(R.id.btnGuestLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnGuestLogin.setOnClickListener(v -> attemptGuestLogin());
    }

    private void attemptLogin() {
        String email    = etEmail.getText() != null    ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse body = response.body();
                    AuthManager.getInstance(LoginActivity.this)
                               .saveSession(body.token, body.userId, body.username, body.role);
                    goToMain();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login failed — check your email and password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void attemptGuestLogin() {
        btnGuestLogin.setEnabled(false);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.loginAsGuest().enqueue(new Callback<GuestAuthResponse>() {
            @Override
            public void onResponse(Call<GuestAuthResponse> call, Response<GuestAuthResponse> response) {
                btnGuestLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    GuestAuthResponse body = response.body();
                    // Save guest session with default guest credentials
                    AuthManager.getInstance(LoginActivity.this)
                               .saveSession(body.token, body.id, "Guest", "GUEST");
                    goToMain();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Guest login failed — please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GuestAuthResponse> call, Throwable t) {
                btnGuestLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
