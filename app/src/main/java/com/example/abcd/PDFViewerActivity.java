package com.example.abcd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.abcd.utils.PDFStorageManager;
import com.example.abcd.utils.FileUtils;
import com.example.abcd.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.os.Environment;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class PDFViewerActivity extends AppCompatActivity {

    private RecyclerView pdfRecyclerView;
    private PDFAdapter adapter;
    private List<PDFStorageManager.PDFFile> pdfFiles;
    private List<PDF> firebasePdfList;
    private String selectedSemester;
    private String selectedSubject;
    private PDFStorageManager pdfStorageManager;
    private static final int PICK_PDF_FILE = 1;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private static final int STORAGE_PERMISSION_CODE = 1001;
    private SessionManager sessionManager;
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        sessionManager = new SessionManager(this);

        FloatingActionButton fabAddPdf = findViewById(R.id.fabAddPdf);
        fabAddPdf.setVisibility(View.GONE);

        String userEmail = sessionManager.getEmail();
        if (userEmail != null) {
            fetchUserRole(userEmail);
        } else {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
        }

        selectedSemester = getIntent().getStringExtra("SEMESTER");
        selectedSubject = getIntent().getStringExtra("SUBJECT");

        if (selectedSemester == null || selectedSubject == null) {
            finish();
            return;
        }

        pdfStorageManager = new PDFStorageManager(this);
        firebasePdfList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("pdfUrls");

        pdfRecyclerView = findViewById(R.id.pdfRecyclerView);
        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pdfFiles = new ArrayList<>();
        adapter = new PDFAdapter(pdfFiles, firebasePdfList, false);
        pdfRecyclerView.setAdapter(adapter);

        TextView titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText(selectedSubject + " Question Papers");

        fabAddPdf.setOnClickListener(v -> {
            if ("teacher".equalsIgnoreCase(userRole)) {
                adapter.showAddUrlDialog(this);
            } else {
                Toast.makeText(this, "You do not have permission to add PDFs", Toast.LENGTH_SHORT).show();
            }
        });

        loadPDFs();
        retrievePdfUrls();
    }

    private void fetchUserRole(String email) {
        FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String role = userSnapshot.child("userRole").getValue(String.class);
                            if (role != null) {
                                userRole = role.toLowerCase();
                                adapter.setIsTeacher("teacher".equals(userRole));
                                updateFabVisibility();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PDFViewer", "Role fetch error: " + error.getMessage());
                    }
                });
    }

    private void updateFabVisibility() {
        FloatingActionButton fab = findViewById(R.id.fabAddPdf);
        fab.setVisibility("teacher".equalsIgnoreCase(userRole) ? View.VISIBLE : View.GONE);
    }

    private void loadPDFs() {
        pdfFiles.clear();
        pdfFiles.addAll(pdfStorageManager.getPDFsForSubject(selectedSemester, selectedSubject));
        adapter.notifyDataSetChanged();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    String fileName = com.example.abcd.utils.FileUtils.getFileName(PDFViewerActivity.this, uri);
                    if (fileName == null) {
                        fileName = "document_" + System.currentTimeMillis() + ".pdf";
                    }

                    pdfStorageManager.savePDF(uri, selectedSemester, selectedSubject, fileName);

                    loadPDFs();
                }
            }
        }
    }

    private class PDFAdapter extends RecyclerView.Adapter<PDFViewHolder> {
        private final List<PDFStorageManager.PDFFile> localPdfFiles;
        private final List<PDF> firebasePdfFiles;
        private boolean isTeacher;

        public PDFAdapter(List<PDFStorageManager.PDFFile> localPdfFiles, List<PDF> firebasePdfFiles, boolean isTeacher) {
            this.localPdfFiles = localPdfFiles;
            this.firebasePdfFiles = firebasePdfFiles;
            this.isTeacher = isTeacher;
        }

        public void setIsTeacher(boolean isTeacher) {
            this.isTeacher = isTeacher;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PDFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pdf, parent, false);
            return new PDFViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PDFViewHolder holder, int position) {
            if (position < localPdfFiles.size()) {
                bindLocalPdf(holder, localPdfFiles.get(position));
            } else {
                bindFirebasePdf(holder, firebasePdfFiles.get(position - localPdfFiles.size()));
            }
        }

        private void bindLocalPdf(PDFViewHolder holder, PDFStorageManager.PDFFile pdfFile) {
            holder.pdfNameTextView.setText(pdfFile.getName());
            holder.deleteButton.setVisibility(isTeacher ? View.VISIBLE : View.GONE);
            holder.renameButton.setVisibility(isTeacher ? View.VISIBLE : View.GONE);
            holder.downloadButton.setVisibility(View.GONE);
            holder.viewButton.setVisibility(View.VISIBLE);

            if (isTeacher) {
                holder.deleteButton.setOnClickListener(v -> showDeleteDialog(pdfFile, holder.getAdapterPosition()));
                holder.renameButton.setOnClickListener(v -> showRenameDialog(pdfFile));
            } else {
                holder.deleteButton.setOnClickListener(null);
                holder.renameButton.setOnClickListener(null);
            }

            holder.viewButton.setOnClickListener(v -> {
                Uri fileUri = pdfStorageManager.getPDFUri(selectedSemester, selectedSubject, pdfFile.getName());
                if (fileUri != null) {
                    openPdf(fileUri);
                } else {
                    Toast.makeText(PDFViewerActivity.this, "Error: Cannot open PDF", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void bindFirebasePdf(PDFViewHolder holder, PDF pdf) {
            holder.pdfNameTextView.setText(pdf.getName());
            holder.deleteButton.setVisibility(isTeacher ? View.VISIBLE : View.GONE);
            holder.renameButton.setVisibility(isTeacher ? View.VISIBLE : View.GONE);
            holder.downloadButton.setVisibility(View.VISIBLE);
            holder.viewButton.setVisibility(View.VISIBLE);

            if (isTeacher) {
                holder.deleteButton.setOnClickListener(v -> showDeleteFirebasePdfDialog(pdf));
                holder.renameButton.setOnClickListener(v -> showEditFirebasePdfDialog(pdf));
            } else {
                holder.deleteButton.setOnClickListener(null);
                holder.renameButton.setOnClickListener(null);
            }

            holder.downloadButton.setOnClickListener(v -> {
                if (pdf.getUrl() != null && !pdf.getUrl().isEmpty()) {
                    if (checkStoragePermission()) {
                        downloadPDF(pdf.getUrl());
                    } else {
                        requestStoragePermission(pdf.getUrl());
                    }
                } else {
                    Toast.makeText(PDFViewerActivity.this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
                }
            });

            holder.viewButton.setOnClickListener(v -> {
                if (pdf.getUrl() != null && !pdf.getUrl().isEmpty()) {
                    Intent intent = new Intent(PDFViewerActivity.this, PDFWebViewActivity.class);
                    intent.putExtra("PDF_URL", pdf.getUrl());
                    intent.putExtra("PDF_NAME", pdf.getName());
                    startActivity(intent);
                } else {
                    Toast.makeText(PDFViewerActivity.this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void showDeleteDialog(PDFStorageManager.PDFFile pdfFile, int position) {
            new AlertDialog.Builder(PDFViewerActivity.this)
                    .setTitle("Delete PDF")
                    .setMessage("Are you sure you want to delete '" + pdfFile.getName() + "'?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (pdfStorageManager.deletePDF(selectedSemester, selectedSubject, pdfFile.getName())) {
                            Toast.makeText(PDFViewerActivity.this, "PDF deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPDFs();
                        } else {
                            Toast.makeText(PDFViewerActivity.this, "Failed to delete PDF", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void showRenameDialog(PDFStorageManager.PDFFile pdfFile) {
            View dialogView = LayoutInflater.from(PDFViewerActivity.this).inflate(R.layout.dialog_rename, null);
            EditText renameEditText = dialogView.findViewById(R.id.renameEditText);

            String currentName = pdfFile.getName();
            String nameWithoutExtension = currentName.toLowerCase().endsWith(".pdf")
                    ? currentName.substring(0, currentName.length() - 4)
                    : currentName;

            renameEditText.setText(nameWithoutExtension);
            renameEditText.setSelection(nameWithoutExtension.length());

            new AlertDialog.Builder(PDFViewerActivity.this)
                    .setTitle("Rename PDF")
                    .setView(dialogView)
                    .setPositiveButton("Rename", (dialog, which) -> {
                        String newName = renameEditText.getText().toString().trim();
                        if (newName.isEmpty()) {
                            Toast.makeText(PDFViewerActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!newName.toLowerCase().endsWith(".pdf")) {
                            newName += ".pdf";
                        }

                        if (!isValidFileName(newName)) {
                            Toast.makeText(PDFViewerActivity.this, "Invalid filename. Avoid special characters", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (newName.equals(currentName)) {
                            Toast.makeText(PDFViewerActivity.this, "New name is same as current name", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (pdfStorageManager.updatePDFName(selectedSemester, selectedSubject, currentName, newName)) {
                            Toast.makeText(PDFViewerActivity.this, "File renamed successfully", Toast.LENGTH_SHORT).show();
                            loadPDFs();
                        } else {
                            Toast.makeText(PDFViewerActivity.this, "Failed to rename file", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void showDeleteFirebasePdfDialog(PDF pdf) {
            new AlertDialog.Builder(PDFViewerActivity.this)
                    .setTitle("Delete PDF")
                    .setMessage("Are you sure you want to delete this PDF?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteFirebasePdf(pdf);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void deleteFirebasePdf(PDF pdf) {
            DatabaseReference pdfRef = databaseReference
                    .child(selectedSemester)
                    .child(selectedSubject);

            pdfRef.orderByChild("url").equalTo(pdf.getUrl())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(PDFViewerActivity.this,
                                                    "PDF deleted successfully", Toast.LENGTH_SHORT).show();
                                            retrievePdfUrls();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(PDFViewerActivity.this,
                                                        "Failed to delete PDF: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show());
                                break;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(PDFViewerActivity.this,
                                    "Error: " + databaseError.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return localPdfFiles.size() + firebasePdfFiles.size();
        }

        public void showAddUrlDialog(Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_pdf_url, null);

            EditText urlInput = dialogView.findViewById(R.id.urlInput);
            EditText fileNameInput = dialogView.findViewById(R.id.fileNameInput);

            builder.setView(dialogView)
                    .setTitle("Add PDF URL")
                    .setPositiveButton("Add", (dialog, which) -> {
                        String url = urlInput.getText().toString().trim();
                        String fileName = fileNameInput.getText().toString().trim();

                        if (url.isEmpty() || fileName.isEmpty()) {
                            Toast.makeText(context, "Please enter both URL and file name", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!fileName.toLowerCase().endsWith(".pdf")) {
                            fileName += ".pdf";
                        }

                        String key = databaseReference.child(selectedSemester)
                                .child(selectedSubject)
                                .push()
                                .getKey();

                        if (key != null) {
                            // Fix: Swap the order to match PDF constructor (url, name)
                            PDF pdfData = new PDF(url, fileName); // Corrected order
                            databaseReference.child(selectedSemester)
                                    .child(selectedSubject)
                                    .child(key)
                                    .setValue(pdfData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "PDF URL added successfully", Toast.LENGTH_SHORT).show();
                                        retrievePdfUrls();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to add PDF URL: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private class PDFViewHolder extends RecyclerView.ViewHolder {
        TextView pdfNameTextView;
        ImageButton deleteButton;
        ImageButton renameButton;
        com.google.android.material.button.MaterialButton downloadButton;
        com.google.android.material.button.MaterialButton viewButton;

        PDFViewHolder(View itemView) {
            super(itemView);
            pdfNameTextView = itemView.findViewById(R.id.pdfNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            renameButton = itemView.findViewById(R.id.renameButton);
            downloadButton = itemView.findViewById(R.id.downloadButton);
            viewButton = itemView.findViewById(R.id.viewButton);
        }
    }

    private void openPdf(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No PDF viewer app found. Please install a PDF viewer.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void submitPdfUrl(String url) {
        if (!url.isEmpty()) {
            String fileName = com.example.abcd.utils.FileUtils.getFileNameFromUrl(url);
            PDF pdfData = new PDF(url, fileName);

            String key = databaseReference
                    .child(selectedSemester)
                    .child(selectedSubject)
                    .push()
                    .getKey();

            if (key != null) {
                databaseReference
                        .child(selectedSemester)
                        .child(selectedSubject)
                        .child(key)
                        .setValue(pdfData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "PDF URL saved successfully", Toast.LENGTH_SHORT).show();
                            Log.d("Firebase", "PDF URL stored: " + url);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save PDF URL", Toast.LENGTH_SHORT).show();
                            Log.e("Firebase", "Error storing PDF URL: " + e.getMessage());
                        });
            }
        }
    }

    private void retrievePdfUrls() {
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }

        valueEventListener = databaseReference.child(selectedSemester)
                .child(selectedSubject)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        firebasePdfList.clear();
                        for (DataSnapshot pdfSnapshot : snapshot.getChildren()) {
                            PDF pdf = pdfSnapshot.getValue(PDF.class);
                            if (pdf != null) {
                                firebasePdfList.add(pdf);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PDFViewerActivity.this,
                                "Failed to load PDFs: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePdfList(List<PDF> pdfList) {
        firebasePdfList.clear();
        firebasePdfList.addAll(pdfList);
        adapter.notifyDataSetChanged();
    }

    private void savePdfFromUrl(String url, Context context) {
        AlertDialog progressDialog = new AlertDialog.Builder(context)
                .setView(LayoutInflater.from(context).inflate(R.layout.progress_dialog, null))
                .setCancelable(false)
                .create();

        progressDialog.show();

        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            File tempFile = null;

            try {
                URL pdfUrl = new URL(url);
                connection = (HttpURLConnection) pdfUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.connect();

                tempFile = File.createTempFile("downloaded_pdf", ".pdf", context.getExternalFilesDir(null));
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();

                File finalTempFile = tempFile;
                runOnUiThread(() -> {
                    try {
                        boolean isSaved = pdfStorageManager.savePDF(
                                Uri.fromFile(finalTempFile),
                                selectedSemester,
                                selectedSubject,
                                com.example.abcd.utils.FileUtils.getFileNameFromUrl(url)
                        );

                        if (isSaved) {
                            Toast.makeText(context, "PDF downloaded successfully!", Toast.LENGTH_SHORT).show();
                            loadPDFs();
                        } else {
                            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show();
                        }
                    } finally {
                        progressDialog.dismiss();
                        if (finalTempFile != null) {
                            finalTempFile.delete();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(context, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                    if (inputStream != null) inputStream.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void openPdfFromUrl(String pdfUrl) {
        try {
            if (checkStoragePermission()) {
                downloadPDF(pdfUrl);
            } else {
                requestStoragePermission(pdfUrl);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission(String pdfUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to download PDFs to your device")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    STORAGE_PERMISSION_CODE);
                        })
                        .setNegativeButton("Cancel", (dialog, which) ->
                                dialog.dismiss())
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadPDF(String pdfUrl) {
        Toast.makeText(this, "Starting download...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                String fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1);
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    fileName = "document_" + System.currentTimeMillis() + ".pdf";
                }

                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }

                File outputFile = new File(downloadsDir, fileName);
                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                is.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "PDF downloaded to Downloads folder", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showEditFirebasePdfDialog(PDF pdf) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_firebase_pdf, null);
        EditText nameEditText = dialogView.findViewById(R.id.pdfNameEditText);
        EditText urlEditText = dialogView.findViewById(R.id.pdfUrlEditText);

        nameEditText.setText(pdf.getName());
        urlEditText.setText(pdf.getUrl());

        new AlertDialog.Builder(this)
                .setTitle("Edit PDF Details")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = nameEditText.getText().toString().trim();
                    String newUrl = urlEditText.getText().toString().trim();

                    if (newName.isEmpty() || newUrl.isEmpty()) {
                        Toast.makeText(this, "Name and URL cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newName.toLowerCase().endsWith(".pdf")) {
                        newName += ".pdf";
                    }

                    updatePdfInFirebase(pdf, newName, newUrl);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePdfInFirebase(PDF oldPdf, String newName, String newUrl) {
        DatabaseReference pdfRef = databaseReference
                .child(selectedSemester)
                .child(selectedSubject);

        pdfRef.orderByChild("url").equalTo(oldPdf.getUrl())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PDF pdf = new PDF(newUrl, newName);
                            snapshot.getRef().setValue(pdf)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(PDFViewerActivity.this,
                                                "PDF details updated successfully", Toast.LENGTH_SHORT).show();
                                        loadPDFs();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(PDFViewerActivity.this,
                                                    "Failed to update PDF: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show());
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(PDFViewerActivity.this,
                                "Error: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    public static class PDF {
        public String url;
        public String name;

        public PDF() {}

        public PDF(String url, String name) {
            this.url = url;
            this.name = name;
        }

        public String getUrl() { return url; }
        public String getName() { return name; }
        public void setUrl(String url) { this.url = url; }
        public void setName(String name) { this.name = name; }
    }

    private boolean isValidFileName(String fileName) {
        return fileName.matches("^[a-zA-Z0-9()\\s_.-]+\\.pdf$");
    }

    private static class FileUtils {
        public static String getFileNameFromUrl(String url) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            fileName = Uri.decode(fileName);
            return fileName;
        }
    }
}