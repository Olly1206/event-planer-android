package com.example.myapplication;

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
 * minSdk = 30 so we can use the non-deprecated WindowInsetsController API
 * directly without the compat wrappers.
 */
public abstract class BaseActivity extends AppCompatActivity {

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
