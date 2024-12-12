package com.example.abcd;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class mainDashBoard extends AppCompatActivity {

    private TextView usernameText, profileActionText;
    private MaterialButton logoutButton, editProfileButton;
    private ShapeableImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        usernameText = findViewById(R.id.tvUsername);
        profileActionText = findViewById(R.id.profileActionText);
        logoutButton = findViewById(R.id.btnLogout);
        editProfileButton = findViewById(R.id.btnEditProfile);

        // Setup toolbar
        setupToolbar();

        // Add gradient animation to the username
        setupUsernameGradientAnimation();

        // Show full-screen image on profile image click
        profileImage.setOnClickListener(v -> showFullScreenImage());

        // Profile action click listener
        profileActionText.setOnClickListener(v -> openProfile());

        // Logout button click listener
        logoutButton.setOnClickListener(v -> logout());

        // Edit profile button click listener
        editProfileButton.setOnClickListener(v ->
                startActivity(new Intent(mainDashBoard.this, ProfileActivity.class))
        );

        // Fetch user data from Firebase
        fetchUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
    }

    private void setupUsernameGradientAnimation() {
        usernameText.animate()
                .setDuration(200)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withStartAction(() -> {
                    int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN};
                    int randomColor = colors[(int) (Math.random() * colors.length)];
                    usernameText.setTextColor(randomColor);
                })
                .withEndAction(this::setupUsernameGradientAnimation)
                .start();
    }

    private void showFullScreenImage() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        dialog.setContentView(R.layout.dialog_fullscreen_image);

        ImageView fullScreenImageView = dialog.findViewById(R.id.fullScreenImageView);
        fullScreenImageView.setImageDrawable(profileImage.getDrawable());

        fullScreenImageView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchUserData() {
        SessionManager sessionManager = new SessionManager(this);
        String email = sessionManager.getEmail();

        if (email != null) {
            String sanitizedEmail = email.replace(".", ",");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(sanitizedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        HelperClassPOJO user = snapshot.getValue(HelperClassPOJO.class);
                        if (user != null) {
                            usernameText.setText("Hi, " + user.getUser().toUpperCase());
                            // Optional: Load profile image using Glide or other library
                            // Glide.with(mainDashBoard.this).load(user.getProfileImage()).into(profileImage);
                        }
                    } else {
                        Toast.makeText(mainDashBoard.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(mainDashBoard.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openProfile() {
        startActivity(new Intent(mainDashBoard.this, ProfileActivity.class));
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logout();

        Intent intent = new Intent(mainDashBoard.this, loginActivity1.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
