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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.abcd.ExamQuizes.examQuizesMainActivity;
import com.example.abcd.ExtrateacherFeatures.examTimeTableCreation;
import com.example.abcd.MathFeature.CGPACalculatorActivity;
import com.example.abcd.MathFeature.EquationSolver;
import com.example.abcd.MathFeature.MathFeatures;
import com.example.abcd.R;
import com.example.abcd.SemestersActivity;
import com.example.abcd.imagesizecompresor;
import com.example.abcd.ocrcapture.ocrcapture;
import com.example.abcd.selectChatModel;
import com.example.abcd.userMessaging.userSearchingActivity;
import com.example.abcd.utils.SessionManager;
import com.example.abcd.videoplayers1.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class dashboardFragment extends Fragment {
    private MaterialButton lastClickedButton = null;
    private ValueAnimator glowAnimator;
    private SessionManager sessionManager;
    private DatabaseReference databaseReference;
    private TextView toolbarTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard2, container, false);

        // Initialize SessionManager and Firebase
        sessionManager = new SessionManager(getActivity());
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        toolbarTitle = view.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Dashboard"); // Set static title

        // Setup buttons
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
            String emailKey = userEmail.replace(".", ",");

            databaseReference.child("users").child(emailKey).child("userRole").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String userRole = dataSnapshot.getValue(String.class);

                    // Setup buttons with feature tracking
                    setupButton(view, R.id.btnVideos, MainActivity.class, "video");
                    setupButton(view, R.id.btnQuizzes, examQuizesMainActivity.class, "quizzes");
                    setupButton(view, R.id.btnOldPapers, SemestersActivity.class, "old_paper");
                    setupButton(view, R.id.btnCoding, imagesizecompresor.class, "imagesize_compressor");
                    setupButton(view, R.id.btnDevTools, ocrcapture.class, "doc_scanner");
                    setupButton(view, R.id.btnMath, EquationSolver.class, "math_feature");
                    setupButton(view, R.id.btnAIModels, selectChatModel.class, "message");
                    setupButton(view, R.id.PersonalStorage, CGPACalculatorActivity.class, "cgpa_calculator");

                    // Teacher-specific button
                    MaterialCardView timeTableCard = view.findViewById(R.id.btnTimeTableCreation)
                            .getParent().getParent() instanceof MaterialCardView ?
                            (MaterialCardView) view.findViewById(R.id.btnTimeTableCreation)
                                    .getParent().getParent() : null;

                    if ("teacher".equals(userRole) && timeTableCard != null) {
                        timeTableCard.setVisibility(View.VISIBLE);
                        setupButton(view, R.id.btnTimeTableCreation, examTimeTableCreation.class, null);
                    } else if (timeTableCard != null) {
                        timeTableCard.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error loading role: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupProfileButton(View view) {
        MaterialButton profileButton = view.findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            animateButtonClick(profileButton, null);
            // Add profile activity intent here if needed
        });
    }

    private void setupButton(View view, int buttonId, Class<?> activityClass, String featureKey) {
        MaterialButton button = view.findViewById(buttonId);
        MaterialCardView cardView = (MaterialCardView) button.getParent().getParent();

        button.setOnClickListener(v -> {
            animateButtonClick(button, cardView);
            Intent intent = new Intent(getActivity(), activityClass);
            startActivity(intent);

            // Update feature usage if applicable (kept for analytics)
            if (featureKey != null) {
                updateFeatureUsage(featureKey);
            }
        });

        addGlowEffect(cardView);
    }

    private void updateFeatureUsage(String featureKey) {
        String userEmail = sessionManager.getEmail();
        if (userEmail != null) {
            String emailKey = userEmail.replace(".", ",");
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            DatabaseReference featureRef = databaseReference
                    .child("analytics")
                    .child(todayDate)
                    .child("users")
                    .child(emailKey)
                    .child("feature_touch")
                    .child("0")
                    .child(featureKey);

            featureRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long currentCount = snapshot.getValue(Long.class);
                    if (currentCount == null) currentCount = 0L;
                    featureRef.setValue(currentCount + 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error updating feature: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                    Color.parseColor("#3D3D3D"),
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
        cardView.setStrokeColor(Color.parseColor("#3D3D3D"));
    }

    private void initGlowAnimation() {
        glowAnimator = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.parseColor("#3D3D3D"),
                Color.parseColor("#6200EE"),
                Color.parseColor("#3D3D3D"));

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