package com.example.abcd;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.abcd.models.RewardManager.Reward;
import com.example.abcd.models.UserStats;
import com.example.abcd.utils.SessionManager;
import android.widget.Button;
import android.content.Intent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RewardActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private DatabaseReference userStatsRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);
        String email = sessionManager.getEmail();
        if (email != null) {
            String sanitizedEmail = email.replace(".", ",");
            userStatsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(sanitizedEmail)
                .child("stats");
        }

        // Get views
        TextView titleText = findViewById(R.id.rewardTitle);
        TextView messageText = findViewById(R.id.rewardMessage);
        TextView pointsText = findViewById(R.id.pointsText);
        TextView motivationalText = findViewById(R.id.motivationalMessage);
        ImageView trophyImage = findViewById(R.id.trophyImage);
        Button continueButton = findViewById(R.id.continueButton);

        // Get reward data from intent
        String title = getIntent().getStringExtra("REWARD_TITLE");
        String message = getIntent().getStringExtra("REWARD_MESSAGE");
        int points = getIntent().getIntExtra("REWARD_POINTS", 0);
        String motivationalMsg = getIntent().getStringExtra("MOTIVATIONAL_MESSAGE");

        // Set texts
        titleText.setText(title);
        messageText.setText(message);
        pointsText.setText("+" + points + " points!");
        motivationalText.setText(motivationalMsg);

        // Update user stats in Firebase
        updateUserStats(points);

        // Animate trophy
        ObjectAnimator rotation = ObjectAnimator.ofFloat(trophyImage, "rotationY", 0f, 360f);
        rotation.setDuration(2000);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation.start();

        // Show points with animation
        pointsText.setAlpha(0f);
        pointsText.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .start();

        // Continue button click
        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateUserStats(final int points) {
        if (userStatsRef == null) return;

        userStatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserStats stats;
                if (dataSnapshot.exists()) {
                    stats = dataSnapshot.getValue(UserStats.class);
                } else {
                    stats = new UserStats(0, 0, 0);
                }

                if (stats != null) {
                    stats.incrementTotalQuizzes();
                    stats.addPoints(points);
                    userStatsRef.setValue(stats);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Handle back button same as continue button
        Intent intent = new Intent(this, QuizActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
