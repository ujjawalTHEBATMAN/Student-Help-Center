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
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class dashboardFragment extends Fragment {
    private MaterialButton lastClickedButton = null;
    private ValueAnimator glowAnimator;
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard2, container, false);

        // Initialize SessionManager and Firebase
        sessionManager = new SessionManager(getActivity());
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize College Support buttons
        setupButtonsBasedOnRole(view);

        // Setup profile button
        setupProfileButton(view);

        // Initialize glow animation
        initGlowAnimation();

        return view;
    }

    private void setupButtonsBasedOnRole(View view) {
        String userEmail = sessionManager.getEmail();
        if (userEmail != null) {
            // Sanitize email for Firebase path (replace . with ,)
            String emailKey = userEmail.replace(".", ",");

            databaseReference.child(emailKey).child("userRole").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userRole = dataSnapshot.getValue(String.class);

                    // Setup common buttons for all users
                    setupButton(view, R.id.btnVideos, com.example.abcd.videoplayers1.MainActivity.class);
                    setupButton(view, R.id.btnQuizzes, examQuizesMainActivity.class);
                    setupButton(view, R.id.btnOldPapers, SemestersActivity.class);
                    setupButton(view, R.id.PersonalStorage, CGPACalculatorActivity.class);
                    setupButton(view, R.id.btnCoding, imagesizecompresor.class);
                    setupButton(view, R.id.btnMath, EquationSolver.class);
                    setupButton(view, R.id.btnDevTools, ocrcapture.class);
                    setupButton(view, R.id.btnAIModels, selectChatModel.class);

                    // Show TimeTableCreation button only for teachers
                    MaterialCardView timeTableCard = view.findViewById(R.id.btnTimeTableCreation)
                            .getParent().getParent() instanceof MaterialCardView ?
                            (MaterialCardView) view.findViewById(R.id.btnTimeTableCreation)
                                    .getParent().getParent() : null;

                    if ("teacher".equals(userRole) && timeTableCard != null) {
                        timeTableCard.setVisibility(View.VISIBLE);
                        setupButton(view, R.id.btnTimeTableCreation, examTimeTableCreation.class);
                    } else if (timeTableCard != null) {
                        timeTableCard.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    private void setupProfileButton(View view) {
        MaterialButton profileButton = view.findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            animateButtonClick(profileButton, null);
        });
    }

    private void setupButton(View view, int buttonId, Class<?> activityClass) {
        MaterialButton button = view.findViewById(buttonId);
        MaterialCardView cardView = (MaterialCardView) button.getParent().getParent();

        button.setOnClickListener(v -> {
            animateButtonClick(button, cardView);
            Intent intent = new Intent(getActivity(), activityClass);
            startActivity(intent);
        });

        addGlowEffect(cardView);
    }

    private void animateButtonClick(MaterialButton button, MaterialCardView cardView) {
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