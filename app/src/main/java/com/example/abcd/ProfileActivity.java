package com.example.abcd;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.abcd.firebaseLogin.HelperClassPOJO;
import com.example.abcd.models.UserStats;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 123;

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
    private StorageReference storageRef;
    private Uri imageUri;

    // Progress Dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Initialize SessionManager and get email from session
        sessionManager = new SessionManager(this);
        email = sessionManager.getEmail();

        // Load profile image from Firebase
        loadProfileImage();

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

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile image...");
        progressDialog.setCancelable(false);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Profile");
            // Removed back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    private void loadProfileImage() {
        if (email != null) {
            String sanitizedEmail = email.replace(".", ",");
            StorageReference imageRef = storageRef.child(sanitizedEmail + ".jpg");
            
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Load image using Glide
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(profileImage);
            }).addOnFailureListener(e -> {
                // Set default image if no custom image exists
                profileImage.setImageResource(R.drawable.default_profile);
            });
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

        // Update stats from Firebase
        String sanitizedEmail = email.replace(".", ",");
        DatabaseReference statsRef = FirebaseDatabase.getInstance().getReference("users")
            .child(sanitizedEmail)
            .child("stats");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(ProfileActivity.this, 
                    "Failed to load stats: " + databaseError.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        // Add click listener for profile image
        profileImage.setOnClickListener(v -> openImageChooser());
    }

    private void openImageChooser() {
        if (checkStoragePermission()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
        } else {
            requestStoragePermission();
        }
    }

    private boolean checkStoragePermission() {
        return ActivityCompat.checkSelfPermission(this, 
            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, 
            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("Storage permission is needed to select profile image")
                .setPositiveButton("OK", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK 
            && data != null && data.getData() != null) {
            
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null && email != null) {
            try {
                // Show progress dialog
                progressDialog.show();
                progressDialog.setMessage("Processing image...");

                // Compress image in background thread
                new Thread(() -> {
                    try {
                        // Get bitmap from Uri
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        
                        // Compress bitmap
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        byte[] data = baos.toByteArray();

                        // Upload compressed image
                        String sanitizedEmail = email.replace(".", ",");
                        StorageReference imageRef = storageRef.child(sanitizedEmail + ".jpg");

                        runOnUiThread(() -> progressDialog.setMessage("Uploading image..."));

                        imageRef.putBytes(data)
                            .addOnSuccessListener(taskSnapshot -> {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileActivity.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                                loadProfileImage();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileActivity.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            })
                            .addOnProgressListener(snapshot -> {
                                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                runOnUiThread(() -> progressDialog.setMessage("Uploading: " + (int)progress + "%"));
                            });

                    } catch (IOException e) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();

            } catch (Exception e) {
                progressDialog.dismiss();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        }
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