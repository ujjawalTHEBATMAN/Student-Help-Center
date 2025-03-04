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

import com.example.abcd.MathFeature.MathFeatures;
import com.example.abcd.imagesizecompresor;
import com.example.abcd.ocrcapture.ocrcapture;
import com.example.abcd.selectChatModel;
import com.example.abcd.storage;
import com.example.abcd.userMessaging.userSearchingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.abcd.R;
import com.example.abcd.QuizActivity;
import com.example.abcd.SemestersActivity;

public class dashboardFragment extends Fragment {
    private MaterialButton lastClickedButton = null;
    private ValueAnimator glowAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard2, container, false);

        // Initialize College Support buttons
        setupButton(view, R.id.btnVideos, com.example.abcd.videoplayers1.MainActivity.class);
        setupButton(view, R.id.btnQuizzes, QuizActivity.class);
        setupButton(view, R.id.btnOldPapers, SemestersActivity.class);
        setupButton(view, R.id.PersonalStorage, storage.class);
        setupButton(view,R.id.btnCoding, imagesizecompresor.class);
        setupButton(view,R.id.btnMath, MathFeatures.class);
        setupButton(view,R.id.btnDevTools, ocrcapture.class);
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

    private void initGlowAnimation() {
        glowAnimator = ValueAnimator.ofFloat(0f, 1f);
        glowAnimator.setDuration(2000);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private void addGlowEffect(MaterialCardView cardView) {
        ValueAnimator glowAnim = ValueAnimator.ofFloat(1f, 1.05f);
        glowAnim.setDuration(1500);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        glowAnim.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            cardView.setScaleX(scale);
            cardView.setScaleY(scale);
        });

        glowAnim.start();
    }

    private void animateButtonClick(MaterialButton button, MaterialCardView cardView) {
        // Button color animation
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#3C3C3C"),
                Color.parseColor("#4F8EFF")
        );

        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator ->
                button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        (Integer) animator.getAnimatedValue()
                ))
        );

        // Card elevation and scale animation if cardView exists
        if (cardView != null) {
            cardView.animate()
                    .translationZ(24f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        cardView.animate()
                                .translationZ(8f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(300)
                                .start();
                    })
                    .start();
        }

        colorAnimation.start();
    }

    private void resetButtonAnimation(MaterialButton button) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#4F8EFF"),
                Color.parseColor("#3C3C3C")
        );

        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator ->
                button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        (Integer) animator.getAnimatedValue()
                ))
        );

        colorAnimation.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (glowAnimator != null) {
            glowAnimator.cancel();
        }
    }
}