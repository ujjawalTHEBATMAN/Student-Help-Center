package com.example.abcd;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.abcd.fragment.dashboardFragment;
import com.example.abcd.fragment.homeFragment;
import com.example.abcd.fragment.treatMeWellFragment;
import com.example.abcd.fragment.ProfileFragment;

public class mainDashBoard extends AppCompatActivity {
    private BottomNavigationView bottomNavView;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dash_board);

        initializeViews();
        setupBottomNavigation();

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new homeFragment());
        }
    }

    private void initializeViews() {
        bottomNavView = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        bottomNavView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                // Handle navigation selection
                if (item.getItemId() == R.id.navigation_home) {
                    fragment = new homeFragment();
                } else if (item.getItemId() == R.id.navigation_dashboard) {
                    fragment = new dashboardFragment();
                } else if (item.getItemId() == R.id.treatme) {
                    fragment = new treatMeWellFragment();
                } else if (item.getItemId() == R.id.navigation_profile) {
                    fragment = new ProfileFragment();
                }

                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();

            activeFragment = fragment;
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // If we're not on the home fragment, go to home
        if (!(activeFragment instanceof homeFragment)) {
            bottomNavView.setSelectedItemId(R.id.navigation_home);
        } else {
            super.onBackPressed();
        }
    }
}