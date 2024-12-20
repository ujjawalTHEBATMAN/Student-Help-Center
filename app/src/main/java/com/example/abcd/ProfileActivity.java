package com.example.abcd;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private MaterialButton changeImageButton;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String userEmail; // Replace with actual user email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize views
        profileImageView = findViewById(R.id.profileImage);
        changeImageButton = findViewById(R.id.changeImageButton);

        // Set click listener for change image button
        changeImageButton.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Display selected image
                profileImageView.setImageURI(imageUri);
                
                // Upload image to Supabase and Firebase
                uploadImage(imageUri);
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        // Generate random number for image
        Random random = new Random();
        int imageNumber = random.nextInt(1000000);
        
        // Create unique filename
        String filename = "profile_" + UUID.randomUUID().toString() + "_" + imageNumber;
        
        // Upload to Firebase Storage
        StorageReference fileRef = storageReference.child(filename);
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save image URL and number to Firebase Database
                        String userPath = userEmail.replace(".", ",");
                        DatabaseReference userRef = databaseReference.child(userPath);
                        userRef.child("profileImageUrl").setValue(uri.toString());
                        userRef.child("imageNumber").setValue(imageNumber);
                        
                        Toast.makeText(ProfileActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // TODO: Add your Supabase upload code here using your Supabase client
        // The implementation will depend on your Supabase setup
    }
}