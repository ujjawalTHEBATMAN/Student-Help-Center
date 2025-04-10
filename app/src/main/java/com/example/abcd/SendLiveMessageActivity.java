package com.example.abcd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.abcd.models.Message;
import com.example.abcd.utils.SessionManager;
import java.util.Map;

public class SendLiveMessageActivity extends AppCompatActivity {
    private TextInputEditText editTextPost;
    private MaterialButton imageButton;
    private MaterialButton gifButton;
    private MaterialButton buttonPost;
    private MaterialButton removeImageButton;
    private ProgressBar progressBar;
    private ImageView imagePreview;
    private LinearLayout imagePreviewLayout;
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
        editTextPost = findViewById(R.id.editTextPost);
        imageButton = findViewById(R.id.imageButton);
        gifButton = findViewById(R.id.gifButton);
        buttonPost = findViewById(R.id.buttonPost);
        removeImageButton = findViewById(R.id.removeImageButton);
        progressBar = findViewById(R.id.progressBar);
        imagePreview = findViewById(R.id.imagePreview);
        imagePreviewLayout = findViewById(R.id.imagePreviewLayout);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        sessionManager = new SessionManager(this);
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                uploadImageToCloudinary(selectedImageUri);
            }
        });
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
        gifButton.setOnClickListener(v -> Toast.makeText(this, "GIF selection not implemented yet", Toast.LENGTH_SHORT).show());
        buttonPost.setOnClickListener(v -> {
            String messageText = editTextPost.getText().toString().trim();
            String currentUserName = sessionManager.getEmail();
            if (!messageText.isEmpty() && currentUserName != null) {
                sendMessage(messageText, currentUserName);
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
        removeImageButton.setOnClickListener(v -> {
            imagePreviewLayout.setVisibility(View.GONE);
            selectedImageUri = null;
            pendingImageUrl = null;
            Glide.with(this).clear(imagePreview);
        });
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        MediaManager.get().upload(imageUri).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {}
            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {}
            @Override
            public void onSuccess(String requestId, Map resultData) {
                progressBar.setVisibility(View.GONE);
                String imageUrl = (String) resultData.get("secure_url");
                handleUploadSuccess(imageUrl);
            }
            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SendLiveMessageActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }

    private void handleUploadSuccess(String imageUrl) {
        pendingImageUrl = imageUrl;
        imagePreviewLayout.setVisibility(View.VISIBLE);
        Glide.with(this).load(imageUrl).into(imagePreview);
        if (currentMessageId != null) {
            updateMessageWithImage(currentMessageId, imageUrl);
        }
    }

    private void updateMessageWithImage(String messageId, String imageUrl) {
        messagesRef.child(messageId).child("imageURL").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Image added to message", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add image to message", Toast.LENGTH_SHORT).show());
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
                    imagePreviewLayout.setVisibility(View.GONE);
                    pendingImageUrl = null;
                    currentMessageId = null;
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}