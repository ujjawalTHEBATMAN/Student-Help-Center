package com.example.abcd.adminfeature;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogStorageHelper {

    private DatabaseReference logsRef;
    private static final String DEFAULT_EMAIL = "system@example.com";  // Default email if none provided

    public LogStorageHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        logsRef = database.getReference("logs");
    }

    // Original method
    public void storeLog(String logTitle, String logMessage, String email) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        LogEntry logEntry = new LogEntry(logTitle, logMessage, timestamp, email);
        String logId = logsRef.push().getKey();
        if (logId != null) {
            logsRef.child(logId).setValue(logEntry);
        }
    }

    // New method for simple logging (similar to Log.d)
    public void log(String tag, String message) {
        storeLog(tag, message, DEFAULT_EMAIL);
    }

    // Overloaded method with custom email
    public void log(String tag, String message, String email) {
        storeLog(tag, message, email);
    }
}