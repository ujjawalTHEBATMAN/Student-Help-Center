package com.example.abcd;

import com.google.firebase.database.DataSnapshot;
import java.util.Objects;

public class StorageFileModel {
    private String id;
    private String name;
    private String url;
    private long timestamp;
    private Long fileSize; // Changed to Long wrapper class

    public StorageFileModel() {}

    public StorageFileModel(DataSnapshot snapshot) {
        id = Objects.requireNonNull(snapshot.child("id").getValue(String.class));
        name = snapshot.child("name").getValue(String.class);
        url = Objects.requireNonNull(snapshot.child("url").getValue(String.class));
        timestamp = snapshot.child("timestamp").getValue(Long.class) != null ?
                snapshot.child("timestamp").getValue(Long.class) : 0L;

        // Handle fileSize with null safety
        fileSize = snapshot.child("fileSize").getValue(Long.class);
        if(fileSize == null) {
            fileSize = 0L; // Default value for old entries
        }

        if (name == null || name.isEmpty()) {
            name = "Unnamed File";
        }
    }

    public long getFileSize() {
        return fileSize != null ? fileSize : 0L;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getName() {
        return (name != null && !name.isEmpty()) ? name : "Unnamed File";
    }

    public String getUrl() {
        return url != null ? url : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = (name != null && !name.isEmpty()) ? name : "Unnamed File";
    }

    public void setUrl(String url) {
        this.url = (url != null && !url.isEmpty()) ? url : "";
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }
}