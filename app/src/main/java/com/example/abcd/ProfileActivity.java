package com.example.abcd;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    // Profile Basic Information Views
    private MaterialTextView tvUsername, tvUserEmail, tvUserRole;
    private MaterialTextView tvPostsCount, tvFollowersCount, tvFollowingCount;
    private MaterialButton btnLogout;
    private ShapeableImageView profileImage;
    private Toolbar toolbar;

    // Session and Firebase Management
    private SessionManager sessionManager;
    private String email;
    private DatabaseReference userRef;
    private ValueEventListener userDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize SessionManager and get email from session
        sessionManager = new SessionManager(this);
        email = sessionManager.getEmail();

        // Set profile image from session
        setProfileImage();

        // Validate and load user data
        validateAndLoadUserData();

        // Setup click listeners
        setupClickListeners();
    }

    @SuppressLint("WrongViewCast")
    private void initializeViews() {
        // Profile Basic Information
        toolbar = findViewById(R.id.toolbar);
        tvUsername = findViewById(R.id.tvUsername);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserRole = findViewById(R.id.tvUserRole);
        profileImage = findViewById(R.id.profileImage);

        // Profile Stats
        tvPostsCount = findViewById(R.id.tvPostsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setProfileImage() {
        int savedImageId = sessionManager.getProfileImage();
        if (savedImageId != -1) {
            profileImage.setImageResource(savedImageId);
        }
    }

    private void validateAndLoadUserData() {
        if (email != null) {
            setupLiveUserDataListener(email);
        } else {
            // If no email is found in session, prompt user to log in
            Toast.makeText(this, "Please log in to view your profile.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private void setupLiveUserDataListener(String email) {
        // Replace '.' with ',' in email to avoid Firebase path issues
        String sanitizedEmail = email.replace(".", ",");

        // Get reference to the specific user in the database
        userRef = FirebaseDatabase.getInstance().getReference("users").child(sanitizedEmail);

        // Create a listener for real-time updates
        userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HelperClassPOJO user = dataSnapshot.getValue(HelperClassPOJO.class);
                    if (user != null) {
                        // Update user profile information in real-time
                        updateUserProfileUI(user);
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found in database.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this,
                        "Failed to load user data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        };

        // Attach the listener to the database reference
        userRef.addValueEventListener(userDataListener);
    }

    private void updateUserProfileUI(HelperClassPOJO user) {
        // Set username
        tvUsername.setText(user.getUser());

        // Set email
        tvUserEmail.setText(email);

        // Set user role from the database
        tvUserRole.setText(user.getUserRole());

        // Update profile stats from the database
        tvPostsCount.setText(String.valueOf(user.getPostsCount()));
        tvFollowersCount.setText(String.valueOf(user.getFollowersCount()));
        tvFollowingCount.setText(String.valueOf(user.getFollowingCount()));
    }

    private void setupClickListeners() {
        // Set logout click listener
        btnLogout.setOnClickListener(v -> {
            // Clear the saved profile image on logout
            sessionManager.saveProfileImage(-1);

            // Logout from session
            sessionManager.logout();

            // Show logout message
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to login
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        startActivity(new Intent(ProfileActivity.this, loginActivity1.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener to prevent memory leaks
        if (userRef != null && userDataListener != null) {
            userRef.removeEventListener(userDataListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}