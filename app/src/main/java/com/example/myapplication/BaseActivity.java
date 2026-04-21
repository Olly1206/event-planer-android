package com.example.myapplication;

import android.os.Bundle;
import android.view.WindowInsetsController;
import android.view.WindowInsets;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base class for every Activity in this app.
 *
 * Enables sticky-immersive mode: the system navigation bar (home, back,
 * recents) is hidden but slides back in temporarily when the user swipes up
 * from the bottom edge. This keeps the UI unobstructed by default.
 *
 * Also provides back button support via onSupportNavigateUp().
 *
 * minSdk = 30 so we can use the non-deprecated WindowInsetsController API
 * directly without the compat wrappers.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The back button will be automatically shown by ActionBar if parent activity is set
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Back button in the action bar will call finish()
        finish();
        return true;
    }

    private void hideSystemBars() {
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            // Hide both the top status bar and the bottom navigation bar
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}
