package com.example.myapplication.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.myapplication.BaseActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.network.dto.AuthResponse;
import com.example.myapplication.network.dto.RegisterRequest;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private TextInputEditText etUsername, etEmail, etPassword;
    private RadioGroup radioGroupRole;
    private Button btnRegister, btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername     = findViewById(R.id.etUsername);
        etEmail        = findViewById(R.id.etEmail);
        etPassword     = findViewById(R.id.etPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        btnRegister    = findViewById(R.id.btnRegister);
        btnGoToLogin   = findViewById(R.id.btnGoToLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());

        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read the selected radio button to determine role
        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        RadioButton selected = findViewById(selectedId);
        String role = selected.getText().toString().equals("Company") ? "COMPANY" : "PRIVATE";

        btnRegister.setEnabled(false);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.register(new RegisterRequest(username, email, password, role))
           .enqueue(new Callback<AuthResponse>() {
               @Override
               public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                   btnRegister.setEnabled(true);
                   if (response.isSuccessful() && response.body() != null) {
                       AuthResponse body = response.body();
                       AuthManager.getInstance(RegisterActivity.this)
                                  .saveSession(body.token, body.userId, body.username, body.role);
                       startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                       finish();
                   } else {
                       String msg = response.code() == 400
                               ? "Email or username already taken"
                               : "Registration failed (code " + response.code() + ")";
                       Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                   }
               }

               @Override
               public void onFailure(Call<AuthResponse> call, Throwable t) {
                   btnRegister.setEnabled(true);
                   Toast.makeText(RegisterActivity.this,
                           "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
               }
           });
    }
}
