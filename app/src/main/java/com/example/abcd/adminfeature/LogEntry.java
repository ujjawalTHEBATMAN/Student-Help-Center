package com.example.abcd.adminfeature;

public class LogEntry {
    public String logTitle;
    public String logMessage;
    public String timestamp;
    public String email;  // New field

    public LogEntry() {
        // Default constructor required for Firebase
    }

    public LogEntry(String logTitle, String logMessage, String timestamp, String email) {
        this.logTitle = logTitle;
        this.logMessage = logMessage;
        this.timestamp = timestamp;
        this.email = email;
    }
}
