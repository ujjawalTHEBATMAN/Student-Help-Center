package com.example.abcd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import com.google.android.material.slider.Slider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class imagesizecompresor extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private ImageView imageView;
    private AutoCompleteTextView formatSpinner;
    private Slider qualitySeekBar;
    private Slider scaleSeekBar;
    private TextView txtOriginalSize;
    private TextView txtCompressedSize;
    private View resultLayout;
    private Uri imageUri;
    private File compressedFile;
    private long originalSize = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> compressionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagesizecompresor);
        initializeViews();
        setupSpinners();
        setupSliders();
        setupButtons();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        formatSpinner = findViewById(R.id.formatSpinner);
        qualitySeekBar = findViewById(R.id.qualitySeekBar);
        scaleSeekBar = findViewById(R.id.scaleSeekBar);
        txtOriginalSize = findViewById(R.id.txtOriginalSize);
        txtCompressedSize = findViewById(R.id.txtCompressedSize);
        resultLayout = findViewById(R.id.resultLayout);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> formatAdapter = ArrayAdapter.createFromResource(this,
                R.array.compression_formats, android.R.layout.simple_spinner_item);
        formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(formatAdapter);
    }

    private void setupSliders() {
        qualitySeekBar.addOnChangeListener((slider, value, fromUser) -> {});
        scaleSeekBar.addOnChangeListener((slider, value, fromUser) -> {});
    }

    private void setupButtons() {
        findViewById(R.id.btnSelect).setOnClickListener(v -> openGallery());
        findViewById(R.id.btnCompress).setOnClickListener(v -> startCompression());
        findViewById(R.id.btnShare).setOnClickListener(v -> shareImage());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                originalSize = getFileSize(imageUri);
                txtOriginalSize.setText(formatFileSize(originalSize));
                Bitmap bitmap = loadBitmap(imageUri, calculateMaxSize());
                imageView.setImageBitmap(bitmap);
                resultLayout.setVisibility(View.GONE);
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCompression() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        compressionTask = executor.submit(() -> {
            runOnUiThread(() -> findViewById(R.id.progressBar).setVisibility(View.VISIBLE));
            try {
                File result = compressImage();
                runOnUiThread(() -> {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (result != null) {
                        compressedFile = result;
                        long compressedSize = result.length();
                        txtCompressedSize.setText(formatFileSize(compressedSize));
                        double ratio = (1 - (compressedSize / (double) originalSize)) * 100;
                        Toast.makeText(imagesizecompresor.this,
                                String.format(Locale.getDefault(), "Compression successful! Saved %.1f%%", ratio),
                                Toast.LENGTH_LONG).show();
                        resultLayout.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(imagesizecompresor.this,
                        "Compression failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private File compressImage() throws IOException {
        Bitmap bitmap = loadBitmap(imageUri, calculateMaxSize());
        Bitmap.CompressFormat format = getSelectedFormat();
        int quality = Math.max((int) qualitySeekBar.getValue(), 1);

        File outputFile = createOutputFile(format);
        FileOutputStream fos = new FileOutputStream(outputFile);
        bitmap.compress(format, quality, fos);
        fos.flush();
        fos.close();
        bitmap.recycle();
        return outputFile;
    }

    private Bitmap loadBitmap(Uri uri, int maxWidth) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        options.inSampleSize = calculateScaleFactor(options, maxWidth);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();

        InputStream exifStream = getContentResolver().openInputStream(uri);
        ExifInterface exif = new ExifInterface(exifStream);
        exifStream.close();
        return rotateBitmap(bitmap,
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED));
    }

    private int calculateScaleFactor(BitmapFactory.Options options, int maxWidth) {
        int width = options.outWidth;
        int scaleFactor = 1;
        while (width > maxWidth) {
            width /= 2;
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

    private int calculateMaxSize() {
        return (int) (Math.max(scaleSeekBar.getValue(), 1) / 100f * 4096);
    }

    private File createOutputFile(Bitmap.CompressFormat format) {
        String extension = format == Bitmap.CompressFormat.PNG ? "png" :
                format == Bitmap.CompressFormat.WEBP ? "webp" : "jpg";
        return new File(getExternalFilesDir(null),
                "compressed_" + System.currentTimeMillis() + "." + extension);
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
            default:
                return bitmap;
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotated;
    }

    private long getFileSize(Uri uri) throws IOException {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
            return pfd.getStatSize();
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = 0;
        double sizeDouble = size;
        while (sizeDouble > 1024 && digitGroups < units.length - 1) {
            sizeDouble /= 1024;
            digitGroups++;
        }
        return String.format(Locale.getDefault(), "%.1f %s", sizeDouble, units[digitGroups]);
    }

    private void shareImage() {
        if (compressedFile == null) return;
        Uri contentUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", compressedFile);
        Intent share = new Intent(Intent.ACTION_SEND)
                .setType("image/*")
                .putExtra(Intent.EXTRA_STREAM, contentUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(share, "Share Compressed Image"));
    }

    @Override
    protected void onDestroy() {
        if (compressionTask != null && !compressionTask.isDone()) {
            compressionTask.cancel(true);
        }
        executor.shutdown();
        super.onDestroy();
    }
}