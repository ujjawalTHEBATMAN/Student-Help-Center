package com.example.abcd.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PDFStorageManager {
    private static final String PDF_DIR = "question_papers";
    private final Context context;

    public PDFStorageManager(Context context) {
        this.context = context;
    }

    // Get the base directory for storing PDFs
    private File getBaseDir() {
        File baseDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), PDF_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        return baseDir;
    }

    // Get directory for a specific semester and subject
    private File getSubjectDir(String semester, String subject) {
        File semesterDir = new File(getBaseDir(), sanitizePath(semester));
        File subjectDir = new File(semesterDir, sanitizePath(subject));
        if (!subjectDir.exists()) {
            subjectDir.mkdirs();
        }
        return subjectDir;
    }

    // Save a PDF file
    public boolean savePDF(Uri sourceUri, String semester, String subject, String fileName) {
        File targetDir = getSubjectDir(semester, subject);
        File targetFile = new File(targetDir, sanitizePath(fileName));

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return false;

            OutputStream outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a PDF file
    public boolean deletePDF(String semester, String subject, String fileName) {
        File file = new File(getSubjectDir(semester, subject), sanitizePath(fileName));
        return file.exists() && file.delete();
    }

    // Get all PDFs for a subject
    public List<PDFFile> getPDFsForSubject(String semester, String subject) {
        List<PDFFile> pdfFiles = new ArrayList<>();
        File subjectDir = getSubjectDir(semester, subject);
        
        File[] files = subjectDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (files != null) {
            for (File file : files) {
                pdfFiles.add(new PDFFile(
                    file.getName(),
                    file.length(),
                    Uri.fromFile(file)
                ));
            }
        }
        return pdfFiles;
    }

    // Get Uri for a specific PDF
    public Uri getPDFUri(String semester, String subject, String fileName) {
        File file = new File(getSubjectDir(semester, subject), sanitizePath(fileName));
        return Uri.fromFile(file);
    }

    // Update PDF name
    public boolean updatePDFName(String semester, String subject, String oldName, String newName) {
        File oldFile = new File(getSubjectDir(semester, subject), sanitizePath(oldName));
        File newFile = new File(getSubjectDir(semester, subject), sanitizePath(newName));
        
        // Don't overwrite existing files
        if (newFile.exists()) {
            return false;
        }
        
        return oldFile.renameTo(newFile);
    }

    private String sanitizePath(String path) {
        return path.toLowerCase().replaceAll("[^a-z0-9.]", "_");
    }

    public static class PDFFile {
        private final String name;
        private final long size;
        private final Uri uri;
        private final String path;

        public PDFFile(String name, long size, Uri uri) {
            this.name = name;
            this.size = size;
            this.uri = uri;
            this.path = uri.getPath();
        }

        public String getName() { return name; }
        public long getSize() { return size; }
        public Uri getUri() { return uri; }
        public String getPath() { return path; }
    }
}
