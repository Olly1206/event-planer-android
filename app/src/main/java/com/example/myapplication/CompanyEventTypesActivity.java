package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CompanyEventTypesActivity extends BaseActivity {

    private String selectedEventType = "";
    private Button selectedButton = null;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_event_types);

        btnNext = findViewById(R.id.btnNext);
        btnNext.setEnabled(false);
        LinearLayout container = findViewById(R.id.buttonContainer);

        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            if (view instanceof Button) {
                final Button button = (Button) view;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectButton(button);
                    }
                });
            }
        }

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CompanyEventTypesActivity.this, EventOptionsActivity.class);
                intent.putExtra("EVENT_TYPE", selectedEventType);
                startActivity(intent);
            }
        });
    }

    private void selectButton(Button button) {
        // Reset previous selection
        if (selectedButton != null) {
            selectedButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            // Re-apply outlined style logically if needed, but for simplicity we'll toggle background
        }

        // Set new selection
        selectedButton = button;
        selectedEventType = button.getText().toString();
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, androidx.appcompat.R.color.material_deep_teal_200));
        
        btnNext.setEnabled(true);
    }
}