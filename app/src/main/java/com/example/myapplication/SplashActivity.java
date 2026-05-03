package com.example.myapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.auth.LoginActivity;

public class SplashActivity extends BaseActivity {

    private static final long ANIMATION_DURATION_MS = 850L;
    private static final long ROUTE_DELAY_MS = 1150L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View content = findViewById(R.id.splashContent);
        content.setAlpha(0f);
        content.setScaleX(0.88f);
        content.setScaleY(0.88f);
        content.setTranslationY(24f);

        AnimatorSet intro = new AnimatorSet();
        intro.playTogether(
                ObjectAnimator.ofFloat(content, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(content, View.SCALE_X, 0.88f, 1f),
                ObjectAnimator.ofFloat(content, View.SCALE_Y, 0.88f, 1f),
                ObjectAnimator.ofFloat(content, View.TRANSLATION_Y, 24f, 0f)
        );
        intro.setDuration(ANIMATION_DURATION_MS);
        intro.setInterpolator(new DecelerateInterpolator());
        intro.start();

        new Handler(Looper.getMainLooper()).postDelayed(this::routeToApp, ROUTE_DELAY_MS);
    }

    private void routeToApp() {
        Class<?> destination = AuthManager.getInstance(this).hasActiveSession()
                ? MainActivity.class
                : LoginActivity.class;
        startActivity(new Intent(this, destination));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
