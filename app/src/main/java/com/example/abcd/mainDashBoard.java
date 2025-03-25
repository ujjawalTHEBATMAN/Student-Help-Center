package com.example.abcd;

import android.annotation.SuppressLint;
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
import com.example.abcd.AuthenTication.login.loginActivity;
import com.example.abcd.R;
import com.example.abcd.fragment.adminhomeFragment;
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
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);

        sessionManager = new SessionManager(this);
        validateAndLoadUserData();
        initializeViews();

        // Load user role and customize UI accordingly
        loadUserRoleAndInflateMenu();

        // Disable default icon tint
        bottomNavView.setItemIconTintList(null);

        setupBottomNavigation();
        tintOtherMenuIcons();
        loadProfileImageFromDatabase();

        if (savedInstanceState == null) {
            loadFragment(new homeFragment()); // Default fragment
        }
    }

    private void loadUserRoleAndInflateMenu() {
        String sanitizedEmail = email.replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userRole = snapshot.child("userRole").getValue(String.class);

                    // Inflate the correct menu
                    bottomNavView.getMenu().clear();
                    if ("admin".equalsIgnoreCase(userRole)) {
                        getMenuInflater().inflate(R.menu.bottom_nav_admin, bottomNavView.getMenu());
                        bottomNavView.setBackgroundColor(getResources().getColor(R.color.admin_bg));  // Dark Blue
                    } else if ("teacher".equalsIgnoreCase(userRole)) {
                        getMenuInflater().inflate(R.menu.bottom_nav_teacher, bottomNavView.getMenu());
                        bottomNavView.setBackgroundColor(getResources().getColor(R.color.teacher_bg));  // Dark Green
                    } else if ("student".equalsIgnoreCase(userRole)) {
                        getMenuInflater().inflate(R.menu.bottom_nav_student, bottomNavView.getMenu());
                        bottomNavView.setBackgroundColor(getResources().getColor(R.color.student_bg));  // Light Gray
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mainDashBoard.this, "Error loading user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment fragment = null;

                if ("admin".equalsIgnoreCase(userRole)) {
                    if (id == R.id.menu_home) fragment = new homeFragment();
                    else if (id == R.id.menu_admin_home) fragment = new adminhomeFragment();
                    else if (id == R.id.menu_profile) fragment = new ProfileFragment();
                } else if ("teacher".equalsIgnoreCase(userRole)) {
                    if (id == R.id.menu_home) fragment = new homeFragment();
                    else if (id == R.id.menu_dashboard) fragment = new dashboardFragment();
                    else if (id == R.id.menu_profile) fragment = new ProfileFragment();
                } else if ("student".equalsIgnoreCase(userRole)) {
                    if (id == R.id.menu_home) fragment = new homeFragment();
                    else if (id == R.id.menu_dashboard) fragment = new dashboardFragment();
                    else if (id == R.id.menu_treatme) fragment = new treatMeWellFragment();
                    else if (id == R.id.menu_profile) fragment = new ProfileFragment();
                }

                return loadFragment(fragment);
            }
        });
    }

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

    private void initializeViews() {
        bottomNavView = findViewById(R.id.bottomNavigation);
    }

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

    private void tintOtherMenuIcons() {
        for (int i = 0; i < bottomNavView.getMenu().size(); i++) {
            MenuItem item = bottomNavView.getMenu().getItem(i);

            if (item.getItemId() != R.id.menu_profile) {
                Drawable icon = item.getIcon();
                if (icon != null) {
                    icon = DrawableCompat.wrap(icon);
                    DrawableCompat.setTint(icon, getResources().getColor(android.R.color.black));
                    item.setIcon(icon);
                }
            }
        }
    }

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
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mainDashBoard.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateProfileIcon(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        bottomNavView.getMenu().findItem(R.id.menu_profile).setIcon(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        bottomNavView.getMenu().findItem(R.id.menu_profile).setIcon(placeholder);
                    }
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, loginActivity.class);
        startActivity(intent);
        finish();
    }
}
