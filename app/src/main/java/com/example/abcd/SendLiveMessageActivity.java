package com.example.abcd;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.abcd.models.Message;
import com.example.abcd.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class SendLiveMessageActivity extends AppCompatActivity {
    private static final String TAG = SendLiveMessageActivity.class.getSimpleName();
    private EditText editTextPost;
    private ImageButton imageButton;
    private ProgressBar progressBar;
    private DatabaseReference messagesRef;
    private SessionManager sessionManager;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String currentMessageId;
    private String pendingImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_live_message);

        initializeCloudinary();
        initializeViews();
        setupImagePicker();
        setupFirebase();
        setupClickListeners();
    }

    private void initializeCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dqlhjvblv");
            config.put("api_key", "965822312279393");
            config.put("api_secret", "OhXXmqN1MluEb5uX0gPYbNPnfd0");
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            Log.d(TAG, "Cloudinary already initialized");
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        editTextPost = findViewById(R.id.editTextPost);
        imageButton = findViewById(R.id.imageButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    uploadImageToCloudinary(selectedImageUri);
                }
            }
        );
    }

    private void setupFirebase() {
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        sessionManager = new SessionManager(this);
    }

    private void setupClickListeners() {
        imageButton.setOnClickListener(v -> openImagePicker());

        MaterialButton buttonPost = findViewById(R.id.buttonPost);
        buttonPost.setOnClickListener(v -> {
            String messageText = editTextPost.getText().toString().trim();
            String currentUserName = sessionManager.getEmail();
            if (!messageText.isEmpty() && currentUserName != null) {
                sendMessage(messageText, currentUserName);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        String requestId = MediaManager.get().upload(imageUri)
            .callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    Log.d(TAG, "Upload started");
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    double progress = (bytes / (double) totalBytes) * 100;
                    Log.d(TAG, "Upload progress: " + progress + "%");
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    progressBar.setVisibility(View.GONE);
                    String imageUrl = (String) resultData.get("secure_url");
                    handleUploadSuccess(imageUrl);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SendLiveMessageActivity.this, 
                        "Upload failed: " + error.getDescription(), 
                        Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    Log.d(TAG, "Upload rescheduled");
                }
            })
            .dispatch();
    }

    private void handleUploadSuccess(String imageUrl) {
        pendingImageUrl = imageUrl;
        // Display the image in the EditText
        Glide.with(this)
            .load(imageUrl)
            .into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, 
                    @Nullable Transition<? super Drawable> transition) {
                    editTextPost.setCompoundDrawablesWithIntrinsicBounds(
                        null, resource, null, null);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    editTextPost.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, null, null);
                }
            });
        
        // If we have a current message, update it with the image URL
        if (currentMessageId != null) {
            updateMessageWithImage(currentMessageId, imageUrl);
        }
    }

    private void updateMessageWithImage(String messageId, String imageUrl) {
        DatabaseReference messageRef = messagesRef.child(messageId);
        messageRef.child("imageURL").setValue(imageUrl)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Image added to message", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Image URL added to message successfully");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to add image to message", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error adding image URL to message", e);
            });
    }

    private void sendMessage(String messageText, String currentUserName) {
        Message message = new Message(messageText, currentUserName);
        if (pendingImageUrl != null) {
            message.setImageURL(pendingImageUrl);
        }
        currentMessageId = String.valueOf(message.getTimestamp());
        messagesRef.child(currentMessageId).setValue(message)
            .addOnSuccessListener(aVoid -> {
                editTextPost.setText("");
                editTextPost.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                pendingImageUrl = null;
                currentMessageId = null;
                Log.d(TAG, "Message sent successfully");
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to send message", e);
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
