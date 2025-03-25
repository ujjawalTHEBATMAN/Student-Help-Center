package com.example.abcd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagePreviewActivity extends AppCompatActivity {
    private SubsamplingScaleImageView imageView;
    private ProgressBar progressBar;
    private ExecutorService executor;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        // Initialize views
        imageView = findViewById(R.id.fullscreenImageView);
        progressBar = findViewById(R.id.progressBar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Image Preview");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize executor
        executor = Executors.newSingleThreadExecutor();

        // Get image URI from intent
        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            loadImage();
        } else {
            Toast.makeText(this, "Error: No image to display", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadImage() {
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                // First, get EXIF information to handle orientation
                int orientation = ExifInterface.ORIENTATION_UNDEFINED;
                try (InputStream exifStream = getContentResolver().openInputStream(imageUri)) {
                    if (exifStream != null) {
                        ExifInterface exif = new ExifInterface(exifStream);
                        orientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                    }
                } catch (Exception e) {
                    // Some URI schemes might not support EXIF data
                }

                // Use a temporary file to load the image
                final File tempFile = File.createTempFile("preview", ".jpg", getCacheDir());
                try (InputStream input = getContentResolver().openInputStream(imageUri);
                     FileOutputStream output = new FileOutputStream(tempFile)) {

                    // Copy the content
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.flush();
                }

                // Final orientation for the image
                final int finalOrientation = orientation;

                runOnUiThread(() -> {
                    try {
                        // Configure the image view based on EXIF orientation
                        imageView.setOrientation(getSubsamplingImageOrientation(finalOrientation));

                        // Load the image
                        imageView.setImage(ImageSource.uri(Uri.fromFile(tempFile)));

                        // Set image event listeners
                        imageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
                            @Override
                            public void onReady() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onImageLoadError(Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ImagePreviewActivity.this,
                                        "Error loading image: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ImagePreviewActivity.this,
                                "Error displaying image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ImagePreviewActivity.this,
                            "Error processing image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private int getSubsamplingImageOrientation(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}