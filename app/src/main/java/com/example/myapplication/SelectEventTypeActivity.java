package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class SelectEventTypeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_event_type);

        View buttonCompany = findViewById(R.id.buttonCompany);
        buttonCompany.setOnClickListener(v -> {
            Intent intent = new Intent(SelectEventTypeActivity.this, CompanyEventTypesActivity.class);
            startActivity(intent);
        });

        View buttonPrivate = findViewById(R.id.buttonPrivate);
        buttonPrivate.setOnClickListener(v -> {
            Intent intent = new Intent(SelectEventTypeActivity.this, PrivateEventTypesActivity.class);
            startActivity(intent);
        });
    }
}