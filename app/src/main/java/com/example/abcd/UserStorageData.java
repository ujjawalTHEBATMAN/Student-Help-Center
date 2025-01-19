package com.example.abcd;

import com.example.abcd.models.UserStats;

import java.util.List;

public class UserStorageData {
    private String email;
    private String password;
    private String user;
    private String userRole;
    private List<StorageItem> personalStorage;
    private UserStats stats;

    public static class StorageItem {
        private String id;
        private String name;
        private String url;
        private long size;
        private long timestamp;
        private String type; // "image" or "file"

        // Getters and setters
    }

    // Getters and setters
}