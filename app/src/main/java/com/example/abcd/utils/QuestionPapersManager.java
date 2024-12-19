package com.example.abcd.utils;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionPapersManager {
    private static final String STORAGE_PATH = "question_papers";
    private static final String DB_PATH = "subjects";
    
    private final FirebaseStorage storage;
    private final FirebaseDatabase database;
    
    public QuestionPapersManager() {
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
    }
    
    // Add a new subject to a semester
    public Task<Void> addSubjectToSemester(String semester, String subject) {
        String semesterKey = sanitizePath(semester);
        DatabaseReference semesterRef = database.getReference(DB_PATH).child(semesterKey);
        return semesterRef.push().setValue(subject);
    }
    
    // Upload a PDF file
    public UploadTask uploadPDF(String semester, String subject, Uri pdfUri, String fileName) {
        String path = getStoragePath(semester, subject, fileName);
        StorageReference fileRef = storage.getReference().child(path);
        return fileRef.putFile(pdfUri);
    }
    
    // Delete a PDF file
    public Task<Void> deletePDF(String semester, String subject, String fileName) {
        String path = getStoragePath(semester, subject, fileName);
        return storage.getReference().child(path).delete();
    }
    
    // Get storage reference for listing PDFs
    public StorageReference getPDFsReference(String semester, String subject) {
        String semesterKey = sanitizePath(semester);
        String subjectKey = sanitizePath(subject);
        return storage.getReference()
                .child(STORAGE_PATH)
                .child(semesterKey)
                .child(subjectKey);
    }
    
    // Get database reference for subjects
    public DatabaseReference getSubjectsReference(String semester) {
        String semesterKey = sanitizePath(semester);
        return database.getReference(DB_PATH).child(semesterKey);
    }
    
    // Initialize default subjects for a semester
    public Task<Void> initializeDefaultSubjects(String semester, List<String> subjects) {
        String semesterKey = sanitizePath(semester);
        DatabaseReference semesterRef = database.getReference(DB_PATH).child(semesterKey);
        
        Map<String, Object> updates = new HashMap<>();
        for (int i = 0; i < subjects.size(); i++) {
            updates.put("subject_" + i, subjects.get(i));
        }
        
        return semesterRef.updateChildren(updates);
    }
    
    // Initialize all semesters with default subjects
    public void initializeAllSemesters() {
        for (int i = 1; i <= 6; i++) {
            String semester = "semester_" + i;
            List<String> subjects = getDefaultSubjects(semester);
            if (!subjects.isEmpty()) {
                initializeDefaultSubjects(semester, subjects)
                    .addOnFailureListener(e -> 
                        System.err.println("Failed to initialize " + semester + ": " + e.getMessage()));
            }
        }
    }
    
    // Helper method to sanitize paths
    private String sanitizePath(String path) {
        return path.toLowerCase().replace(" ", "_");
    }
    
    // Helper method to construct storage path
    private String getStoragePath(String semester, String subject, String fileName) {
        return String.format("%s/%s/%s/%s",
                STORAGE_PATH,
                sanitizePath(semester),
                sanitizePath(subject),
                fileName);
    }
    
    // Get default subjects for testing
    public static List<String> getDefaultSubjects(String semester) {
        List<String> subjects = new ArrayList<>();
        switch (semester.toLowerCase()) {
            case "semester_1":
                subjects.add("Mathematics I");
                subjects.add("Physics");
                subjects.add("Chemistry");
                subjects.add("Introduction to Programming");
                subjects.add("English Communication");
                break;
            case "semester_2":
                subjects.add("Mathematics II");
                subjects.add("Electronics");
                subjects.add("Data Structures");
                subjects.add("Computer Organization");
                subjects.add("Environmental Science");
                break;
            case "semester_3":
                subjects.add("Discrete Mathematics");
                subjects.add("Operating Systems");
                subjects.add("Database Management Systems");
                subjects.add("Object-Oriented Programming");
                subjects.add("Software Engineering");
                break;
            case "semester_4":
                subjects.add("Theory of Computation");
                subjects.add("Computer Networks");
                subjects.add("Algorithm Design and Analysis");
                subjects.add("Web Development");
                subjects.add("Artificial Intelligence");
                break;
            case "semester_5":
                subjects.add("Machine Learning");
                subjects.add("Cloud Computing");
                subjects.add("Mobile Application Development");
                subjects.add("Big Data Analytics");
                subjects.add("Cybersecurity");
                break;
            case "semester_6":
                subjects.add("Capstone Project");
                subjects.add("Internet of Things");
                subjects.add("Blockchain Technology");
                subjects.add("Ethical Hacking");
                subjects.add("Advanced Topics in CS");
                break;
        }
        return subjects;
    }
}
