package com.example.abcd;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.abcd.utils.FileUtils;
import com.example.abcd.utils.PDFStorageManager;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ManageQuestionPapersActivity extends AppCompatActivity {

    private PDFStorageManager pdfStorageManager;
    private String currentSemester;
    private String currentSubject;
    
    private ActivityResultLauncher<Intent> pdfPickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_question_papers);
        
        currentSemester = getIntent().getStringExtra("SEMESTER");
        currentSubject = getIntent().getStringExtra("SUBJECT");
        
        if (currentSemester == null || currentSubject == null) {
            finish();
            return;
        }
        
        pdfStorageManager = new PDFStorageManager(this);
        
        setupViews();
        setupPDFPicker();
    }
    
    private void setupViews() {
        MaterialButton uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> pickPDF());
        
        // Set title
        setTitle(currentSubject + " - Question Papers");
    }
    
    private void setupPDFPicker() {
        pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri pdfUri = result.getData().getData();
                    if (pdfUri != null) {
                        uploadPDF(pdfUri);
                    }
                }
            }
        );
    }
    
    private void pickPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        pdfPickerLauncher.launch(intent);
    }
    
    private void uploadPDF(Uri pdfUri) {
        String fileName = FileUtils.getFileName(this, pdfUri);
        
        boolean success = pdfStorageManager.savePDF(pdfUri, currentSemester, currentSubject, fileName);
        if (success) {
            Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
