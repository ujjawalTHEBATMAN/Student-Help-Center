package com.example.abcd.ocrcapture;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.example.abcd.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ocrcapture extends AppCompatActivity {

    private ShapeableImageView scannedImageView;
    private MaterialTextView resultTextView;
    private MaterialButton copyButton;
    private ProgressBar progressBar;
    private FloatingActionButton btnFullScreen;
    private Uri imageUri;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    handleCameraResult();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleGalleryResult(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrcapture);
        initializeViews();
        setupClickListeners();
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());
        handleIncomingIntent();
    }

    private void initializeViews() {
        scannedImageView = findViewById(R.id.scannedImage);
        resultTextView = findViewById(R.id.resultText);
        copyButton = findViewById(R.id.copyButton);
        progressBar = findViewById(R.id.progressBar);
        btnFullScreen = findViewById(R.id.btnFullScreen);
        scannedImageView.setVisibility(View.GONE);
        resultTextView.setVisibility(View.GONE);
        copyButton.setVisibility(View.GONE);
        btnFullScreen.setVisibility(View.GONE);
        findViewById(R.id.imageCard).setVisibility(View.GONE);
        findViewById(R.id.resultCard).setVisibility(View.GONE);
        findViewById(R.id.imageContainer).setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        findViewById(R.id.scanButton).setOnClickListener(v -> checkCameraPermission());
        findViewById(R.id.galleryButton).setOnClickListener(v -> launchGallery());
        resultTextView.setOnClickListener(v -> copyOCRText());
        copyButton.setOnClickListener(v -> copyOCRText());
        btnFullScreen.setOnClickListener(v -> showFullScreenPreview());
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if (intent.getType().startsWith("image/")) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    handleGalleryResult(uri);
                }
            }
        }
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
            Toast.makeText(this, "Error creating file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "DOC_SCAN_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void handleCameraResult() {
        executor.execute(() -> {
            try {
                Bitmap bitmap = loadAndRotateBitmap(imageUri);
                runOnUiThread(() -> {
                    updateUIWithImage(bitmap);
                    runOCR(bitmap);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void handleGalleryResult(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Invalid image URI", Toast.LENGTH_SHORT).show();
            return;
        }
        imageUri = uri;
        progressBar.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                Bitmap bitmap = loadAndRotateBitmap(imageUri);
                runOnUiThread(() -> {
                    updateUIWithImage(bitmap);
                    runOCR(bitmap);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private Bitmap loadAndRotateBitmap(Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, 1024, 1024);
        Bitmap bitmap;
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        }
        try (InputStream exifStream = getContentResolver().openInputStream(uri)) {
            if (exifStream != null) {
                ExifInterface exif = new ExifInterface(exifStream);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                bitmap = rotateBitmap(bitmap, orientation);
            }
        }
        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        options.inJustDecodeBounds = true;
        try (InputStream input = getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(input, null, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        options.inJustDecodeBounds = false;
        return inSampleSize;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setScale(1, -1);
                break;
            default:
                return bitmap;
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    private void updateUIWithImage(Bitmap bitmap) {
        scannedImageView.setImageBitmap(bitmap);
        scannedImageView.setVisibility(View.VISIBLE);
        findViewById(R.id.imageContainer).setVisibility(View.VISIBLE);
        btnFullScreen.setVisibility(View.VISIBLE);
        findViewById(R.id.imageCard).setVisibility(View.VISIBLE);
        resultTextView.setVisibility(View.GONE);
        copyButton.setVisibility(View.GONE);
        findViewById(R.id.resultCard).setVisibility(View.GONE);
    }

    private void runOCR(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    progressBar.setVisibility(View.GONE);
                    displayOCRResults(visionText.getText());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "OCR failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayOCRResults(String text) {
        if (text.trim().isEmpty()) {
            resultTextView.setText("No text detected");
        } else {
            resultTextView.setText(text);
        }
        resultTextView.setVisibility(View.VISIBLE);
        copyButton.setVisibility(View.VISIBLE);
        findViewById(R.id.resultCard).setVisibility(View.VISIBLE);
    }

    private void copyOCRText() {
        String text = resultTextView.getText().toString();
        if (!text.equals("No text detected") && !text.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OCR Result", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFullScreenPreview() {
        if (imageUri == null) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "View Image"));
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}