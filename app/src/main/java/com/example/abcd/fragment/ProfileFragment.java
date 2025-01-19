package com.example.abcd.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.abcd.R;
import com.example.abcd.loginActivity1;
import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.example.abcd.models.UserStats;
import com.example.abcd.storage;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ProfileFragment extends Fragment {

    // Profile Basic Information Views
    private MaterialTextView tvUsername, tvUserEmail, tvUserRole;
    private MaterialTextView tvPostsCount, tvFollowersCount, tvFollowingCount;
    private MaterialButton btnLogout;
    private ShapeableImageView profileImage;
    private Toolbar toolbar;
    private ImageButton imageButton;
    // Session and Firebase Management
    private SessionManager sessionManager;
    private String email;
    private DatabaseReference userRef;
    private ValueEventListener userDataListener;
    private View rootView;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private Uri selectedImageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCloudinary();



    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dqlhjvblv");
            config.put("api_key", "965822312279393");
            config.put("api_secret", "OhXXmqN1MluEb5uX0gPYbNPnfd0");
            config.put("secure", "true");
            MediaManager.init(requireContext(), config);
        } catch (IllegalStateException e) {
            // MediaManager already initialized
        }
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

        // Load profile image from Firebase
        loadProfileImageFromFirebase();

        // Validate and load user data
        validateAndLoadUserData();

        // Setup click listeners
        setupClickListeners();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), storage.class));
            }
        });

    }

    @SuppressLint("WrongViewCast")
    private void initializeViews() {
        // Profile Basic Information
        toolbar = rootView.findViewById(R.id.toolbar);
        tvUsername = rootView.findViewById(R.id.tvUsername);
        tvUserEmail = rootView.findViewById(R.id.tvUserEmail);
        tvUserRole = rootView.findViewById(R.id.tvUserRole);
        profileImage = rootView.findViewById(R.id.profileImage);
        imageButton = rootView.findViewById(R.id.imageButton);

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

    private void loadProfileImageFromFirebase() {
        if (email != null && isAdded() && getContext() != null) {
            String sanitizedEmail = email.replace(".", ",");
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(sanitizedEmail);

            userRef.child("imageSend").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (isAdded() && getContext() != null) {
                        String imageUrl = dataSnapshot.getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Clear Glide cache for this image
                            Glide.get(requireContext()).clearMemory();
                            // Load image using Glide
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_default_profile)
                                    .error(R.drawable.ic_default_profile)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(profileImage);
                        } else {
                            // Set default image if no URL is found
                            profileImage.setImageResource(R.drawable.ic_default_profile);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (isAdded() && getContext() != null) {
                        // Handle error and set default image
                        profileImage.setImageResource(R.drawable.ic_default_profile);
                        Toast.makeText(requireContext(),
                                "Failed to load profile image: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Set default image if no email is found
            if (isAdded() && getContext() != null) {
                profileImage.setImageResource(R.drawable.ic_default_profile);
            }
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

        // Load profile image
        loadProfileImageFromFirebase();

        // Update stats from Firebase
        String sanitizedEmail = email.replace(".", ",");
        DatabaseReference statsRef = FirebaseDatabase.getInstance().getReference("users")
            .child(sanitizedEmail)
            .child("stats");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded() && getContext() != null) {
                    if (dataSnapshot.exists()) {
                        UserStats stats = dataSnapshot.getValue(UserStats.class);
                        if (stats != null) {
                            // Update UI with stats
                            tvPostsCount.setText(String.valueOf(stats.getTotalQuizzes()));
                            tvFollowersCount.setText(String.valueOf(stats.getEarnedPoints()));
                            tvFollowingCount.setText(String.valueOf(stats.getTotalPoints()));
                        }
                    } else {
                        // Set default values if no stats exist
                        tvPostsCount.setText("0");
                        tvFollowersCount.setText("0");
                        tvFollowingCount.setText("0");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(),
                            "Failed to load stats: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        profileImage.setOnClickListener(v -> checkPermissionAndPickImage());
    }

    private void checkPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE
            );
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Show selected image in ImageView
                profileImage.setImageURI(selectedImageUri);
                // Upload to Cloudinary
                uploadImageToCloudinary(selectedImageUri);
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
        }

        String requestId = MediaManager.get().upload(imageUri)
                .unsigned("usersImage")
                .option("timeout", 60000)  // Increase timeout to 60 seconds
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), "Upload starting...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if (getContext() != null) {
                            double progress = (bytes * 100) / totalBytes;
                            Toast.makeText(requireContext(), 
                                "Uploading: " + (int)progress + "%", 
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        if (getContext() != null) {
                            String imageUrl = (String) resultData.get("secure_url"); // Use secure_url instead of url
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                // Save URL to Firebase
                                saveImageUrlToFirebase(imageUrl);
                            } else {
                                Toast.makeText(requireContext(), 
                                    "Failed to get image URL from Cloudinary", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (getContext() != null) {
                            Toast.makeText(requireContext(), 
                                "Upload error: " + error.getDescription(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Handle reschedule
                    }
                })
                .dispatch();
    }

    private void saveImageUrlToFirebase(String imageUrl) {
        if (email != null) {
            String sanitizedEmail = email.replace(".", ",");
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(sanitizedEmail);

            userRef.child("imageSend").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded() && getContext() != null) {
                            // Load the image immediately after successful upload
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_default_profile)
                                    .error(R.drawable.ic_default_profile)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(profileImage);
                            
                            Toast.makeText(requireContext(), 
                                "Profile image updated successfully", 
                                Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(requireContext(), 
                                "Failed to update profile image: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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