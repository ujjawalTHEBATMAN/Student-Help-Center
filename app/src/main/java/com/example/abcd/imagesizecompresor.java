package com.example.abcd;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class imagesizecompresor extends AppCompatActivity {
    private static final int MAX_IMAGE_SIZE = 4096;
    private static final int DEFAULT_QUALITY = 80;

    // Views
    private ImageView imageView;
    private AutoCompleteTextView formatSpinner;
    private Slider qualitySeekBar;
    private TextView imageNameText;
    private TextView imageDimensionsText;
    private TextView imageSizeText;
    private TextView comparisonText;
    private android.view.View resultLayout;
    private ImageView originalThumbnail;
    private ImageView compressedThumbnail;
    private android.view.View previewPlaceholder;
    private android.view.View imageInfoOverlay;
    private MaterialButton btnCompress;
    private MaterialButton btnShare;
    private MaterialButton btnSave;
    private FloatingActionButton btnFullScreen;
    private NestedScrollView nestedScrollView; // Added for scrolling

    // Data
    private Uri imageUri;
    private File compressedFile;
    private long originalSize = 0;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private MaterialToolbar toolbar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Activity Result Launchers
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleImageSelection(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagesizecompresor);
        initializeViews();
        setupListeners();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString("imageUri");
            if (uriString != null) {
                imageUri = Uri.parse(uriString);
                handleImageSelection(imageUri);
            }
        }

        handleIncomingIntent();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null) {
            outState.putString("imageUri", imageUri.toString());
        }
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        formatSpinner = findViewById(R.id.formatSpinner);
        qualitySeekBar = findViewById(R.id.qualitySeekBar);
        imageNameText = findViewById(R.id.imageNameText);
        imageDimensionsText = findViewById(R.id.imageDimensionsText);
        imageSizeText = findViewById(R.id.imageSizeText);
        comparisonText = findViewById(R.id.comparisonText);
        resultLayout = findViewById(R.id.resultLayout);
        originalThumbnail = findViewById(R.id.originalThumbnail);
        compressedThumbnail = findViewById(R.id.compressedThumbnail);
        previewPlaceholder = findViewById(R.id.previewPlaceholder);
        imageInfoOverlay = findViewById(R.id.imageInfoOverlay);
        btnCompress = findViewById(R.id.btnCompress);
        btnShare = findViewById(R.id.btnShare);
        btnSave = findViewById(R.id.btnSave);
        btnFullScreen = findViewById(R.id.btnFullScreen);
        nestedScrollView = findViewById(R.id.nestedScrollView); // Initialize NestedScrollView

        ArrayAdapter<CharSequence> formatAdapter = ArrayAdapter.createFromResource(this,
                R.array.compression_formats, android.R.layout.simple_spinner_item);
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(formatAdapter);
        formatSpinner.setText("JPEG", false);

        qualitySeekBar.setValue(DEFAULT_QUALITY);

        imageView.setVisibility(android.view.View.GONE);
        imageInfoOverlay.setVisibility(android.view.View.GONE);
        resultLayout.setVisibility(android.view.View.GONE);
        btnFullScreen.setVisibility(android.view.View.GONE);
    }

    private void setupListeners() {
        findViewById(R.id.btnSelect).setOnClickListener(v -> openGallery());
        findViewById(R.id.btnCamera).setOnClickListener(v -> openCamera());
        btnCompress.setOnClickListener(v -> startCompression());
        btnShare.setOnClickListener(v -> shareImage());
        btnSave.setOnClickListener(v -> saveImageToGallery());
        btnFullScreen.setOnClickListener(v -> showFullScreenPreview());

        formatSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String format = parent.getItemAtPosition(position).toString();
            updateQualitySliderForFormat(format);
        });
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if (intent.getType().startsWith("image/")) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    handleImageSelection(uri);
                }
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && imageUri != null) {
                    handleImageSelection(imageUri);
                }
            });

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            imageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", photoFile);
            takePictureLauncher.launch(imageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void handleImageSelection(Uri uri) {
        if (uri == null) return;
        imageUri = uri;

        Snackbar.make(imageView, "Loading image...", Snackbar.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                originalSize = getFileSize(uri);
                String fileName = getFileName(uri);
                BitmapFactory.Options options = getOptimalBitmapOptions(uri);
                final Bitmap bitmap = loadBitmap(uri, options);

                runOnUiThread(() -> {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(android.view.View.VISIBLE);
                    previewPlaceholder.setVisibility(android.view.View.GONE);
                    imageNameText.setText(fileName != null ? fileName : "Image");
                    imageDimensionsText.setText(String.format(Locale.getDefault(),
                            "%d × %d px", imageWidth, imageHeight));
                    imageSizeText.setText(formatFileSize(originalSize));
                    imageInfoOverlay.setVisibility(android.view.View.VISIBLE);
                    btnFullScreen.setVisibility(android.view.View.VISIBLE);
                    resultLayout.setVisibility(android.view.View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateQualitySliderForFormat(String format) {
        if ("PNG".equals(format)) {
            qualitySeekBar.setValue(100);
            qualitySeekBar.setEnabled(false);
        } else {
            qualitySeekBar.setEnabled(true);
            if (!qualitySeekBar.isEnabled()) {
                qualitySeekBar.setValue(DEFAULT_QUALITY);
            }
        }
    }

    private void startCompression() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        Snackbar snackbar = Snackbar.make(imageView, "Compressing image...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();

        executor.execute(() -> {
            try {
                File result = compressImage();
                runOnUiThread(() -> {
                    snackbar.dismiss();
                    if (result != null) {
                        compressedFile = result;
                        long compressedSize = result.length();
                        double ratio = (1 - (compressedSize / (double) originalSize)) * 100;
                        updateCompressionResults(compressedSize, ratio);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    snackbar.dismiss();
                    Toast.makeText(this,
                            "Compression failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private int[] getImageDimensions(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return new int[]{options.outWidth, options.outHeight};
    }

    private void updateCompressionResults(long compressedSize, double ratio) {
        resultLayout.setVisibility(android.view.View.VISIBLE);
        originalThumbnail.setImageURI(imageUri);
        compressedThumbnail.setImageURI(Uri.fromFile(compressedFile));

        String originalName = getFileName(imageUri);
        String compressedName = compressedFile.getName();
        int[] originalDimensions = { imageWidth, imageHeight };
        int[] compressedDimensions = getImageDimensions(compressedFile);

        String comparisonDetails = String.format(Locale.getDefault(),
                "Compression Successful!\n\n" +
                        "Original Image:\n" +
                        "Name: %s\n" +
                        "Dimensions: %d × %d px\n" +
                        "Size: %s\n\n" +
                        "Compressed Image:\n" +
                        "Name: %s\n" +
                        "Dimensions: %d × %d px\n" +
                        "Size: %s\n\n" +
                        "Saved: %.1f%%",
                originalName, originalDimensions[0], originalDimensions[1], formatFileSize(originalSize),
                compressedName, compressedDimensions[0], compressedDimensions[1], formatFileSize(compressedSize),
                ratio);

        comparisonText.setText(comparisonDetails);

        Snackbar.make(resultLayout, "Compression complete. See comparison details below.",
                Snackbar.LENGTH_LONG).setAction("OK", v -> {}).show();

        // Scroll to result section
        nestedScrollView.post(() -> nestedScrollView.smoothScrollTo(0, resultLayout.getTop()));
    }

    private File compressImage() throws IOException {
        Bitmap.CompressFormat format = getSelectedFormat();
        int quality = getSelectedQuality();
        File outputFile = createOutputFile(format);
        BitmapFactory.Options options = getOptimalBitmapOptions(imageUri);
        Bitmap bitmap = loadBitmap(imageUri, options);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            bitmap.compress(format, quality, fos);
            fos.flush();
        } finally {
            bitmap.recycle();
        }
        return outputFile;
    }

    private BitmapFactory.Options getOptimalBitmapOptions(Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(input, null, options);
        }
        options.inSampleSize = calculateScaleFactor(options, MAX_IMAGE_SIZE);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return options;
    }

    private Bitmap loadBitmap(Uri uri, BitmapFactory.Options options) throws IOException {
        Bitmap bitmap;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(input, null, options);
        }

        try (InputStream exifStream = getContentResolver().openInputStream(uri)) {
            if (exifStream != null) {
                ExifInterface exif = new ExifInterface(exifStream);
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = rotateBitmap(bitmap, orientation);
            }
        } catch (Exception e) {
            // Handle cases where EXIF data is unavailable
        }

        // Set dimensions after rotation
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();
        return bitmap;
    }

    private int calculateScaleFactor(BitmapFactory.Options options, int maxSize) {
        int width = options.outWidth;
        int height = options.outHeight;
        int scaleFactor = 1;
        while ((width / scaleFactor) > maxSize || (height / scaleFactor) > maxSize) {
            scaleFactor *= 2;
        }
        return scaleFactor;
    }

    private Bitmap.CompressFormat getSelectedFormat() {
        String format = formatSpinner.getText().toString();
        switch (format) {
            case "PNG": return Bitmap.CompressFormat.PNG;
            case "WEBP": return Bitmap.CompressFormat.WEBP;
            default: return Bitmap.CompressFormat.JPEG;
        }
    }

    private int getSelectedQuality() {
        return Math.max((int) qualitySeekBar.getValue(), 1);
    }

    private File createOutputFile(Bitmap.CompressFormat format) {
        String extension = format == Bitmap.CompressFormat.PNG ? "png" :
                format == Bitmap.CompressFormat.WEBP ? "webp" : "jpg";
        String fileName = "compressed_" + System.currentTimeMillis() + "." + extension;
        return new File(getExternalFilesDir(null), fileName);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(-90); break;
            default: return bitmap;
        }
        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (bitmap != rotatedBitmap) bitmap.recycle();
            return rotatedBitmap;
        } catch (OutOfMemoryError e) {
            return bitmap;
        }
    }

    private long getFileSize(Uri uri) throws IOException {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
            if (pfd != null) return pfd.getStatSize();
        }
        return 0;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private void shareImage() {
        if (compressedFile == null) {
            Toast.makeText(this, "Please compress an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri contentUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", compressedFile);
        Intent share = new Intent(Intent.ACTION_SEND)
                .setType("image/*")
                .putExtra(Intent.EXTRA_STREAM, contentUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share Compressed Image"));
    }

    private void saveImageToGallery() {
        if (compressedFile == null) {
            Toast.makeText(this, "Please compress an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            try {
                String originalName = getFileName(imageUri);
                String fileName;
                if (originalName != null) {
                    int dotIndex = originalName.lastIndexOf('.');
                    String baseName = dotIndex == -1 ? originalName : originalName.substring(0, dotIndex);
                    String extension = dotIndex == -1 ? ".jpg" : originalName.substring(dotIndex);
                    fileName = baseName + "_compressed" + extension;
                } else {
                    fileName = "Compressed_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format(new Date()) + ".jpg";
                }

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream output = getContentResolver().openOutputStream(uri);
                         InputStream input = new FileInputStream(compressedFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }
                    runOnUiThread(() -> Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to save image: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showFullScreenPreview() {
        if (imageUri == null) return;
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}