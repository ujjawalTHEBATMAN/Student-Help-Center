package com.example.abcd;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.abcd.utils.SessionManager;
import com.google.firebase.database.ValueEventListener;
import android.os.ParcelFileDescriptor;
import java.io.IOException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class storage extends AppCompatActivity implements StorageAdapter.DeleteListener {
    private Uri selectedImageUri;
    private ImageView uploadImageView;
    private EditText fileNameEditText;
    private MaterialButton uploadButton;
    private DatabaseReference userStorageRef;
    private SessionManager sessionManager;
    private String userEmail;
    private RecyclerView recyclerView;
    private StorageAdapter adapter;

    private TextView storageStatusText, imageCountText;
    private ProgressBar storageProgress;
    private StorageTracker storageTracker;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    uploadImageView.setImageURI(uri);
                    validateUploadButton();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        // Existing code
        FloatingActionButton fab = findViewById(R.id.fabAddFile);
        fab.setOnClickListener(v -> showUploadDialog());
        setupUserStorage();
        initializeViews();
        loadFiles();
        initCloudinary();

        // New initialization
        storageStatusText = findViewById(R.id.storageStatusText);
        imageCountText = findViewById(R.id.imageCountText);
        storageProgress = findViewById(R.id.storageProgress);
        storageTracker = new StorageTracker();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.filesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new StorageAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }
    private void checkFileSize(Uri imageUri) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "r");
            long fileSize = pfd.getStatSize();
            pfd.close();

            if(!storageTracker.canUpload(fileSize)) {
                Toast.makeText(this, "Not enough storage space!", Toast.LENGTH_SHORT).show();
                return;
            }
            storageTracker.addFile(fileSize);
            storageTracker.updateUI();
        } catch (IOException e) {
            Log.e("Storage", "File size error", e);
        }
    }
    private void loadFiles() {
        if (userStorageRef == null) {
            Toast.makeText(this, "Firebase reference is not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        userStorageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<StorageFileModel> filesList = new ArrayList<>();
                for (DataSnapshot fileSnapshot : snapshot.getChildren()) {
                    try {
                        StorageFileModel file = new StorageFileModel(fileSnapshot);
                        filesList.add(file);
                    } catch (IllegalArgumentException e) {
                        fileSnapshot.getRef().removeValue();
                        Log.w("InvalidEntry", "Removed invalid file entry: " + fileSnapshot.getKey());
                    }
                }
                adapter.updateFiles(filesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(storage.this,
                        "Failed to load files: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUserStorage() {
        sessionManager = new SessionManager(this);
        userEmail = sessionManager.getEmail();

        if (userEmail == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String sanitizedEmail = userEmail.replace(".", ",");
        userStorageRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sanitizedEmail)
                .child("storage");
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dqlhjvblv");
            config.put("api_key", "965822312279393");
            config.put("api_secret", "OhXXmqN1MluEb5uX0gPYbNPnfd0");
            config.put("secure", "true");
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    private void showUploadDialog() {
        Dialog uploadDialog = new Dialog(this);
        uploadDialog.setContentView(R.layout.dialog_upload);
        uploadDialog.setCancelable(true);

        uploadImageView = uploadDialog.findViewById(R.id.dialogImageView);
        fileNameEditText = uploadDialog.findViewById(R.id.dialogFileName);
        uploadButton = uploadDialog.findViewById(R.id.dialogUploadButton);
        MaterialButton insertButton = uploadDialog.findViewById(R.id.dialogInsertButton);

        uploadButton.setEnabled(false);

        insertButton.setOnClickListener(v -> pickImage.launch("image/*"));

        uploadButton.setOnClickListener(v -> {
            if (selectedImageUri != null && !fileNameEditText.getText().toString().trim().isEmpty()) {
                uploadToCloudinary(selectedImageUri, fileNameEditText.getText().toString().trim());
                uploadDialog.dismiss();
            }
        });

        fileNameEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUploadButton();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        uploadDialog.show();
    }

    private void validateUploadButton() {
        boolean isValid = selectedImageUri != null &&
                fileNameEditText != null &&
                !fileNameEditText.getText().toString().trim().isEmpty();
        if (uploadButton != null) {
            uploadButton.setEnabled(isValid);
        }
    }

    private void uploadToCloudinary(Uri imageUri, String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            Toast.makeText(this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }
        checkFileSize(imageUri);
        Toast.makeText(this, "Starting upload...", Toast.LENGTH_SHORT).show();

        String sanitizedEmail = userEmail.replaceAll("[^a-zA-Z0-9]", "_");
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        MediaManager.get().upload(imageUri)
                .option("public_id", sanitizedEmail + "/" + uniqueFileName)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveToFirebase(imageUrl, fileName);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> Toast.makeText(storage.this,
                                "Upload failed: " + error.getDescription(),
                                Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    @Override
    public void onDeleteFile(String fileId) {
        // Add this to existing implementation
        userStorageRef.child(fileId).get().addOnSuccessListener(snapshot -> {
            Long fileSize = snapshot.child("fileSize").getValue(Long.class);
            if(fileSize != null) {
                storageTracker.removeFile(fileSize);
                storageTracker.updateUI();
            }
        });

        // Keep original deletion code
        if (userStorageRef != null) {
            userStorageRef.child(fileId).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this,
                            "File deleted successfully",
                            Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this,
                            "Failed to delete file: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
        }
    }

    private long getFileSize(Uri uri) {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
            return pfd != null ? pfd.getStatSize() : 0;
        } catch (IOException e) {
            Log.e("Storage", "Error getting file size", e);
            return 0;
        }
    }

    private void saveToFirebase(String imageUrl, String fileName) {
        // Add file size calculation
        long fileSize = getFileSize(selectedImageUri);

        String fileId = UUID.randomUUID().toString();
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("id", fileId);
        fileData.put("name", fileName);
        fileData.put("url", imageUrl);
        fileData.put("timestamp", System.currentTimeMillis());
        fileData.put("fileSize", fileSize); // Add this line

        userStorageRef.child(fileId).setValue(fileData)
                .addOnSuccessListener(aVoid -> runOnUiThread(() ->
                        Toast.makeText(storage.this,
                                "File uploaded successfully",
                                Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> runOnUiThread(() ->
                        Toast.makeText(storage.this,
                                "Failed to save file data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()));
    }


    private class StorageTracker {
        private static final long MAX_STORAGE = 10 * 1024 * 1024; // 10MB
        private long usedStorage = 0;
        private int fileCount = 0;

        boolean canUpload(long fileSize) {
            return (usedStorage + fileSize) <= MAX_STORAGE;
        }

        void addFile(long fileSize) {
            usedStorage += fileSize;
            fileCount++;
        }

        void removeFile(long fileSize) {
            usedStorage = Math.max(0, usedStorage - fileSize);
            fileCount = Math.max(0, fileCount - 1);
        }

        void updateUI() {
            runOnUiThread(() -> {
                storageStatusText.setText(String.format("Storage: %dMB/10MB",
                        usedStorage / (1024 * 1024)));
                imageCountText.setText(String.format("Images: %d", fileCount));
                storageProgress.setProgress((int) ((usedStorage * 100) / MAX_STORAGE));
            });
        }
    }

}