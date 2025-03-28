package com.example.abcd.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.fragment.app.Fragment;

import com.example.abcd.ExamQuizes.examQuizesMainActivity;
import com.example.abcd.ExtrateacherFeatures.examTimeTableCreation;
import com.example.abcd.MathFeature.CGPACalculatorActivity;
import com.example.abcd.MathFeature.EquationSolver;
import com.example.abcd.MathFeature.MathFeatures;
import com.example.abcd.imagesizecompresor;
import com.example.abcd.ocrcapture.ocrcapture;
import com.example.abcd.selectChatModel;
import com.example.abcd.storage;
import com.example.abcd.userMessaging.userSearchingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.abcd.R;
import com.example.abcd.SemestersActivity;

public class dashboardFragment extends Fragment {
    private MaterialButton lastClickedButton = null;
    private ValueAnimator glowAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard2, container, false);

        // Initialize College Support buttons
        setupButton(view,R.id.btnTimeTableCreation, examTimeTableCreation.class);
        setupButton(view, R.id.btnVideos, com.example.abcd.videoplayers1.MainActivity.class);
        setupButton(view,R.id.btnQuizzes, examQuizesMainActivity.class);
        setupButton(view, R.id.btnOldPapers, SemestersActivity.class);
        setupButton(view, R.id.PersonalStorage, CGPACalculatorActivity.class);
        setupButton(view,R.id.btnCoding, imagesizecompresor.class);
        setupButton(view,R.id.btnMath, EquationSolver.class);   ///update
        setupButton(view,R.id.btnDevTools, ocrcapture.class);  /// updated
        setupButton(view,R.id.btnAIModels, selectChatModel.class);   //// userSearchingActivity

        // Initialize Programming section buttons
        // Setup profile button
        setupProfileButton(view);

        // Initialize glow animation
        initGlowAnimation();

        return view;
    }

    private void setupProfileButton(View view) {
        MaterialButton profileButton = view.findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            // Add profile button click handling here
            animateButtonClick(profileButton, null);
        });
    }

    private void setupButton(View view, int buttonId, Class<?> activityClass) {
        MaterialButton button = view.findViewById(buttonId);
        MaterialCardView cardView = (MaterialCardView) button.getParent().getParent();

        button.setOnClickListener(v -> {
            // Animate button click
            animateButtonClick(button, cardView);

            // Start activity
            Intent intent = new Intent(getActivity(), activityClass);
            startActivity(intent);
        });

        // Add glow effect to card
        addGlowEffect(cardView);
    }

    private void setupProgrammingButton(View view, int buttonId) {
        MaterialButton button = view.findViewById(buttonId);
        MaterialCardView cardView = (MaterialCardView) button.getParent().getParent();

        button.setOnClickListener(v -> {
            // Reset previous button if exists
            if (lastClickedButton != null && lastClickedButton != button) {
                resetButtonAnimation(lastClickedButton);
            }

            // Animate current button
            animateButtonClick(button, cardView);
            lastClickedButton = button;
        });

        // Add glow effect to card
        addGlowEffect(cardView);
    }

    private void animateButtonClick(MaterialButton button, MaterialCardView cardView) {
        // Scale animation for button
        button.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                })
                .start();

        // Glow animation for card if available
        if (cardView != null) {
            ValueAnimator colorAnimator = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    Color.parseColor("#3C3C3C"),
                    Color.parseColor("#6200EE"));

            colorAnimator.setDuration(300);
            colorAnimator.addUpdateListener(animator -> {
                cardView.setStrokeColor((int) animator.getAnimatedValue());
            });

            colorAnimator.start();
        }
    }

    private void resetButtonAnimation(MaterialButton button) {
        button.setScaleX(1f);
        button.setScaleY(1f);
    }

    private void addGlowEffect(MaterialCardView cardView) {
        cardView.setStrokeWidth(2);
        cardView.setStrokeColor(Color.parseColor("#3C3C3C"));
    }

    private void initGlowAnimation() {
        glowAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#3C3C3C"),
                Color.parseColor("#6200EE"),
                Color.parseColor("#3C3C3C"));

        glowAnimator.setDuration(2000);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (glowAnimator != null && glowAnimator.isRunning()) {
            glowAnimator.cancel();
        }
    }
}