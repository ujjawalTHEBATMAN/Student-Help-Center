package com.example.abcd;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.abcd.fragment.dashboardFragment;
import com.example.abcd.fragment.homeFragment;
import com.example.abcd.fragment.treatMeWellFragment;
import com.example.abcd.fragment.ProfileFragment;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class mainDashBoard extends AppCompatActivity {
    private BottomNavigationView bottomNavView;
    private Fragment activeFragment;
    private String email;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);

        // Retrieve email from intent extras or session
        sessionManager = new SessionManager(this);
        validateAndLoadUserData();

        initializeViews();
        // Disable the global icon tint so we can reapply it selectively.
        bottomNavView.setItemIconTintList(null);
        setupBottomNavigation();
        // Manually tint non-profile menu items white.
        tintOtherMenuIcons();

        // Load the profile image from Firebase and update the profile icon.
        loadProfileImageFromDatabase();

        if (savedInstanceState == null) {
            loadFragment(new homeFragment());
        }
    }

    /**
     * Validates and loads the user email.
     * Checks for USER_EMAIL in the intent extras; otherwise, retrieves from SessionManager.
     * If no email is found, redirects to the login screen.
     */
    private void validateAndLoadUserData() {
        if (getIntent() != null && getIntent().hasExtra("USER_EMAIL")) {
            email = getIntent().getStringExtra("USER_EMAIL");
        } else {
            email = sessionManager.getEmail();
        }
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    /**
     * Redirects the user to the login activity.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, loginActivity1.class);
        startActivity(intent);
        finish();
    }

    /**
     * Initializes the BottomNavigationView.
     */
    private void initializeViews() {
        bottomNavView = findViewById(R.id.bottomNavigation);
    }

    /**
     * Sets up the BottomNavigationView with a listener to load the appropriate fragment.
     */
    private void setupBottomNavigation() {
        bottomNavView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    fragment = new homeFragment();
                } else if (id == R.id.navigation_dashboard) {
                    fragment = new dashboardFragment();
                } else if (id == R.id.treatme) {
                    fragment = new treatMeWellFragment();
                } else if (id == R.id.navigation_profile) {
                    fragment = new ProfileFragment();
                }
                return loadFragment(fragment);
            }
        });
    }

    /**
     * Loads the specified fragment into the fragment container.
     *
     * @param fragment The fragment to display.
     * @return True if the fragment is loaded; false otherwise.
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            activeFragment = fragment;
            return true;
        }
        return false;
    }

    /**
     * Iterates over all menu items and applies a white tint to those that are not the profile item.
     */
    private void tintOtherMenuIcons() {
        for (int i = 0; i < bottomNavView.getMenu().size(); i++) {
            MenuItem item = bottomNavView.getMenu().getItem(i);
            if (item.getItemId() != R.id.navigation_profile) {
                Drawable icon = item.getIcon();
                if (icon != null) {
                    icon = DrawableCompat.wrap(icon);
                    DrawableCompat.setTint(icon, getResources().getColor(R.color.white));
                    item.setIcon(icon);
                }
            }
        }
    }

    /**
     * Retrieves the profile image URL from Firebase using the validated email.
     * The image URL is expected to be stored under the "imageSend" key.
     */
    private void loadProfileImageFromDatabase() {
        if (email != null && !email.isEmpty()) {
            String sanitizedEmail = email.replace(".", ",");
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(sanitizedEmail);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String imageUrl = snapshot.child("imageSend").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            updateProfileIcon(imageUrl);
                        } else {
                            Toast.makeText(mainDashBoard.this, "No profile image found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mainDashBoard.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mainDashBoard.this,
                            "Failed to load profile image: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Uses Glide to load the image from the provided URL, applies a circular crop,
     * and sets the resulting Drawable as the icon for the profile item in the BottomNavigationView.
     * This drawable is not tinted, so it displays with its original colors.
     *
     * @param imageUrl The URL of the user's profile image.
     */
    private void updateProfileIcon(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        // Set the profile icon without applying any tint.
                        bottomNavView.getMenu().findItem(R.id.navigation_profile).setIcon(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        bottomNavView.getMenu().findItem(R.id.navigation_profile).setIcon(placeholder);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (!(activeFragment instanceof homeFragment)) {
            bottomNavView.setSelectedItemId(R.id.navigation_home);
        } else {
            super.onBackPressed();
        }
    }
}
