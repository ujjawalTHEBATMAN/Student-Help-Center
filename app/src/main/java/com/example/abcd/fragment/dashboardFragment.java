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
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.abcd.R;
import com.example.abcd.QuizActivity;
import com.example.abcd.SemestersActivity;

public class dashboardFragment extends Fragment {
    private MaterialButton lastClickedButton = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard2, container, false);

        // Initialize buttons
        setupButton(view, R.id.VideoMaterial, com.example.abcd.videoplayers1.MainActivity.class);
            setupButton(view, R.id.Quizes, QuizActivity.class);
            setupButton(view, R.id.oldpaper, SemestersActivity.class);

        // Initialize programming section buttons
        setupProgrammingButton(view, R.id.btnProgramming);
        setupProgrammingButton(view, R.id.btnDevTools);
        setupProgrammingButton(view, R.id.btnMath);


        return view;
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
    }

    private void setupProgrammingButton(View view, int buttonId) {
        MaterialButton button = view.findViewById(buttonId);
        MaterialCardView cardView = (MaterialCardView) button.getParent().getParent();

        button.setOnClickListener(v -> {
            // Reset previous button if exists
            if (lastClickedButton != null) {
                resetButtonAnimation(lastClickedButton);
            }

            // Animate current button
            animateButtonClick(button, cardView);
            lastClickedButton = button;
        });
    }

    private void animateButtonClick(MaterialButton button, MaterialCardView cardView) {
        // Button color animation
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#3C3C3C"),
                Color.parseColor("#FFFFFF")
        );

        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator ->
                button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        (Integer) animator.getAnimatedValue()
                ))
        );

        // Card elevation animation
        cardView.animate()
                .translationZ(16f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        colorAnimation.start();
    }

    private void resetButtonAnimation(MaterialButton button) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#FFFFFF"),
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
}