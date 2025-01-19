// StorageFileUpload.java
package com.example.abcd;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class storage extends AppCompatActivity {
    private Uri selectedImageUri;
    private ImageView uploadImageView;
    private EditText fileNameEditText;
    private MaterialButton uploadButton;
    private DatabaseReference userStorageRef;
    private SessionManager sessionManager;
    private String userEmail;
    private RecyclerView recyclerView;
    private StorageAdapter adapter;

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

        initializeViews();
        setupUserStorage();
        loadFiles();
        initCloudinary();

    }

    private void initializeViews() {
        FloatingActionButton addButton = findViewById(R.id.fabAddFile);
        addButton.setOnClickListener(v -> showUploadDialog());

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.filesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new StorageAdapter(this);
        recyclerView.setAdapter(adapter);

        // Load existing files
        loadFiles();
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
                    StorageFileModel file = new StorageFileModel();
                    file.setId(fileSnapshot.child("id").getValue(String.class));
                    file.setName(fileSnapshot.child("name").getValue(String.class));
                    file.setUrl(fileSnapshot.child("url").getValue(String.class));
                    file.setTimestamp(fileSnapshot.child("timestamp").getValue(Long.class));
                    filesList.add(file);
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
        Toast.makeText(this, "Starting upload...", Toast.LENGTH_SHORT).show();

        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        MediaManager.get().upload(imageUri)
                .option("public_id", userEmail + "/" + uniqueFileName)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Upload started
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Show progress if needed
                    }

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
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Handle reschedule
                    }
                })
                .dispatch();
    }

    private void saveToFirebase(String imageUrl, String fileName) {
        String fileId = UUID.randomUUID().toString();
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("id", fileId);
        fileData.put("name", fileName);
        fileData.put("url", imageUrl);
        fileData.put("timestamp", System.currentTimeMillis());

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
}