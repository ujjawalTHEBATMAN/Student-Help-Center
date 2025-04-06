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
import com.example.abcd.AuthenTication.login.loginActivity;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView welcomeTextView, taglineTextView;
    private View loadingIndicator;
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
        taglineTextView = findViewById(R.id.taglineTextView);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Set initial alpha values to 0 for animation
        logoImageView.setAlpha(0f);
        welcomeTextView.setAlpha(0f);
        taglineTextView.setAlpha(0f);
        loadingIndicator.setAlpha(0f);

        // Apply dynamic colors if supported
        applyDynamicColors();

        // Setup gradient background
        setupGradientBackground();

        // Run animation after a slight delay to ensure layout is ready
        logoImageView.postDelayed(this::animateSplashScreenElements, 100);

        // Navigate after splash delay
        checkLoginAndNavigate();
    }

    private void applyDynamicColors() {
        // Apply dynamic colors if available (Material 3 Dynamic Colors)
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
    }

    private void setupGradientBackground() {
        View rootView = findViewById(R.id.splashRootView);
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
            // Note: To actually apply this gradient, you would create a drawable from the shader
            // or use a gradient drawable resource. This code is currently only setting up the gradient.
        });
    }

    private void animateSplashScreenElements() {
        // Scale animation for logo
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImageView, View.SCALE_X, 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImageView, View.SCALE_Y, 0.5f, 1f);

        // Fade in animations
        ObjectAnimator fadeInLogo = ObjectAnimator.ofFloat(logoImageView, View.ALPHA, 0f, 1f);
        ObjectAnimator fadeInWelcome = ObjectAnimator.ofFloat(welcomeTextView, View.ALPHA, 0f, 1f);
        ObjectAnimator fadeInTagline = ObjectAnimator.ofFloat(taglineTextView, View.ALPHA, 0f, 1f);
        ObjectAnimator fadeInLoader = ObjectAnimator.ofFloat(loadingIndicator, View.ALPHA, 0f, 1f);

        // Combine animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(scaleX, scaleY, fadeInLogo, fadeInWelcome, fadeInTagline, fadeInLoader);
        animatorSet.start();
    }

    private void checkLoginAndNavigate() {
        logoImageView.postDelayed(() -> {
            Intent intent;
            if (sessionManager.isLoggedIn()) {
                intent = new Intent(MainActivity.this, mainDashBoard.class);
            } else {
                intent = new Intent(MainActivity.this, loginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}
