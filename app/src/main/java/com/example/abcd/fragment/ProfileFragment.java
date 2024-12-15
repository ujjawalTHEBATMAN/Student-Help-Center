package com.example.abcd.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.R;
import com.example.abcd.loginActivity1;
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

public class ProfileFragment extends Fragment {

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
    private View rootView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize SessionManager and get email from session
        sessionManager = new SessionManager(requireContext());
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
        toolbar = rootView.findViewById(R.id.toolbar);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
        tvUserRole = rootView.findViewById(R.id.tvUserRole);
        profileImage = rootView.findViewById(R.id.profileImage);

        // Profile Stats
        tvPostsCount = rootView.findViewById(R.id.tvPostsCount);
        tvFollowersCount = rootView.findViewById(R.id.tvFollowersCount);
        tvFollowingCount = rootView.findViewById(R.id.tvFollowingCount);
        btnLogout = rootView.findViewById(R.id.btnLogout);
    }

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("User Profile");
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
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
            Toast.makeText(requireContext(), "Please log in to view your profile.", Toast.LENGTH_SHORT).show();
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
                if (isAdded() && getContext() != null) {
                    if (dataSnapshot.exists()) {
                        HelperClassPOJO user = dataSnapshot.getValue(HelperClassPOJO.class);
                        if (user != null) {
                            // Update user profile information in real-time
                            updateUserProfileUI(user);
                        } else {
                            Toast.makeText(requireContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                            redirectToLogin();
                        }
                    } else {
                        Toast.makeText(requireContext(), "User not found in database.", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(),
                            "Failed to load user data: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
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
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to login
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        if (getActivity() != null) {
            startActivity(new Intent(requireActivity(), loginActivity1.class));
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the listener to prevent memory leaks
        if (userRef != null && userDataListener != null) {
            userRef.removeEventListener(userDataListener);
        }
    }
}