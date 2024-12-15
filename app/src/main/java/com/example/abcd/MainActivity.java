package com.example.abcd;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.color.DynamicColors;
import com.example.abcd.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView welcomeTextView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Install system splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Initialize views
        logoImageView = findViewById(R.id.logoImageView);
        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Apply dynamic colors if supported
        applyDynamicColors();

        // Setup gradient background
        setupGradientBackground();

        // Animate splash screen elements
        animateSplashScreenElements();

        // Navigate based on login status
        checkLoginAndNavigate();
    }

    private void applyDynamicColors() {
        // Apply dynamic colors if available (Material 3 Dynamic Colors)
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
    }

    private void setupGradientBackground() {
        View rootView = findViewById(R.id.splashRootView);

        // Define gradient colors
        int[] colors = {
                getResources().getColor(R.color.gradient_start, null),
                getResources().getColor(R.color.gradient_end, null)
        };

        rootView.post(() -> {
            LinearGradient gradient = new LinearGradient(
                    0, 0,
                    rootView.getWidth(), rootView.getHeight(),
                    colors,
                    null,
                    Shader.TileMode.CLAMP
            );
        });
    }

    private void animateSplashScreenElements() {
        // Scale animation for logo
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 0.5f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 0.5f, 1f);

        // Fade in animation
        ObjectAnimator fadeInLogo = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0f, 1f);
        ObjectAnimator fadeInText = ObjectAnimator.ofFloat(welcomeTextView, View.ALPHA, 0f, 1f);

        // Combine animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(
                scaleXAnimator,
                scaleYAnimator,
                fadeInLogo,
                fadeInText
        );
        animatorSet.start();
    }

    private void checkLoginAndNavigate() {
        // Delay navigation to simulate splash screen
        logoImageView.postDelayed(() -> {
            Intent intent;
            if (sessionManager.isLoggedIn()) {

                intent = new Intent(MainActivity.this, mainDashBoard.class);
            } else {

                intent = new Intent(MainActivity.this, loginActivity2.class);
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}