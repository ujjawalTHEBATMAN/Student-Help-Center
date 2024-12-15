package com.example.abcd;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.util.Random;

public class mainDashBoard extends AppCompatActivity {

    private MaterialCardView dynamicCardView;
    private TextView cardTitle, cardSubtitle;
    private ImageView cardImage, profileImageToolbar;

    // Card Cycling Variables
    private Handler handler;
    private int currentCardIndex = 0;

    // Session Management
    private SessionManager sessionManager;

    // Dynamic Card Data
    private String[] cardTitles = {
            "BCA Help Center",
            "AI Integration",
            "Messaging Support"
    };

    private String[] cardSubtitles = {
            "BCA Help Center offers study material, old papers, and quizzes",
            "AI integration offers profile image generation, resume creation, and chatbot services",
            "Messaging support offers real-time communication, instant assistance, and customer service"
    };

    private int[] cardImages = {
            R.drawable.cardviewimage1,
            R.drawable.cardviewimage2,
            R.drawable.cardviewimage3
    };

    // Profile Image Resources
    private int[] availableProfileImages = {
            R.drawable.profile1,
            R.drawable.profile2,
            R.drawable.profile3,
            R.drawable.profile4,
            R.drawable.profile5
    };

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);

        // Initialize Session Manager
        sessionManager = new SessionManager(this);

        // Initialize Views
        initializeViews();

        // Setup Profile Image
        setupProfileImage();

        // Set up Profile Icon Click
        setupProfileIconClick();

        // Set up Dynamic Card Click
        setupDynamicCardClick();

        // Start Card Changer
        startCardChanger();





    }

    private void initializeViews() {
        profileImageToolbar = findViewById(R.id.profileImageToolbar);
        dynamicCardView = findViewById(R.id.dynamicCardView);
        cardTitle = findViewById(R.id.cardTitle);
        cardSubtitle = findViewById(R.id.cardSubtitle);
        cardImage = findViewById(R.id.cardImage);

        // Initialize Handler
        handler = new Handler();
    }

    private void setupProfileImage() {
        // Check if a profile image is already saved
        int savedImageId = sessionManager.getProfileImage();

        if (savedImageId == -1) {
            // No saved image, select a random one
            savedImageId = selectRandomProfileImage();
            sessionManager.saveProfileImage(savedImageId);
        }

        // Set the profile image
        profileImageToolbar.setImageResource(savedImageId);
    }

    private int selectRandomProfileImage() {
        Random random = new Random();
        int randomIndex = random.nextInt(availableProfileImages.length);
        return availableProfileImages[randomIndex];
    }

    private void setupProfileIconClick() {
        profileImageToolbar.setOnClickListener(v -> {
            Intent intent = new Intent(mainDashBoard.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupDynamicCardClick() {
        dynamicCardView.setOnClickListener(v ->
                Toast.makeText(
                        mainDashBoard.this,
                        "Learn More: " + cardTitle.getText(),
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void startCardChanger() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update card content dynamically
                currentCardIndex = (currentCardIndex + 1) % cardTitles.length;

                cardTitle.setText(cardTitles[currentCardIndex]);
                cardSubtitle.setText(cardSubtitles[currentCardIndex]);
                cardImage.setImageResource(cardImages[currentCardIndex]);

                // Schedule next card change
                handler.postDelayed(this, 3000); // Change every 3 seconds
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}