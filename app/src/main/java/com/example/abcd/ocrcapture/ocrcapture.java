package com.example.abcd.ocrcapture;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import com.example.abcd.R;
import java.io.File;
import java.io.IOException;

public class ocrcapture extends AppCompatActivity {

    private ShapeableImageView scannedImageView;
    private MaterialTextView resultTextView;
    private MaterialButton copyButton;
    private ProgressBar progressBar;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    handleCameraResult();
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrcapture);

        initializeViews();
        setupClickListeners();

        // Set navigation click on the toolbar (back button)
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        scannedImageView = findViewById(R.id.scannedImage);
        resultTextView = findViewById(R.id.resultText);
        copyButton = findViewById(R.id.copyButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        findViewById(R.id.scanButton).setOnClickListener(v -> checkCameraPermission());
        resultTextView.setOnClickListener(v -> copyOCRText());
        copyButton.setOnClickListener(v -> copyOCRText());
    }

    private void checkCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File file = createImageFile();
            imageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            showError("Error creating file");
            progressBar.setVisibility(View.GONE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "DOC_SCAN_" + timeStamp;
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void handleCameraResult() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            updateUIWithImage(bitmap);
            runOCR(bitmap);
        } catch (IOException e) {
            showError("Error loading image");
            progressBar.setVisibility(View.GONE);
        }
    }

    private void updateUIWithImage(Bitmap bitmap) {
        scannedImageView.setImageBitmap(bitmap);
        scannedImageView.setVisibility(View.VISIBLE);
        findViewById(R.id.imageCard).setVisibility(View.VISIBLE);
    }

    private void runOCR(Bitmap bitmap) {
        progressBar.setVisibility(View.VISIBLE);
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    progressBar.setVisibility(View.GONE);
                    displayOCRResults(visionText.getText());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showError("OCR failed: " + e.getMessage());
                });
    }

    private void displayOCRResults(String text) {
        resultTextView.setText(text);
        resultTextView.setVisibility(View.VISIBLE);
        copyButton.setVisibility(View.VISIBLE);
        findViewById(R.id.resultCard).setVisibility(View.VISIBLE);
    }

    private void copyOCRText() {
        String text = resultTextView.getText().toString();
        if (!text.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OCR Result", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }
}
