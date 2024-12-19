package com.example.abcd.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.abcd.R;
import com.example.abcd.QuizActivity;
import com.example.abcd.SemestersActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class dashboardFragment extends Fragment {
    private CardView slidingCardView;
    private ImageView slideImage;
    private TextView slideName;
    private Handler handler;
    private Runnable slideRunnable;
    private int currentCardIndex = 0;

    private final SlideData[] slides = new SlideData[]{
            new SlideData(R.drawable.ic_chatbot, "Featured Content"),
            new SlideData(R.drawable.ic_stats, "Latest Updates"),
            new SlideData(R.drawable.ic_username, "Trending Topics")
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard2, container, false);
        initializeViews(view);
        setupSlideShow();
        return view;
    }

    private void initializeViews(@NonNull View view) {
        slidingCardView = view.findViewById(R.id.slidingCardView);
        slideImage = view.findViewById(R.id.slideImage);
        slideName = view.findViewById(R.id.slideName);

        // Set click listener for button2 (OLD PAPERS) to go to SemestersActivity
        RelativeLayout quizLayout = view.findViewById(R.id.quizLayout);
        if (quizLayout != null) {
            quizLayout.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SemestersActivity.class);
                startActivity(intent);
            });
        }

        // Set click listener for button3 (QUIZES) to go to QuizActivity
        RelativeLayout button3Layout = view.findViewById(R.id.button3Layout);
        if (button3Layout != null) {
            button3Layout.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), QuizActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupSlideShow() {
        handler = new Handler(Looper.getMainLooper());
        slideRunnable = new Runnable() {
            @Override
            public void run() {
                updateSlide();
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(slideRunnable, 3000);
    }

    private void updateSlide() {
        if (!isAdded() || slidingCardView == null) return;

        SlideData currentSlide = slides[currentCardIndex];

        // Fancy animation
        slidingCardView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .alpha(0.7f)
                .setDuration(300)
                .withEndAction(() -> {
                    slideImage.setImageResource(currentSlide.imageRes);
                    slideName.setText(currentSlide.name);

                    slidingCardView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(300);
                });

        currentCardIndex = (currentCardIndex + 1) % slides.length;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && slideRunnable != null) {
            handler.postDelayed(slideRunnable, 3000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && slideRunnable != null) {
            handler.removeCallbacks(slideRunnable);
        }
    }

    private static class SlideData {
        int imageRes;
        String name;

        SlideData(int imageRes, String name) {
            this.imageRes = imageRes;
            this.name = name;
        }
    }
}